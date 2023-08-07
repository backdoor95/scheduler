package com.fastcampus.minischeduler.user;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
import com.fastcampus.minischeduler.user.UserResponse.GetUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final AES256Utils aes256Utils;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final HttpServletRequest httpServletRequest;

    // Aws s3
    private final AmazonS3 amazonS3;

    //aws s3 버킷 이름.
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String getBucketName() {
        return this.bucketName;
    }

    /**
     * 회원가입 메서드입니다.
     * Controller에서 유효성 검사가 완료된 DTO를 받아 비밀번호를 BCrypt 인코딩 후 사용자 정보 테이블(user_tb)에 저장합니다.
     * @param request : 회원가입 시 기재한 정보
     * @return        : 회원가입 된 회원 정보
     */
    @Transactional
    public UserResponse.JoinDTO signup(
            UserRequest.JoinDTO request,
            MultipartFile image
    ) throws GeneralSecurityException, IOException {

        // 인코딩 및 사진 추가
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setEmail(aes256Utils.encryptAES256(request.getEmail()));
        request.setFullName(aes256Utils.encryptAES256(request.getFullName()));
        if (image != null) request.setProfileImage(uploadImageToS3(image));

        // 회원 가입
        User userPS = userRepository.save(request.toEntity());

        // USER 는 티켓 제공, ADMIN 은 제공 안함
        if (userPS.getRole().equals(Role.USER)) userPS.setSizeOfTicket(12 - Calendar.getInstance().get(Calendar.MONTH));
        if (userPS.getRole().equals(Role.ADMIN)) userPS.setSizeOfTicket(0);

        UserResponse.JoinDTO response = new UserResponse.JoinDTO(userPS);
        response.setFullName(aes256Utils.decryptAES256(response.getFullName()));
        response.setEmail(aes256Utils.decryptAES256(response.getEmail()));

        return response;
    }

    /**
     * 로그인합니다.
     * @param authentication : 인증된 계정 정보
     * @return               : 토큰
     */
    @Transactional
    public Map<String, Object> signin(Authentication authentication) throws GeneralSecurityException {

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

        // 프론트 요청으로 유저 정보 리턴
        UserResponse.UserDto responseUserInfo = new UserResponse.UserDto(loginUser);
        responseUserInfo.setId(loginUser.getId());
        responseUserInfo.setFullName(aes256Utils.decryptAES256(loginUser.getFullName()));
        responseUserInfo.setEmail(aes256Utils.decryptAES256(loginUser.getEmail()));
        responseUserInfo.setRole(loginUser.getRole());
        responseUserInfo.setProfileImage(loginUser.getProfileImage());
        responseUserInfo.setSizeOfTicket(loginUser.getSizeOfTicket());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtTokenProvider.create(loginUser));
        response.put("userInfo", responseUserInfo);

        return response;
    }

    /**
     * 사용자 정보를 조회합니다. - 여기는 스케줄 미포함된 DTO를 반환함.
     * @return           : 사용자 정보 DTO
     */
    @Transactional
    public GetUserInfoDTO getUserInfo(String token) throws GeneralSecurityException {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        User userPS = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        return GetUserInfoDTO.builder()
                .id(loginUserId)
                .email(aes256Utils.decryptAES256(userPS.getEmail()))
                .fullName(aes256Utils.decryptAES256(userPS.getFullName()))
                .profileImage(userPS.getProfileImage())
                .sizeOfTicket(userPS.getSizeOfTicket())
                .usedTicket(userPS.getUsedTicket())
                .profileImage(userPS.getProfileImage())
                .role(userPS.getRole())
                .build();
    }

    /**
     * 사용자 정보를 조회합니다. - 여기는 스케줄 포함된 DTO를 반환함.
     * Role = user 일때
     * @return           : user의 정보, 나의 티켓 리스트 목록리스트 반환.
     */
    @Transactional
    public UserResponse.GetRoleUserInfoDTO getRoleUserInfo(String token) throws GeneralSecurityException {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        User userPS = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        GetUserInfoDTO getUserInfoDTO = new GetUserInfoDTO(userPS);
        getUserInfoDTO.setEmail(aes256Utils.decryptAES256(userPS.getEmail()));
        getUserInfoDTO.setFullName(aes256Utils.decryptAES256(userPS.getFullName()));

        return UserResponse.GetRoleUserInfoDTO.builder()
                .getUserInfoDTO(getUserInfoDTO)
                .schedulerRoleUserList(userRepository.findRoleUserTicketListById(loginUserId))
                .build();
    }

    /**
     * 사용자 정보를 조회합니다. - 여기는 스케줄 포함된 DTO를 반환함.
     * Role = admin 일때
     * @return            : Admin의 정보, 나의 티켓 리스트 목록리스트, 행사현황정보 반환.
     */
    @Transactional
    public UserResponse.GetRoleAdminInfoDTO getRoleAdminInfo(String token) throws GeneralSecurityException {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        User userPS = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        // 미완성. repository에 쿼리작성해서 만들기.
        // 방안 1. 쿼리 만들기
        // 방안 2. 기존에 있던 findby .. 이용

        UserResponse.UserDto userDto = new UserResponse.UserDto(userPS);
        userDto.setEmail(aes256Utils.decryptAES256(userPS.getEmail()));
        userDto.setFullName(aes256Utils.decryptAES256(userPS.getFullName()));

        return UserResponse.GetRoleAdminInfoDTO.builder()
                .userDto(userDto)
                .getRoleAdminCountProgressDTO(userRepository.countAllScheduleUserProgressByAdminId(loginUserId))
                .registeredEventCount(userRepository.countAdminScheduleRegisteredEvent(loginUserId))
                .schedulerRoleAdminList(userRepository.findRoleAdminScheduleListById(loginUserId))
                .build();
    }

    @Transactional
    public GetUserInfoDTO updateUserInfo(
            UserRequest.UpdateUserInfoDTO updateUserInfoDTO,
            String token
    ) throws GeneralSecurityException {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        User userPS = userRepository.findById(loginUserId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));

        // encoding
        String encodedPassword = passwordEncoder.encode(updateUserInfoDTO.getPassword());
        String encodedFullName = aes256Utils.encryptAES256(updateUserInfoDTO.getFullName());

        userPS.updateUserInfo(encodedPassword, encodedFullName);//이름, 비번  수정

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.

        return GetUserInfoDTO.builder() // 업데이트되고 DB에 반영된 UserDTO를 반환합니다.
                .id(loginUserId)
                .fullName(aes256Utils.decryptAES256(updatedUser.getFullName()))
                .email(aes256Utils.decryptAES256(updatedUser.getEmail()))
                .profileImage(updatedUser.getProfileImage())
                .usedTicket(updatedUser.getUsedTicket())
                .sizeOfTicket(updatedUser.getSizeOfTicket())
                .role(updatedUser.getRole())
                .build();
    }

    /**
     * aws upload
     */
    public String changedImageName(String originName) { //이미지 이름 중복 방지를 위해 랜덤으로 생성
        String random = UUID.randomUUID().toString();
        return random + originName;
    }

    @Transactional
    public String uploadImageToS3(MultipartFile image) throws IOException { // 이미지를 S3에 업로드하고 이미지의 url을 반환

        String originName = image.getOriginalFilename(); // 원본 이미지 이름
        originName.substring(originName.lastIndexOf(".")); // 확장자
        String changedName = changedImageName(originName); // 새로 생성된 이미지 이름

        ObjectMetadata metadata = new ObjectMetadata(); // 메타데이터
        metadata.setContentType(image.getContentType()); // putObject의 인자로 들어갈 메타데이터를 생성.
        // 이미지만 받을 예정이므로 contentType은  "image/확장자"

        amazonS3.putObject(
                new PutObjectRequest(bucketName, changedName, image.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
                        // getInputStream에서 exception 발생 -> controller에서 처리

        //데이터베이스에 저장할 이미지가 저장된 주소
        return amazonS3.getUrl(bucketName, changedName).toString();
    }

    /**
     * 유저의 프로필 사진 업데이트 로직실행
     * @param multipartFile
     * @param userId
     * @return              : GetUserInfoDTO
     * @throws Exception    : 디코딩 에러
     */
    @Transactional
    public UserResponse.GetUserInfoDTO updateUserProfileImage(
            MultipartFile multipartFile,
            String token
    ) throws GeneralSecurityException, IOException {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        User userPS = userRepository.findById(loginUserId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));

        String imageURL = uploadImageToS3(multipartFile);

        userPS.updateUserProfileImage(imageURL);

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.

        // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
        return GetUserInfoDTO.builder()
                .id(loginUserId)
                .fullName(aes256Utils.decryptAES256(updatedUser.getFullName()))
                .email(aes256Utils.decryptAES256(updatedUser.getEmail()))
                .profileImage(updatedUser.getProfileImage())
                .usedTicket(updatedUser.getUsedTicket())
                .sizeOfTicket(updatedUser.getSizeOfTicket())
                .role(updatedUser.getRole())
                .build();
    }

    /**
     * 프로필 이미지 삭제
     * @param token      : 사용자 토큰
     * @return           : GetUserInfoDTO
     * @throws Exception : 디코딩 에러
     */
    @Transactional
    public UserResponse.GetUserInfoDTO deleteUserProfileImage(String token) throws GeneralSecurityException {

        User userPS = userRepository.findById(jwtTokenProvider.getUserIdFromToken(token))
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));
        String imageURL = "https://miniproject12storage.s3.ap-northeast-2.amazonaws.com/default.jpg";

        String url = userPS.getProfileImage();

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        if(!fileName.equals("default.jpg")) amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName)); // aws에서 삭제

        //지울때 url은 기본 프로필로 초기화
        userPS.updateUserProfileImage(imageURL);// profileImage에 파일위치 저장

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.

        // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
        return GetUserInfoDTO.builder()
                .id(updatedUser.getId())
                .fullName(aes256Utils.decryptAES256(updatedUser.getFullName()))
                .email(aes256Utils.decryptAES256(updatedUser.getEmail()))
                .profileImage(updatedUser.getProfileImage())
                .usedTicket(updatedUser.getUsedTicket())
                .sizeOfTicket(updatedUser.getSizeOfTicket())
                .role(updatedUser.getRole())
                .build();
    }

    /**
     * id로 User 객체를 찾습니다.
     * @return
     */
    public User findById(String token) {
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        return userRepository.findById(loginUserId).get();
    }

}