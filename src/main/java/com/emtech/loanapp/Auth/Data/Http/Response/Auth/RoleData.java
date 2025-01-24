package com.emtech.loanapp.Auth.Data.Http.Response.Auth;

import com.emtech.loanapp.Auth.Data.Role.RoleAccessRights;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleData implements Serializable {
    @Builder.Default
    private Long id = null;

    @Builder.Default
    private String name = null;

    @Builder.Default
    private Integer status = null;

    @Builder.Default
    private Timestamp creationDate = null;

    @Builder.Default
    private Timestamp updateDate = null;

    @Builder.Default
    private List<RoleAccessRights> accessRights = null;
}
