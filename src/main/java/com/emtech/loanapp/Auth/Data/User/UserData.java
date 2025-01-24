package com.emtech.loanapp.Auth.Data.User;

import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserData implements Serializable {
    @Builder.Default
    private Long id = null;

    @Builder.Default
    private String username = null;

    @Builder.Default
    private String firstName = null;

    @Builder.Default
    private String lastName = null;
    @Builder.Default
    private String pickUpLocation=null;

    @Builder.Default
    private String email = null;

    @Builder.Default
    private String mobile = null;

    @Builder.Default
    private String status = null;

    @Builder.Default
    private Timestamp creationDate = null;

    @Builder.Default
    private Timestamp updateDate = null;

    @Builder.Default
    private Timestamp deletedDate = null;

    @Builder.Default
    private Integer isLoggedIn = null;

    @Builder.Default
    private List<UserRoleData> roles = null;
}
