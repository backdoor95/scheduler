package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
import com.fastcampus.minischeduler.user.UserRequest.*;
import com.fastcampus.minischeduler.user.UserResponse.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LoginLogRepository loginLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    /**
     * 회원가입 메서드입니다.
     * Controller에서 유효성 검사가 완료된 DTO를 받아 비밀번호를 BCrypt 인코딩 후 사용자 정보 테이블(user_tb)에 저장합니다.
     * @param joinDTO
     * @return
     */
    @Transactional
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) {

        // 비밀번호 인코딩
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        // 회원 가입
        User userPS = userRepository.save(joinDTO.toEntity());

        return new UserResponse.JoinDTO(userPS);
    }

    /**
     *
     * @param loginDTO
     * @return
     */
    @Transactional(readOnly = false)
    public String signin(UserRequest.LoginDTO loginDTO) {
        try{
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginDTO.getEmail(),
                                    loginDTO.getPassword()
                            )
                    );

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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception401("인증되지 않았습니다.");
        }
    }

    @Transactional
    public GetUserInfoDTO getUserInfo(Long userId) {
        User userPS = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        return new GetUserInfoDTO(userPS);
    }

    @Transactional
    public Optional<User> updateUserInfo(
            UpdateUserInfoDTO updateUserInfoDTO,
            Long userId
    ) throws DataAccessException{

        userRepository.updateUserInfo(
                passwordEncoder.encode(updateUserInfoDTO.getPassword()),
                updateUserInfoDTO.getProfileImage(),
                userId
                );
        return userRepository.findById(userId);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

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
}
