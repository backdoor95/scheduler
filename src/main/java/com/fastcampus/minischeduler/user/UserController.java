package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.annotation.MyErrorLog;
import com.fastcampus.minischeduler.core.annotation.MyLog;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.Exception400;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class UserController {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @MyErrorLog
    @MyLog
    @PostMapping("/join")
    public ResponseEntity<?> join(
            @RequestBody
            @Valid
            UserRequest.JoinDTO joinRequestDTO,
            Errors errors
    ) {
        // 유효성 검사
        if(errors.hasErrors()) return null;

        if (userRepository.findByEmail(joinRequestDTO.getEmail()).isPresent())
            throw new Exception400("email", "이미 존재하는 이메일입니다."); // 중복 계정 검사

        UserResponse.JoinDTO joinResponseDTO = userService.signup(joinRequestDTO);
        ResponseDTO<?> responseDTO = new ResponseDTO<>(joinResponseDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody
            @Valid
            UserRequest.LoginDTO loginRequestDTO,
            Errors errors
    ) {
        if(errors.hasErrors()) return null;

        String jwt = userService.signin(loginRequestDTO); // 로그인 후 토큰 발행

        return ResponseEntity.ok()
                .header(JwtTokenProvider.HEADER, jwt)
                .body(new ResponseDTO<>());
    }

    // 사용자 정보 페이지 api
//    @GetMapping("/user/{id}")
//    public ResponseEntity<?> detail(
//            @PathVariable Long id,
//            @AuthenticationPrincipal MyUserDetails myUserDetails
//    ) throws JsonProcessingException {
//
//        if(id.longValue() != myUserDetails.getUser().getId()){
//            throw new Exception403("권한이 없습니다");
//        }
//
//        UserResponse.DetailOutDTO detailOutDTO = userService.getUserDetail(id);
//
//        return ResponseEntity.ok(new ResponseDTO<>(detailOutDTO));
//    }

    @GetMapping("/mypage/{id}")
    public ResponseEntity<?> getUserInfo(
            @PathVariable Long id,
            @RequestParam("role") String role,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        // mypage id와 로그인한 사용자 id비교
        System.out.println("id = "+id+" , loginUserId = "+loginUserId);
        if(!id.equals(loginUserId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        if(role.equals("admin")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserResponse.GetUserInfoDTO getUserInfoDTO = userService.getUserInfo(userId);
        System.out.println("*******"+ getUserInfoDTO+"******");
        return ResponseEntity.ok()
                .header(JwtTokenProvider.HEADER, token)
                .body(getUserInfoDTO);
    }

//    @GetMapping("/getUserInfo")
//    public ResponseEntity<?> getUserInfo2(// 원본
//            @RequestHeader(JwtTokenProvider.HEADER) String token
//    ){
//        Long userId = jwtTokenProvider.getUserIdFromToken(token);
//        UserResponse.GetUserInfoDTO getUserInfoDTO = userService.getUserInfo(userId);
//        return ResponseEntity.ok(getUserInfoDTO);
//    }

    @GetMapping("/mypage/update/{id}")
    public ResponseEntity<?> getUpdateUserInfo(// 수정필요
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        // mypage id와 로그인한 사용자 id비교
        if(!id.equals(loginUserId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }
        UserResponse.GetUserInfoDTO getUserInfoDTO = userService.getUserInfo(id);
        // user 객체를 이용한 작업 수행

        return ResponseEntity.ok()
                .header(JwtTokenProvider.HEADER, token)
                .body(getUserInfoDTO);
    }

    @PostMapping("/mypage/update/{id}")
    public ResponseEntity<?> postUpdateUserInfo(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestBody
            @Valid
            UserRequest.UpdateUserInfoDTO updateUserInfoDTO,
            Errors errors
    ) {
        if (errors.hasErrors()) return null;
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        // mypage update 작성자 id와 로그인한 사용자 id비교
        if (!id.equals(loginUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }
        User userPS = userService.updateUserInfo(updateUserInfoDTO, id)
                .orElseThrow(() -> new RuntimeException("유저 업데이트 실패"));
        // user 객체를 이용한 작업 수행

        return ResponseEntity.ok()
                .header(JwtTokenProvider.HEADER, token)
                .body(userPS);
    }

    // DB 데이터 엑셀 다운로드 테스트 중.
    @GetMapping("/excel")
    public String download() {

        return "/exceldown";
    }

    @GetMapping("/excel/download")
    public void excelDownload(HttpServletResponse httpServletResponse) throws Exception {

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
        for(User user : userService.getAllUsers()) {
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
