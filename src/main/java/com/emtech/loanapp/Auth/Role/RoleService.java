package com.emtech.loanapp.Auth.Role;

import com.emtech.loanapp.Auth.Data.Http.Response.Auth.RoleData;
import com.emtech.loanapp.Auth.Data.Http.Response.Auth.RoleResponse;
import com.emtech.loanapp.Auth.Data.Role.RoleAccessRights;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Getter
    public enum AccessRight {
        VIEW_DASHBOARD("View dashboard"),
        VIEW_BUSINESS_PROFILE("View business profile"),
        VIEW_COUNTIES("View counties"),
        CREATE_COUNTY("Create county"),
        UPDATE_COUNTY("Update county"),
        DELETE_COUNTY("Delete county"),
        VIEW_SUB_COUNTIES("View sub counties"),
        CREATE_SUB_COUNTY("Create sub county"),
        UPDATE_SUB_COUNTY("Update sub county"),
        DELETE_SUB_COUNTY("Delete sub county"),
        VIEW_PICK_UP_LOCATIONS("View pick up locations"),
        CREATE_PICK_UP_LOCATION("Create pick up location"),
        UPDATE_PICK_UP_LOCATION("Update  pick up location"),
        DELETE_PICK_UP_LOCATION("Delete pick up location"),
        VIEW_FARMERS("View farmers"),
        CREATE_FARMER("Create farmer"),
        UPDATE_FARMER("Update farmer"),
        DELETE_FARMER("Delete farmer"),
        VIEW_FLOATS("View floats"),
        CREATE_FLOAT("Create float"),
        UPDATE_FLOAT("Update float"),
        DELETE_FLOAT("Delete float"),
        VIEW_USERS("View users"),
        CREATE_USER("Create user"),
        UPDATE_USER("Update user"),
        VIEW_SALES("View sales"),
        CREATE_SALE("Create sale"),
        VIEW_PRODUCTS_CONFIGURATIONS("View products configurations"),
        CREATE_PRODUCT_CONFIGURATION("Create product configuration"),
        UPDATE_PRODUCT_CONFIGURATION("Update product configuration"),
        DELETE_PRODUCT_CONFIGURATION("Delete product configuration"),
        UPDATE_SALE("Update sale"),
        DELETE_SALE("Delete sale"),
        VIEW_COLLECTIONS("View collections"),
        CREATE_COLLECTION("Create collection"),
        UPDATE_COLLECTION("Update  collection"),
        DELETE_COLLECTION("Delete collection"),
        ACTIVATE_USER("Activate user"),
        DEACTIVATE_USER("Deactivate user"),
        DELETE_USER("Update user"),
        ASSIGN_ROLE("Assign role"),
        VIEW_ROLES("View roles"),
        CREATE_ROLE("Create role"),
        UPDATE_ROLE("Update role"),
        DELETE_ROLE("Delete role");
        private final String name;
        AccessRight(String name) {
            this.name = name;
        }
    }

    public List<RoleAccessRights> accessRights() {
        return Arrays.stream(AccessRight.values())
                .map(s -> RoleAccessRights.builder().name(s.name).accessRights(s).build())
                .collect(Collectors.toList());
    }
    public List<AccessRight> getaccessRights() {
        return Arrays.stream(AccessRight.values()).collect(Collectors.toList());
    }


    public boolean createRole(@NonNull String name, @NonNull List<AccessRight> accessRights) {
        AtomicBoolean res = new AtomicBoolean();
        AtomicReference<Role> role = new AtomicReference<>(new Role());
        role.get().setName(name);
        role.get().setAccessRights(accessRights);
        role.get().setStatus(1);
        role.set(this.roleRepository.save(role.get()));
        log.log(Level.INFO, String.format("Role created [ %s ]", role.get()));
        res.set(true);

        return res.get();
    }

    public boolean updateRole(@NonNull Long id, @NonNull String name, @NonNull List<AccessRight> accessRights) {
        AtomicBoolean res = new AtomicBoolean();
        this.roleRepository.findById(id).ifPresentOrElse(r -> {
            AtomicReference<Role> role = new AtomicReference<>(r);
            role.get().setName(name);
            role.get().setAccessRights(accessRights);
            role.set(this.roleRepository.save(role.get()));
            log.log(Level.INFO, String.format("Role update [ %s ]", role.get()));

            res.set(true);
        }, () -> {
            if (this.createRole(name, accessRights)) {
                res.set(true);
            }
        });
        return res.get();
    }

    public boolean deactivateRole(@NonNull Long id) {
        AtomicBoolean res = new AtomicBoolean();

        this.roleRepository.findById(id).ifPresentOrElse(r -> {
            AtomicReference<Role> role = new AtomicReference<>(r);
            role.get().setStatus(0);
            role.set(this.roleRepository.save(role.get()));
            log.log(Level.INFO, String.format("Role update [ %s ]", role.get()));

            res.set(true);
        }, () -> {
           res.set(false);
        });
        return res.get();
    }

    public boolean activateRole(@NonNull Long id) {
        AtomicBoolean res = new AtomicBoolean();

        this.roleRepository.findById(id).ifPresentOrElse(r -> {
            AtomicReference<Role> role = new AtomicReference<>(r);
            role.get().setStatus(1);
            role.set(this.roleRepository.save(role.get()));
            log.log(Level.INFO, String.format("Role update [ %s ]", role.get()));

            res.set(true);
        }, () -> {
            log.log(Level.SEVERE, String.format("Role with the id [ %s ] not active ", id));

            res.set(false);
        });
        return res.get();
    }

    public RoleResponse fetchAllRoles(){
        AtomicReference<RoleResponse> response = new AtomicReference<>();

        List<Role> roles = roleRepository.findAll();

        List<RoleData> rolesData = new ArrayList<>();
        if(!roles.isEmpty()){
            roles.forEach(role -> {
                RoleData roleData = RoleData.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .creationDate(role.getCreationDate())
                        .updateDate(role.getUpdateDate())
                        .status(role.getStatus())
                        .build();

                List<RoleAccessRights> accessRights = new ArrayList<>();
                if(role.getAccessRights() != null && !role.getAccessRights().isEmpty()){
                    role.getAccessRights().forEach(accessRight -> {
                        accessRights.add(RoleAccessRights.builder().name(accessRight.name).accessRights(accessRight).build());
                    });
                }

                roleData.setAccessRights(accessRights);

                rolesData.add(roleData);

            });

            response.set(RoleResponse.builder().roleData(rolesData).build());
        }

        return response.get();
    }

    public RoleData fetchRoleById(@NonNull Long roleId){
        AtomicReference<RoleData> response = new AtomicReference<>();

        roleRepository.findById(roleId).ifPresentOrElse(role -> {
            RoleData roleData = RoleData.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .creationDate(role.getCreationDate())
                    .updateDate(role.getUpdateDate())
                    .status(role.getStatus())
                    .build();


            List<RoleAccessRights> accessRights = new ArrayList<>();
            if(role.getAccessRights() != null && !role.getAccessRights().isEmpty()){
                role.getAccessRights().forEach(accessRight -> {
                    accessRights.add(RoleAccessRights.builder().name(accessRight.name).accessRights(accessRight).build());
                });
            }

            roleData.setAccessRights(accessRights);

            response.set(roleData);

        }, () -> {
            log.log(Level.SEVERE, "Role not found ");

        });

        return response.get();
    }

    public RoleResponse fetchRolesByStatus(@NonNull Integer status){
        AtomicReference<RoleResponse> response = new AtomicReference<>();

        List<Role> roles = roleRepository.findAllByStatus(status);

        List<RoleData> rolesData = new ArrayList<>();
        if(roles != null && !roles.isEmpty()){
            roles.forEach(role -> {
                RoleData roleData = RoleData.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .creationDate(role.getCreationDate())
                        .updateDate(role.getUpdateDate())
                        .status(role.getStatus())
                        .build();

                List<RoleAccessRights> accessRights = new ArrayList<>();
                if(role.getAccessRights() != null && !role.getAccessRights().isEmpty()){
                    role.getAccessRights().forEach(accessRight -> {
                        accessRights.add(RoleAccessRights.builder().name(accessRight.name).accessRights(accessRight).build());
                    });
                }

                roleData.setAccessRights(accessRights);

                rolesData.add(roleData);
            });

            response.set(RoleResponse.builder().roleData(rolesData).build());
        }

        return response.get();
    }


}
