package com.example.application.data.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Integer id;

    private String profilePicture;

    private String email;

    private String newPassword;
}
