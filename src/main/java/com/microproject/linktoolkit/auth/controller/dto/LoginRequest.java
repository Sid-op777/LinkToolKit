package com.microproject.linktoolkit.auth.controller.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}