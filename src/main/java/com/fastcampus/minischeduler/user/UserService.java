package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRepository;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRequest;
import com.fastcampus.minischeduler.user.UserRequest.*;
import com.fastcampus.minischeduler.user.UserResponse.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;


@RequiredArgsConstructor
@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SchedulerUserRepository schedulerUserRepository;

    private final LoginLogRepository loginLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    /**
     * 회원가입 메서드입니다.
     * Controller에서 유효성 검사가 완료된 DTO를 받아 비밀번호를 BCrypt 인코딩 후 사용자 정보 테이블(user_tb)에 저장합니다.
     *
     * @param joinDTO
     * @return
     */
    @Transactional
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) {

        // 비밀번호 인코딩
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));

        // 회원 가입
        User userPS = userRepository.save(joinDTO.toEntity());

        // USER 는 티켓 제공, ADMIN 은 제공 안함
        if (userPS.getRole().equals(Role.USER)) userPS.setSizeOfTicket(12 - Calendar.getInstance().get(Calendar.MONTH));
        if (userPS.getRole().equals(Role.ADMIN)) userPS.setSizeOfTicket(null);

        return new UserResponse.JoinDTO(userPS);
    }

    @Transactional
    public GetUserInfoDTO getUserInfo(Long userId) {
        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        return new UserResponse.GetUserInfoDTO(userPS);
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
    public User updateUserInfo(
            UserRequest.UpdateUserInfoDTO updateUserInfoDTO,
            Long userId) throws DataAccessException, IOException {


        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));


        // Password encoding - 암호화
        String encodedPassword = passwordEncoder.encode(updateUserInfoDTO.getPassword());

        // Image handling
        MultipartFile profileImageFile = updateUserInfoDTO.getProfileImage();
        String profileImagePath = null;
        if(profileImageFile != null) {// Amazon S3 또는 Google Cloud Storage
            String originalFilename = profileImageFile.getOriginalFilename();
            profileImagePath = "D:\\ImageFile\\" + originalFilename; // 제 컴퓨터에 저장하도록 하였습니다.
            profileImageFile.transferTo(new File(profileImagePath));
        }


        userPS.updateUserInfo(encodedPassword, profileImagePath);// profileImage에 파일위치 저장

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.

        return updatedUser; // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
    }

    /**
     * 모든 사용자를 조회합니다.
     * @return
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 엑셀 파일을 다운받습니다.
     * @throws Exception
     */
    public void excelDownload() throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("database"); // 엑셀 시트 생성
        sheet.setDefaultColumnWidth(28); // 디폴트 너비 설정

        /**
         * header font style
         */
        XSSFFont headerXSSFFont = (XSSFFont) workbook.createFont();
        headerXSSFFont.setColor(new XSSFColor(
                new byte[] {(byte)255, (byte)255, (byte)255},
                new DefaultIndexedColorMap()
        ));

        /**
         * header cell style
         */
        XSSFCellStyle headerXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

        // 테두리 설정
        headerXssfCellStyle.setBorderLeft(BorderStyle.THIN);
        headerXssfCellStyle.setBorderRight(BorderStyle.THIN);
        headerXssfCellStyle.setBorderTop(BorderStyle.THIN);
        headerXssfCellStyle.setBorderBottom(BorderStyle.THIN);

        // 셀 배경 설정
        headerXssfCellStyle.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte) 108, (byte) 39, (byte) 255},
                new DefaultIndexedColorMap()
        ));
        headerXssfCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerXssfCellStyle.setFont(headerXSSFFont);

        /**
         * body cell style
         */
        XSSFCellStyle bodyXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

        // 테두리 설정
        bodyXssfCellStyle.setBorderLeft(BorderStyle.THIN);
        bodyXssfCellStyle.setBorderRight(BorderStyle.THIN);
        bodyXssfCellStyle.setBorderTop(BorderStyle.THIN);
        bodyXssfCellStyle.setBorderBottom(BorderStyle.THIN);

        /**
         * header data
         */

        EntityType<?> entityType = entityManager.getMetamodel().entity(User.class); // User 테이블의 메타정보

        Row row = null;
        Cell cell = null;
        int numberOfRow = 0;

        // Header
        List<Field> fields = Arrays.asList(User.class.getDeclaredFields());
        fields.sort(Comparator.comparingInt(
                field -> {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null) {
                        return column.columnDefinition().length();
                    } else {
                        return 0;
                    }
                }));

        row = sheet.createRow(numberOfRow++); // 행 추가

        int index = 0;
        for (Field field : fields) {
            String fieldName = field.getName();

            cell = row.createCell(index);
            cell.setCellValue(fieldName);
            cell.setCellStyle(headerXssfCellStyle);
            index++;
        }

        // Body
        for(User user : getAllUsers()) {
            row = sheet.createRow(numberOfRow++); // 행 추가
            index = 0;
            for(Field field : user.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                cell = row.createCell(index); // 열 추가
                if(field.get(user) == null) cell.setCellValue("null");
                if(field.get(user) != null) cell.setCellValue(field.get(user).toString()); // 데이터 추가
                cell.setCellStyle(bodyXssfCellStyle); // 바디 스타일 추가
                index++;
            }
        }

        // 컨텐츠 타입과 파일명 지정
        httpServletResponse.setContentType("ms-vnd/excel");
        httpServletResponse.setHeader(
                "Content-Disposition",
                "attachment;filename=" + entityType.getName() + ".xlsx"
        );

        // Excel File Output
        workbook.write(httpServletResponse.getOutputStream());
        workbook.close();
    }

    /**
     * 매년 1월 1일 사용자의 티켓은 12개로 초기화 됩니다.
     */
    @Scheduled(cron = "0 0 0 1 1 *", zone = "Asia/Seoul")
    public void updateTicketEveryYear() {
        System.out.println("1월 1일이 되어 티켓을 12개로 초기화합니다.");
        userRepository.update12TicketsOfAllFans();
    }
}
