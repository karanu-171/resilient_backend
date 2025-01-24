package com.emtech.loanapp.Auth.Data.Role;

import com.emtech.loanapp.Auth.Role.RoleService;
import lombok.*;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleAccessRights {
    @Builder.Default
    private String name = null;

    @Builder.Default
    private RoleService.AccessRight accessRights = null;

}
