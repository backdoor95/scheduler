package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRepository;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminRequest.SchedulerAdminRequestDto;
import static com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminResponse.SchedulerAdminResponseDto;

@Service
@RequiredArgsConstructor
public class SchedulerAdminService {

    @PersistenceContext
    private EntityManager entityManager;

    private final HttpServletResponse httpServletResponse;

    private final SchedulerAdminRepository schedulerAdminRepository;
    private final SchedulerUserRepository schedulerUserRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 전체 일정 목록을 출력합니다.
     * @return schedulerAdminResponseDtoList
     */
    @Transactional
    public List<SchedulerAdminResponseDto> getSchedulerList(){
        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findAll();
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers) {
            SchedulerAdminResponseDto schedulerAdminResponseDto =
                    SchedulerAdminResponseDto.builder()
                            .user(scheduler.getUser())
                            .scheduleStart(scheduler.getScheduleStart())
                            .scheduleEnd(scheduler.getScheduleEnd())
                            .title(scheduler.getTitle())
                            .description(scheduler.getDescription())
                            .image(scheduler.getImage())
                            .createdAt(scheduler.getCreatedAt())
                            .updatedAt(scheduler.getUpdatedAt())
                            .build();
            schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
        }
        return schedulerAdminResponseDtoList;
    }

    /**
     * year와 month로 해당하는 일정의 스케줄만 반환합니다
     * @param year, month
     * @return SchedulerAdminResponseDto
     */
    public List<SchedulerAdminResponseDto> getSchedulerListByYearAndMonth(
            Integer year,
            Integer month
    ) {

        YearMonth yearMonth = YearMonth.of(year, month);

        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findAll();
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers) {
            LocalDateTime scheduleStart = scheduler.getScheduleStart();
            YearMonth scheduleYearMonth = YearMonth.of(scheduleStart.getYear(), scheduleStart.getMonth());
            if(yearMonth.equals(scheduleYearMonth)) {
                SchedulerAdminResponseDto schedulerAdminResponseDto =
                        SchedulerAdminResponseDto.builder()
                                .user(scheduler.getUser())
                                .scheduleStart(scheduler.getScheduleStart())
                                .scheduleEnd(scheduler.getScheduleEnd())
                                .title(scheduler.getTitle())
                                .description(scheduler.getDescription())
                                .image(scheduler.getImage())
                                .createdAt(scheduler.getCreatedAt())
                                .updatedAt(scheduler.getUpdatedAt())
                                .build();
                schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
            }
        }
        return schedulerAdminResponseDtoList;
    }

    /**
     * 일정을 등록합니다.
     * @param schedulerAdminRequestDto, token
     * @return SchedulerAdminResponseDto
     */
    @Transactional
    public SchedulerAdminResponseDto createScheduler(
            SchedulerAdminRequestDto schedulerAdminRequestDto,
            String token
    ){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        SchedulerAdmin scheduler = SchedulerAdmin.builder()
                .user(user)
                .scheduleStart(schedulerAdminRequestDto.getScheduleStart())
                .scheduleEnd(schedulerAdminRequestDto.getScheduleEnd())
                .title(schedulerAdminRequestDto.getTitle())
                .description(schedulerAdminRequestDto.getDescription())
                .image(schedulerAdminRequestDto.getImage())
                .build();
        SchedulerAdmin saveScheduler = schedulerAdminRepository.save(scheduler);

        return SchedulerAdminResponseDto.builder()
                .user(saveScheduler.getUser())
                .scheduleStart(saveScheduler.getScheduleStart())
                .scheduleEnd(saveScheduler.getScheduleEnd())
                .title(saveScheduler.getTitle())
                .description(saveScheduler.getDescription())
                .image(saveScheduler.getImage())
                .createdAt(saveScheduler.getCreatedAt())
                .updatedAt(saveScheduler.getUpdatedAt())
                .build();
    }

    /**
     * 일정을 수정합니다.
     * @param id, schedulerAdminRequestDto
     * @return id
     */
    @Transactional
    public Long updateScheduler(
            Long id,
            SchedulerAdminRequestDto schedulerAdminRequestDto
    ){
        SchedulerAdmin scheduler = schedulerAdminRepository.findById(id).orElseThrow(
                ()-> new IllegalStateException("스케쥴러를 찾을 수 없습니다")
        );
        scheduler.update(
                schedulerAdminRequestDto.getScheduleStart(),
                schedulerAdminRequestDto.getScheduleEnd(),
                schedulerAdminRequestDto.getTitle(),
                schedulerAdminRequestDto.getDescription(),
                schedulerAdminRequestDto.getImage()
        );
        return id;
    }

    /**
     * 일정을 삭제합니다.
     * @param id, token
     * @return id
     */
    @Transactional
    public Long delete(Long id, String token){

        SchedulerAdminResponseDto schedulerAdminResponseDto = getSchedulerById(id);
       Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

       if(!schedulerAdminResponseDto.getUser().getId().equals(loginUserId)){
           throw new IllegalStateException("스케줄을 삭제할 권한이 없습니다.");
       }
       SchedulerAdmin schedulerAdmin = schedulerAdminRepository.findById(id)
               .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다"));

       List<SchedulerUser> schedulerUsers = schedulerUserRepository.findBySchedulerAdmin(schedulerAdmin);
       if(!schedulerUsers.isEmpty()){
           for(SchedulerUser schedulerUser : schedulerUsers){
               User user = schedulerUser.getUser();
               int ticket = user.getSizeOfTicket();
               user.setSizeOfTicket(ticket+1);
               userRepository.save(user);
           }
       }

       schedulerAdminRepository.deleteById(id);
       return id;
    }

    /**
     * 사용자 별 일정을 출력합니다.
     * year와 month가 null이 아니라면 해당하는 년도와 달로 출력합니다
     * @param keyword, year, month
     * @return List<SchedulerAdminResponseDto>
     */
    @Transactional
    public List<SchedulerAdminResponseDto> getSchedulerByFullName(
            String keyword,
            Integer year,
            Integer month
    ){
        YearMonth yearMonth = null;
        if(year != null && month != null) yearMonth = YearMonth.of(year, month);

        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findByUserFullNameContaining(keyword);
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers){
            if(yearMonth != null){
                LocalDateTime scheduleStart = scheduler.getScheduleStart();
                YearMonth scheduleYearMonth = YearMonth.of(scheduleStart.getYear(), scheduleStart.getMonth());
                if(yearMonth.equals(scheduleYearMonth)) {
                    SchedulerAdminResponseDto schedulerAdminResponseDto =
                            SchedulerAdminResponseDto.builder()
                                    .user(scheduler.getUser())
                                    .scheduleStart(scheduler.getScheduleStart())
                                    .scheduleEnd(scheduler.getScheduleEnd())
                                    .title(scheduler.getTitle())
                                    .description(scheduler.getDescription())
                                    .image(scheduler.getImage())
                                    .createdAt(scheduler.getCreatedAt())
                                    .updatedAt(scheduler.getUpdatedAt())
                                    .build();
                    schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
                }
            } else {
                SchedulerAdminResponseDto schedulerAdminResponseDto =
                        SchedulerAdminResponseDto.builder()
                                .user(scheduler.getUser())
                                .scheduleStart(scheduler.getScheduleStart())
                                .scheduleEnd(scheduler.getScheduleEnd())
                                .title(scheduler.getTitle())
                                .description(scheduler.getDescription())
                                .image(scheduler.getImage())
                                .createdAt(scheduler.getCreatedAt())
                                .updatedAt(scheduler.getUpdatedAt())
                                .build();
                schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
            }
        }
        return schedulerAdminResponseDtoList;
    }

    /**
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return SchedulerAdminResponseDto
     */
    @Transactional
    public SchedulerAdminResponseDto getSchedulerById(Long id){

        SchedulerAdmin scheduler = schedulerAdminRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );

        return SchedulerAdminResponseDto.builder()
                .user(scheduler.getUser())
                .scheduleStart(scheduler.getScheduleStart())
                .scheduleEnd(scheduler.getScheduleEnd())
                .title(scheduler.getTitle())
                .description(scheduler.getDescription())
                .image(scheduler.getImage())
                .createdAt(scheduler.getCreatedAt())
                .updatedAt(scheduler.getUpdatedAt())
                .build();
    }

    /**
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return SchedulerAdmin
     */
    public SchedulerAdmin getSchedulerAdminById(Long id) {
        return schedulerAdminRepository.findById(id).orElse(null);
    }

    /**
     * token으로 사용자를 찾아 사용자가 작성한 모든 schedule을 반환합니다.
     * @param token
     * @return List<SchedulerAdminResponseDto>
     */
    public List<SchedulerAdminResponseDto> getSchedulerListById(String token) {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        List<SchedulerAdmin> schedulerAdmins = schedulerAdminRepository.findByUser(user);
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin schedulerAdmin : schedulerAdmins){
            SchedulerAdminResponseDto schedulerAdminResponseDto =
                    SchedulerAdminResponseDto.builder()
                            .user(schedulerAdmin.getUser())
                            .scheduleStart(schedulerAdmin.getScheduleStart())
                            .scheduleEnd(schedulerAdmin.getScheduleEnd())
                            .title(schedulerAdmin.getTitle())
                            .description(schedulerAdmin.getDescription())
                            .image(schedulerAdmin.getImage())
                            .createdAt(schedulerAdmin.getCreatedAt())
                            .updatedAt(schedulerAdmin.getUpdatedAt())
                            .build();
            schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
        }
        return schedulerAdminResponseDtoList;
    }

    /**
     * 결재관리 페이지의 해당 기획사 공연에 티케팅한 사용자의 내역과
     * 승인현황 별 티케팅 수를 조회합니다.
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public SchedulerAdminResponse getAdminScheduleDetail(Long id) {

        SchedulerAdminResponse schedulerAdminResponse = new SchedulerAdminResponse(
                schedulerAdminRepository.findSchedulesWithUsersById(id),
                schedulerAdminRepository.countScheduleGroupByProgressById(id));

        return schedulerAdminResponse;
    }

    @Transactional
    public void updateUserSchedule(Long schedulerAdminId, Progress progress) {

        schedulerAdminRepository.updateUserScheduleById(schedulerAdminId, progress);
    }

    /**
     * 엑셀 파일을 다운받습니다.
     * @throws Exception
     */
    public void excelDownload(Long adminId) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("티케팅 현황"); // 엑셀 시트 생성
        sheet.setDefaultColumnWidth(28); // 디폴트 너비 설정

        /**
         * header font style
         */
        XSSFFont headerXSSFFont = (XSSFFont) workbook.createFont();
        headerXSSFFont.setColor(new XSSFColor(
                new byte[]{(byte) 255, (byte) 255, (byte) 255},
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
        EntityType<?> entityType = entityManager.getMetamodel().entity(SchedulerUser.class); // User 테이블의 메타정보
        Row row = null;
        Cell cell = null;
        int numberOfRow = 0;

        // Header
        List<Field> fields = Arrays.asList(SchedulerUser.class.getDeclaredFields());
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
        for (SchedulerUser schedulerUser : getAllTicketsOfThisAdmin(adminId)) {
            row = sheet.createRow(numberOfRow++); // 행 추가
            index = 0;
            for (Field field : schedulerUser.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                cell = row.createCell(index); // 열 추가
                if (field.get(schedulerUser) == null) cell.setCellValue("null");
                if (field.get(schedulerUser) != null) { // 데이터 추가
                    if (field.getName().equals("user")) {
                        User user = (User)field.get(schedulerUser);
                        cell.setCellValue(
                                user.getFullName() + " " + user.getEmail()
                        );
                    } else if (field.getName().equals("schedulerAdmin")) {
                        SchedulerAdmin schedulerAdmin = (SchedulerAdmin)field.get(schedulerUser);
                        cell.setCellValue(
                                "행사번호: " + schedulerAdmin.getId() +
                                "제목: " + schedulerAdmin.getTitle() +
                                " 기간: " + schedulerAdmin.getScheduleStart() +
                                "~ " + schedulerAdmin.getScheduleEnd()
                        );
                    } else {
                        cell.setCellValue(field.get(schedulerUser).toString());
                    }
                }
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

    public List<SchedulerUser> getAllTicketsOfThisAdmin(Long id) {
        return schedulerAdminRepository.findAllTicketsByAdminId(id);
    }
}
