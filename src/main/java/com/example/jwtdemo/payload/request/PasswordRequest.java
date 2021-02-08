package com.example.jwtdemo.payload.request;

import javax.validation.constraints.NotBlank;

public class PasswordRequest {

    @NotBlank
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
