package com.fastcampus.minischeduler.user;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.core.exception.ImageUploadException;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
import com.fastcampus.minischeduler.user.UserResponse.GetUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AES256Utils aes256Utils;

    private final LoginLogRepository loginLogRepository;
    private final HttpServletRequest httpServletRequest;

    // Aws s3
    private final AmazonS3 amazonS3;

    //aws s3 버킷 이름.
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;






    /**
     * 회원가입 메서드입니다.
     * Controller에서 유효성 검사가 완료된 DTO를 받아 비밀번호를 BCrypt 인코딩 후 사용자 정보 테이블(user_tb)에 저장합니다.
     *
     * @param joinDTO
     * @return
     */
    @Transactional
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) throws Exception {

        // 인코딩
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        joinDTO.setEmail(aes256Utils.encryptAES256(joinDTO.getEmail()));
        joinDTO.setFullName(aes256Utils.encryptAES256(joinDTO.getFullName()));

        // 회원 가입
        User userPS = userRepository.save(joinDTO.toEntity());

        // USER 는 티켓 제공, ADMIN 은 제공 안함
        if (userPS.getRole().equals(Role.USER)) userPS.setSizeOfTicket(12 - Calendar.getInstance().get(Calendar.MONTH));
        if (userPS.getRole().equals(Role.ADMIN)) userPS.setSizeOfTicket(null);

        UserResponse.JoinDTO response = new UserResponse.JoinDTO(userPS);
        response.setFullName(aes256Utils.decryptAES256(response.getFullName()));
        response.setEmail(aes256Utils.decryptAES256(response.getEmail()));

        return response;
    }

    @Transactional
    public GetUserInfoDTO getUserInfo(Long userId) throws Exception {

        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        GetUserInfoDTO getUserInfoDTO = GetUserInfoDTO.builder()
                .email(aes256Utils.decryptAES256(userPS.getEmail()))
                .fullName(aes256Utils.decryptAES256(userPS.getFullName()))
                .profileImage(userPS.getProfileImage())
                .sizeOfTicket(userPS.getSizeOfTicket())
                .usedTicket(userPS.getUsedTicket())
                .createdAt(userPS.getCreatedAt())
                .updatedAt(userPS.getUpdatedAt())
                .build();

        return getUserInfoDTO;
    }

    /**
     * 로그인합니다.
     * @param authentication
     * @return
     */
    @Transactional
    public String signin(Authentication authentication) {

        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        User loginUser = myUserDetails.getUser();

        // 최종 로그인 날짜 기록
        loginUser.onUpdateLatestLogin();

        // 로그 테이블 기록
        loginLogRepository.save(
                LoginLog.builder()
                        .userId(loginUser.getId())
                        .userAgent(httpServletRequest.getHeader("User-Agent"))
                        .clientIP(httpServletRequest.getRemoteAddr())
                        .build()
        );

        return JwtTokenProvider.create(loginUser);
    }

    @Transactional
    public GetUserInfoDTO updateUserInfo(
            UserRequest.UpdateUserInfoDTO updateUserInfoDTO,
            Long userId) throws Exception {

        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));

        // Password encoding - 암호화
        String encodedPassword = passwordEncoder.encode(updateUserInfoDTO.getPassword());

        String fullName = updateUserInfoDTO.getFullName();

        userPS.updateUserInfo(encodedPassword, fullName);//이름, 비번  수정

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.

        GetUserInfoDTO responseUser = GetUserInfoDTO.builder()
                .fullName(aes256Utils.decryptAES256(updatedUser.getFullName()))
                .email(aes256Utils.decryptAES256(updatedUser.getEmail()))
                .profileImage(updatedUser.getProfileImage())
                .usedTicket(updatedUser.getUsedTicket())
                .sizeOfTicket(updatedUser.getSizeOfTicket())
                .updatedAt(updatedUser.getUpdatedAt())
                .createdAt(updatedUser.getCreatedAt())
                .build();

        return responseUser; // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
    }





    /**
     * 모든 사용자를 조회합니다.
     * @return
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }



    /**
     * aws upload
     */


    private String changedImageName(String originName) { //이미지 이름 중복 방지를 위해 랜덤으로 생성
        String random = UUID.randomUUID().toString();
        return random+originName;
    }

    @Transactional
    private String uploadImageToS3(MultipartFile image) throws IOException { //이미지를 S3에 업로드하고 이미지의 url을 반환
        String originName = image.getOriginalFilename(); //원본 이미지 이름
        String ext = originName.substring(originName.lastIndexOf(".")); //확장자
        String changedName = changedImageName(originName); //새로 생성된 이미지 이름
        ObjectMetadata metadata = new ObjectMetadata(); //메타데이터
        metadata.setContentType(image.getContentType()); //putObject의 인자로 들어갈 메타데이터를 생성.
        // 이미지만 받을 예정이므로 contentType은  "image/확장자"

        PutObjectResult putObjectResult = amazonS3.putObject(new PutObjectRequest(
                bucketName, changedName, image.getInputStream(), metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead));// getInputStream에서 exception 발생 -> controller에서 처리

        return amazonS3.getUrl(bucketName, changedName).toString(); //데이터베이스에 저장할 이미지가 저장된 주소

    }

    @Transactional
    private void deleteImage(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }


    /**
     * 유저의 프로필 사진 업데이트 로직실행
     * @param userId
     * @return
     * @throws DataAccessException
     * @throws IOException
     */
    @Transactional
    public UserResponse.GetUserInfoDTO updateUserProfileImage(
            MultipartFile multipartFile,
            Long userId) throws Exception {


        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));

        String imageURL = uploadImageToS3(multipartFile);

        userPS.updateUserProfileImage(imageURL);

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.

        UserResponse.GetUserInfoDTO getUserInfoDTO
                = GetUserInfoDTO.builder()
                .fullName(aes256Utils.decryptAES256(updatedUser.getFullName()))
                .email(aes256Utils.decryptAES256(updatedUser.getEmail()))
                .profileImage(updatedUser.getProfileImage())
                .usedTicket(updatedUser.getUsedTicket())
                .sizeOfTicket(updatedUser.getSizeOfTicket())
                .updatedAt(updatedUser.getUpdatedAt())
                .createdAt(updatedUser.getCreatedAt())
                .build();

        return getUserInfoDTO; // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
    }

    /**
     *  이미지 삭제
     * @param userId
     * @return
     * @throws DataAccessException
     * @throws IOException
     */

    @Transactional
    public UserResponse.GetUserInfoDTO deleteUserProfileImage(
            Long userId) throws Exception {


        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));
        String imageURL = "https://miniproject12storage.s3.ap-northeast-2.amazonaws.com/default.jpg";
        String url = userPS.getProfileImage();

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        deleteImage(fileName);// aws에서 삭제


        //지울때 url은 기본 프로필로 초기화
        userPS.updateUserProfileImage(imageURL);// profileImage에 파일위치 저장

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.
        UserResponse.GetUserInfoDTO getUserInfoDTO
                = GetUserInfoDTO.builder()
                .fullName(aes256Utils.decryptAES256(updatedUser.getFullName()))
                .email(aes256Utils.decryptAES256(updatedUser.getEmail()))
                .profileImage(updatedUser.getProfileImage())
                .usedTicket(updatedUser.getUsedTicket())
                .sizeOfTicket(updatedUser.getSizeOfTicket())
                .updatedAt(updatedUser.getUpdatedAt())
                .createdAt(updatedUser.getCreatedAt())
                .build();


        return getUserInfoDTO; // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
    }




    /**
     * id로 User 객체를 찾습니다.
     * @param id
     * @return
     */
    public User findById(Long id){
        return userRepository.findById(id).get();
    }


}
