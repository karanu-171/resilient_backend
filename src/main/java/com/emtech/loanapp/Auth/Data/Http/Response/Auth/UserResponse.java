package com.emtech.loanapp.Auth.Data.Http.Response.Auth;

import com.emtech.loanapp.Auth.Data.User.UserData;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse implements Serializable {
    @Builder.Default
   private  List<UserData> userData = null;

}
