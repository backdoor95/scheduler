package com.fastcampus.minischeduler.core.exception;

public enum ErrorCode {
    FAIL_DECODING("디코딩에 실패하였습니다"),
    FAIL_IMAGE_UPLOAD("이미지 파일 전송에 실패하였습니다"),

    INVALID_YEAR("유효하지 않은 년도입니다."),
    INVALID_MONTH("유효하지 않은 달입니다."),
    INVALID_REQUEST("잘못된 요청입니다"),
    INVALID_ID("유효하지 않은 id값입니다"),
    INVALID_USER("권한이 없습니다"),
    INVALID_ACCESS("잘못된 접근입니다"),
    INVALID_CREATE_SCHEDULE("한달에 한번만 공연을 신청할 수 있습니다."),


    EMPTY_DATE("날짜정보가 비어있습니다"),
    EMPTY_TITLE("제목이 비어있습니다"),
    EMPTY_ROLE("권한을 입력해주세요"),
    EMPTY_ROLE_ADMIN_OR_FAN("'기획사' 또는 '팬'을 선택해주세요"),
    EMPTY_TICKET("해당 티켓이 존재하지 않습니다"),
    EMPTY_PROGRESS("'승인' 또는 '거절'을 선택해주세요"),
    EMPTY_SCHEDULEINFO("해당하는 공연의 정보를 찾을 수 없습니다"),



    ALREADY_ACCEPTED_TICKET("이미 승인된 티켓입니다"),
    ALREADY_REFUSED_TICKET("이미 거절된 티켓입니다"),


    EXISTING_EMAIL("이미 존재하는 이메일입니다"),
    FILE_CAPACITY_EXCEEDED("파일이 너무 큽니다"),
    CHECK_ID_PASSWORD("아이디와 비밀번호를 확인해주세요");




    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }



}
