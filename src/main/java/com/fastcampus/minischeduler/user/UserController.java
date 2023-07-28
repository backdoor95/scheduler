package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.annotation.MyErrorLog;
import com.fastcampus.minischeduler.core.annotation.MyLog;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.Exception400;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.hibernate.metamodel.model.domain.internal.AbstractAttribute;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

//@RestController
@Controller
@RequiredArgsConstructor
public class UserController {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserService userService;
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
