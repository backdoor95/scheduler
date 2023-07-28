package com.fastcampus.minischeduler.user;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String fullname;
    private int sizeOfTicket;
    private Role role;

    public UserDto(Long id, String fullname, int sizeOfTicket, Role role){
        this.id = id;
        this.fullname = fullname;
        this.sizeOfTicket = sizeOfTicket;
        this.role = role;
    }
}
