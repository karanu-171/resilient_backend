package com.emtech.loanapp.Auth.Data.Http.Request.Auth;

import lombok.Data;
@Data
public class UserUpdateRequest {
    private String email;
    private String phone;
}
