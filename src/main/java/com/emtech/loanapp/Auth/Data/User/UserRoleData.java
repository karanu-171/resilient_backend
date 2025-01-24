package com.emtech.loanapp.Auth.Data.User;

import com.emtech.loanapp.Auth.Data.Role.RoleAccessRights;
import lombok.*;

import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRoleData {
    @Builder.Default
    private String name = null;

    @Builder.Default
    private List<RoleAccessRights> accessRights = null;
}
