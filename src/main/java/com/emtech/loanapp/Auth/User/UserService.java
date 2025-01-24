package com.emtech.loanapp.Auth.User;

import com.emtech.loanapp.Auth.Data.Http.Response.Auth.AuthResponse;
import com.emtech.loanapp.Auth.Data.Http.Response.Auth.RecordCreateResponse;
import com.emtech.loanapp.Auth.Data.Http.Response.Auth.UserResponse;
import com.emtech.loanapp.Auth.Data.Role.RoleAccessRights;
import com.emtech.loanapp.Auth.Data.User.UserData;
import com.emtech.loanapp.Auth.Data.User.UserRoleData;
import com.emtech.loanapp.Auth.Role.Role;
import com.emtech.loanapp.Auth.Role.RoleRepository;
import com.emtech.loanapp.Auth.UserRole.UserRole;
import com.emtech.loanapp.Auth.UserRole.UserRoleRepository;
import com.emtech.loanapp.Auth.Utilities.JWTUtil;
import com.emtech.loanapp.Auth.Utilities.PasswordUtil;
import com.emtech.loanapp.Auth.Utilities.SendCredentialToMail;
import com.emtech.loanapp.Auth.Utilities.ToolKit;
import com.emtech.loanapp.Response.EntityResponse;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Value("${jwt.password.token.expirationMs}")
    private String resetPasswordTokenExpiration;

    EntityResponse res= new EntityResponse<>();

    public List<Role> validateUser(@NonNull String username) {
        List<Role> roles = new ArrayList<>();

        this.userRepository.findByUsername(username.trim()).ifPresent(user -> {
            if (Objects.equals(user.getStatus(), "Active")) {
                roles.addAll(this.userRoles(user, true));
            }
        });

        return roles;
    }

    public List<Role> userRoles(@NonNull User user, boolean activeOnly) {
        if (activeOnly) {
            return this.userRoleRepository.findAllByUser(user).stream().map(UserRole::getRole).collect(Collectors.toList());
        } else {
            return this.userRoleRepository.findAllByUserAndStatus(user, 1).stream().map(UserRole::getRole).collect(Collectors.toList());
        }
    }

    public RecordCreateResponse createUser(@NonNull String userName, @NonNull String firstName, @NonNull String lastName, @NonNull String email, @NonNull String mobile, @NonNull Long roleId){
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        this.userRepository.findByUsername(userName.trim()).ifPresentOrElse(user -> {
            log.log(Level.SEVERE, String.format("An account with the username %s already exists !", userName));

            response.set(RecordCreateResponse.builder().message(String.format("An account with the username %s already exists !", userName)).statusCode(HttpStatus.BAD_REQUEST.value()).build());

        }, () -> {
            this.userRepository.findByEmail(email).ifPresentOrElse(user -> {

                log.log(Level.SEVERE, String.format("An account with the email %s already exists !", email));

                response.set(RecordCreateResponse.builder().message( String.format("An account with the email %s already exists !", email)).statusCode(HttpStatus.BAD_REQUEST.value()).build());

            }, () -> {
                this.userRepository.findByMobile(mobile).ifPresentOrElse(user -> {
                    log.log(Level.SEVERE, String.format("An account with the mobile %s  already exist !", mobile));

                    response.set(RecordCreateResponse.builder().message( String.format("An account with the mobile %s  already exist !", mobile)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
                }, () -> {
                    this.roleRepository.findById(roleId).ifPresentOrElse(role -> {
                        if(role.getStatus().compareTo(1) == 0){
                            AtomicReference<User> user = new AtomicReference<>(new User());
                            user.get().setUsername(userName.trim());
                            user.get().setFirstName(firstName);
                            user.get().setLastName(lastName.trim());
                            user.get().setEmail(email.trim());
                            user.get().setMobile(mobile);
                            user.get().setStatus("Active");
                            user.get().setIsLoggedIn(0);

                            ToolKit tk = new ToolKit();

                            String userPassword = tk.generatePassword();

                            user.get().setPassword(passwordUtil.encode(userPassword));

                            user.set(this.userRepository.save(user.get()));

                            log.log(Level.INFO, String.format("User created [ %s ]", user.get()));

                            log.log(Level.INFO, String.format("Role Details [ %s ]", role));

                            if (this.assignRole(user.get(), role, true)) {
                                log.log(Level.INFO, String.format("User assigned role [ %s ]", user.get()));
                            }

//                            try {
//                                SendCredentialToMail sm = new SendCredentialToMail();
//
//                                log.log(Level.INFO, String.format("User Email [ %s ]", user.get().getEmail()));
//
//                                 res= sm.sendMail(user.get().getEmail(), user.get().getUsername(), userPassword);
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            if(res.getStatusCode()==200) {
//                                response.set(RecordCreateResponse.builder().message("User created successfully !").statusCode(HttpStatus.CREATED.value()).build());
//                            }
                            response.set(RecordCreateResponse.builder().message("User created successfully !").statusCode(HttpStatus.CREATED.value()).build());
                        }else {
                            log.log(Level.SEVERE, String.format("Selected role with the id %s is not active !", roleId));

                            response.set(RecordCreateResponse.builder().message(String.format("Selected role with the id %s is not active !", roleId)).statusCode(HttpStatus.BAD_REQUEST.value()).build());

                        }
                    }, () -> {
                        log.log(Level.SEVERE, String.format("Role with the id %s not found !", roleId));

                        response.set(RecordCreateResponse.builder().message(String.format("Role with the id %s not found !", roleId)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
                    });
                });
            });
        });

        return response.get();
    }

    public EntityResponse<?> authenticateUser(@NonNull String username, @NonNull String password){
        EntityResponse<Object> response = new EntityResponse<>();
        AtomicReference<AuthResponse> authResponse = new AtomicReference<>();
        userRepository.findByUsername(username.trim()).ifPresentOrElse(user -> {
            if (Objects.equals(user.getStatus(), "Active")){

//                log.log(Level.INFO, String.format("Encoded Password: [credentials=%s] User Password: [ password=%s ]", passwordUtil.encode(password.trim()), user.getPassword()));
                if(passwordUtil.matches(password.trim(), user.getPassword())){
//                    log.log(Level.INFO, "Inside password encryption]");
                    UserData userData = getUserDetails(user.getId());

//                    log.log(Level.INFO, String.format("User Data Details [ %s ]", userData.toString()));

                    String token = jwtUtil.generateToken(userData);
                    AuthResponse authResp = AuthResponse.builder()
                            .token(token)
                            .id(userData.getId())
                            .username(userData.getUsername())
                            .mobile(userData.getMobile())
                            .roles(userData.getRoles())
                            .build();

                    authResponse.set(authResp);

                    response.setMessage("Login Successful");
                    response.setStatusCode(HttpStatus.OK.value());
                    response.setEntity(authResponse);
                }else{

                    log.log(Level.SEVERE, "Passwords do not match");
                    response.setMessage("Wrong Password");
                    response.setStatusCode(HttpStatus.BAD_REQUEST.value());
                }

            }else{

                log.log(Level.WARNING, String.format("Account for the provided username is not active [ username=%s ]", username));
                response.setMessage("Account with username "+username+" not active");
                response.setStatusCode(HttpStatus.UNAUTHORIZED.value());

            }
        }, () -> {
            log.log(Level.WARNING, "User with the username not found");

            response.setMessage("User account not found");
            response.setStatusCode(HttpStatus.NOT_FOUND.value());

        });
        return response;
    }

    public RecordCreateResponse updateUser(@NonNull Long userId, @NonNull String email, @NonNull String phone){
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        this.userRepository.findById(userId).ifPresentOrElse(userData -> {
            AtomicReference<User> user = new AtomicReference<>(userData);
            user.get().setEmail(email);
            user.get().setMobile(phone);
            user.set(this.userRepository.save(user.get()));

            response.set(RecordCreateResponse.builder().message("User details updated successfully !").statusCode(HttpStatus.OK.value()).build());
        }, () -> {
            log.log(Level.SEVERE, "User not found ");
            response.set(RecordCreateResponse.builder().message("User not found !").statusCode(HttpStatus.BAD_REQUEST.value()).build());

        });

        return response.get();
    }

    public RecordCreateResponse updateUserStatus(@NonNull Long userId, @NonNull String status){
//        AtomicBoolean res = new AtomicBoolean();
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        this.userRepository.findById(userId).ifPresentOrElse(userData -> {
            AtomicReference<User> user = new AtomicReference<>(userData);
            user.get().setStatus(status);

            user.set(this.userRepository.save(user.get()));

            response.set(RecordCreateResponse.builder().message("User status updated successfully !").statusCode(HttpStatus.OK.value()).build());
        }, () -> {
            /* todo:: User not found  */
            log.log(Level.SEVERE, "User not found");

            response.set(RecordCreateResponse.builder().message("User not found").statusCode(HttpStatus.BAD_REQUEST.value()).build());
//            res.set(false);
        });

        return response.get();
    }

    public RecordCreateResponse adminUpdateUserPassword(@NonNull String username, @NonNull String password){
//        AtomicBoolean res = new AtomicBoolean();
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        this.userRepository.findByUsername(username).ifPresentOrElse(userData -> {

            if(Objects.equals(userData.getStatus(), "Active")){
                AtomicReference<User> user = new AtomicReference<>(userData);

                user.get().setPassword(passwordUtil.encode(password));

                user.set(this.userRepository.save(user.get()));

                try {
                    SendCredentialToMail sm = new SendCredentialToMail();

//                    log.log(Level.INFO, String.format("User Email [ %s ]", user.get().getEmail()));

//                    sm.sendMail(user.get().getEmail(), user.get().getUsername(), password);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.set(RecordCreateResponse.builder().message("User password updated successfully !").statusCode(HttpStatus.OK.value()).build());
//                res.set(true);
            }else {
                log.log(Level.SEVERE, String.format("Account with the username %s is not active ", username));

                response.set(RecordCreateResponse.builder().message(String.format("Account with the username %s is not active ", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
//                res.set(false);
            }
        }, () -> {
            /* todo:: User not found  */

            log.log(Level.SEVERE, String.format("User with the username %s  not found", username));

            response.set(RecordCreateResponse.builder().message(String.format("User with the username %s  not found", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
        });

        return response.get();
    }

    public RecordCreateResponse updateUserPassword(@NonNull String username, @NonNull String previousPassword, @NonNull String password){
//        AtomicBoolean res = new AtomicBoolean();
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        this.userRepository.findByUsername(username).ifPresentOrElse(userData -> {

            if(Objects.equals(userData.getStatus(), "Active")){
                if(passwordUtil.matches(previousPassword.trim(), userData.getPassword())){
                    AtomicReference<User> user = new AtomicReference<>(userData);

                    user.get().setPassword(passwordUtil.encode(password));

                    user.set(this.userRepository.save(user.get()));

                    try {
                        SendCredentialToMail sm = new SendCredentialToMail();

                        log.log(Level.INFO, String.format("User Email [ %s ]", user.get().getEmail()));

                        sm.sendMail(user.get().getEmail(), user.get().getUsername(), password);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    response.set(RecordCreateResponse.builder().message("Password updated successfully !").statusCode(HttpStatus.OK.value()).build());
                }else {
                    log.log(Level.SEVERE, "The previous  password you provided is incorrect !");

                    response.set(RecordCreateResponse.builder().message("The previous  password you provided is incorrect !").statusCode(HttpStatus.BAD_REQUEST.value()).build());
                }

            }else {
                log.log(Level.SEVERE, String.format("Account with the username %s is not active ", username));

                response.set(RecordCreateResponse.builder().message(String.format("Account with the username %s is not active ", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());

//                res.set(false);
            }
        }, () -> {
            /* todo:: User not found  */

            log.log(Level.SEVERE, String.format("Account with the username %s not found", username));

//            res.set(false);
            response.set(RecordCreateResponse.builder().message(String.format("Account with the username %s not found ", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
        });

        return response.get();
    }

    public boolean logoutUser(@NonNull Long userId, @NonNull Integer status){
        AtomicBoolean res = new AtomicBoolean();


        this.userRepository.findById(userId).ifPresentOrElse(userData -> {
            AtomicReference<User> user = new AtomicReference<>(userData);
            user.get().setIsLoggedIn(status);

            user.set(this.userRepository.save(user.get()));

            res.set(true);
        }, () -> {
            log.log(Level.SEVERE, String.format("User with the userid [ %s ] not found", userId));

            res.set(false);
        });

        return res.get();
    }


    public boolean assignRole(@NonNull User user, @NonNull Role role, boolean activate){
        AtomicBoolean res = new AtomicBoolean();

        this.userRepository.findById(user.getId()).ifPresentOrElse(userData -> {
            if(Objects.equals(userData.getStatus(), "Active")){
                this.roleRepository.findById(role.getId()).ifPresentOrElse(myRole -> {
                    if(myRole.getStatus().compareTo(1) == 0){
                        userRoleRepository.findByUserAndRole(userData, myRole).ifPresentOrElse(ur -> {
                            AtomicReference<UserRole> userRole = new AtomicReference<>(ur);
                            if (activate) {
                                userRole.get().setStatus(1); /* 1 - activate */
                            } else {
                                userRole.get().setStatus(2); /* 2 - disabled */
                            }

                            userRole.set(this.userRoleRepository.save(userRole.get()));
                            log.log(Level.INFO, String.format("User role deactivated [ %s ]", userRole.get()));
                            res.set(true);
                        }, () -> {
                            if (activate) {
                                AtomicReference<UserRole> userRole = new AtomicReference<>(new UserRole());
                                userRole.get().setUser(userData);
                                userRole.get().setRole(myRole);
                                userRole.get().setStatus(1);
                                userRole.set(this.userRoleRepository.save(userRole.get()));
                                log.log(Level.INFO, String.format("User role created [ %s ]", userRole.get()));

                                res.set(true);
                            }
                        });
                    }else {
                        log.log(Level.SEVERE, "Role is not active");

                        res.set(false);
                    }
                }, () -> {
                    log.log(Level.SEVERE, "Role not found");

                    res.set(false);
                });
            }else{
                log.log(Level.SEVERE, "USer is not active");

                res.set(false);
            }
        }, () -> {
            log.log(Level.SEVERE, "USer is not found");

            res.set(false);
        });

        return res.get();
    }

    public RecordCreateResponse updateUserRole(@NonNull String username, @NonNull Long roleId){
//        AtomicBoolean res = new AtomicBoolean();
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        this.userRepository.findByUsername(username).ifPresentOrElse(userData -> {
            if(Objects.equals(userData.getStatus(), "Active")){
                this.roleRepository.findById(roleId).ifPresentOrElse(myRole -> {
                    if(myRole.getStatus().compareTo(1) == 0){
                        userRoleRepository.findByUser(userData).ifPresentOrElse(ur -> {
                            AtomicReference<UserRole> userRole = new AtomicReference<>(ur);
                            userRole.get().setRole(myRole);

                            userRole.set(this.userRoleRepository.save(userRole.get()));

                            response.set(RecordCreateResponse.builder().message("User role updated successfully !").statusCode(HttpStatus.OK.value()).build());
                        }, () -> {

                        });
                    }else {
                        log.log(Level.SEVERE, "Role is not active");

                        response.set(RecordCreateResponse.builder().message("Selected role is not active !").statusCode(HttpStatus.BAD_REQUEST.value()).build());
//                        res.set(false);
                    }
                }, () -> {
                    log.log(Level.SEVERE, "Role not found");

                    response.set(RecordCreateResponse.builder().message("Role not found !").statusCode(HttpStatus.BAD_REQUEST.value()).build());

//                    res.set(false);
                });
            }else{
                log.log(Level.SEVERE, "User is not active");

                response.set(RecordCreateResponse.builder().message("User is not active !").statusCode(HttpStatus.BAD_REQUEST.value()).build());

//                res.set(false);
            }
        }, () -> {
            log.log(Level.SEVERE, "User not found");

            response.set(RecordCreateResponse.builder().message("User not found !").statusCode(HttpStatus.BAD_REQUEST.value()).build());

//            res.set(false);
        });

        return response.get();
    }

    public UserResponse getAllUsers(){
        AtomicReference<UserResponse> response = new AtomicReference<>();

        List<UserData> usersResponse = new ArrayList<>();
        List<User> users = this.userRepository.findAll();

        if(users != null && !users.isEmpty()){
            users.forEach(user -> {
                UserData userData = UserData.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .status(user.getStatus())
                        .pickUpLocation(user.getPickUpLocation())
                        .creationDate(user.getCreationDate())
                        .updateDate(user.getUpdateDate())
                        .isLoggedIn(user.getIsLoggedIn())
                        .build();

                List<UserRoleData> roles = new ArrayList<>();

                List<UserRole> userRoles = this.userRoleRepository.findAllByUser(user);
                if(userRoles != null && !userRoles.isEmpty()){
                    userRoles.forEach(userRole -> {
                        UserRoleData userRoleData = UserRoleData.builder()
                                .name(userRole.getRole().getName())
                                .build();

                        List<RoleAccessRights> accessRights = new ArrayList<>();
                        if(userRole.getRole().getStatus() !=  null && !userRole.getRole().getAccessRights().isEmpty()){
                            userRole.getRole().getAccessRights().forEach(accessRight -> {
                                accessRights.add(RoleAccessRights.builder().name(accessRight.getName()).accessRights(accessRight).build());
                            });
                        }

                        userRoleData.setAccessRights(accessRights);

                        roles.add(userRoleData);

                        userData.setRoles(roles);
                    });
                }

                usersResponse.add(userData);
            });

            response.set(UserResponse.builder().userData(usersResponse).build());
        }

        return response.get();
    }

    public UserResponse getUsersByRoleName(@NonNull String roleName){
//        EntityResponse response = new EntityResponse();
        AtomicReference<UserResponse> response = new AtomicReference<>();

        List<UserData> usersResponse = new ArrayList<>();
        List<User> users = this.userRepository.getUsersByRoleName(roleName);

        if(users != null && !users.isEmpty()){
            users.forEach(user -> {
                UserData userData = UserData.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .status(user.getStatus())
                        .creationDate(user.getCreationDate())
                        .updateDate(user.getUpdateDate())
                        .isLoggedIn(user.getIsLoggedIn())
                        .build();

                List<UserRoleData> roles = new ArrayList<>();

                List<UserRole> userRoles = this.userRoleRepository.findAllByUser(user);
                if(userRoles != null && !userRoles.isEmpty()){
                    userRoles.forEach(userRole -> {
                        UserRoleData userRoleData = UserRoleData.builder()
                                .name(userRole.getRole().getName())
                                .build();

                        List<RoleAccessRights> accessRights = new ArrayList<>();
                        if(userRole.getRole().getStatus() !=  null && !userRole.getRole().getAccessRights().isEmpty()){
                            userRole.getRole().getAccessRights().forEach(accessRight -> {
                                accessRights.add(RoleAccessRights.builder().name(accessRight.getName()).accessRights(accessRight).build());
                            });
                        }

                        userRoleData.setAccessRights(accessRights);

                        roles.add(userRoleData);

                        userData.setRoles(roles);
                    });
                }

                usersResponse.add(userData);
            });

            response.set(UserResponse.builder().userData(usersResponse).build());
        }
        return response.get();
    }
    public UserData getUserDetails(@NonNull Long userId){
        AtomicReference<UserData> response = new AtomicReference<>();

        this.userRepository.findById(userId).ifPresentOrElse(user -> {
            UserData data = UserData.builder()
                    .id(userId)
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .pickUpLocation(user.getPickUpLocation())
                    .mobile(user.getMobile())
                    .status(user.getStatus())
                    .creationDate(user.getCreationDate())
                    .updateDate(user.getUpdateDate())
                    .isLoggedIn(user.getIsLoggedIn())
                    .build();

            List<UserRoleData> roles = new ArrayList<>();

            List<UserRole> userRoles = this.userRoleRepository.findAllByUser(user);

            if(userRoles != null && !userRoles.isEmpty()){
                userRoles.forEach(userRole -> {
                    UserRoleData userRoleData = UserRoleData.builder()
                            .name(userRole.getRole().getName())
                            .build();

                    List<RoleAccessRights> accessRights = new ArrayList<>();
                    if(userRole.getRole().getStatus() !=  null && !userRole.getRole().getAccessRights().isEmpty()){
                        userRole.getRole().getAccessRights().forEach(accessRight -> {
                            accessRights.add(RoleAccessRights.builder().name(accessRight.getName()).accessRights(accessRight).build());
                        });
                    }

                    userRoleData.setAccessRights(accessRights);

                    roles.add(userRoleData);

                    data.setRoles(roles);
                });
            }

            response.set(data);
        }, () -> {
            log.log(Level.SEVERE, "User");
        });

        return response.get();
    }

    public UserResponse getUsersByStatus(@NonNull String status){
        AtomicReference<UserResponse> response = new AtomicReference<>();

        List<UserData> usersResponse = new ArrayList<>();
        List<User> users = this.userRepository.findByStatus(status);

        if(users != null && !users.isEmpty()){
            users.forEach(user -> {
                UserData userData = UserData.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .status(user.getStatus())
                        .creationDate(user.getCreationDate())
                        .updateDate(user.getUpdateDate())
                        .isLoggedIn(user.getIsLoggedIn())
                        .build();

                List<UserRoleData> roles = new ArrayList<>();

                List<UserRole> userRoles = this.userRoleRepository.findAllByUser(user);
                if(userRoles != null && !userRoles.isEmpty()){
                    userRoles.forEach(userRole -> {
                        UserRoleData userRoleData = UserRoleData.builder()
                                .name(userRole.getRole().getName())
                                .build();

                        List<RoleAccessRights> accessRights = new ArrayList<>();
                        if(userRole.getRole().getStatus() !=  null && !userRole.getRole().getAccessRights().isEmpty()){
                            userRole.getRole().getAccessRights().forEach(accessRight -> {
                                accessRights.add(RoleAccessRights.builder().name(accessRight.getName()).accessRights(accessRight).build());
                            });
                        }

                        userRoleData.setAccessRights(accessRights);

                        roles.add(userRoleData);

                        userData.setRoles(roles);
                    });
                }

                usersResponse.add(userData);
            });

            response.set(UserResponse.builder().userData(usersResponse).build());
        }

        return response.get();
    }

    public RecordCreateResponse forgotPassword(String username){
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        userRepository.findByUsername(username).ifPresentOrElse(user -> {
            if (Objects.equals(user.getStatus(), "Active")){
                UserData userData =  getUserDetails(user.getId());
                String resetPasswordToken = jwtUtil.generateToken(userData);
                AtomicReference<User> data = new AtomicReference<>(user);
                Instant tokenExpirationTime = Instant.now().plusMillis(Long.parseLong(resetPasswordTokenExpiration));

                data.get().setResetPasswordToken(resetPasswordToken);
                data.get().setResetPasswordTokenExpire(Timestamp.from(tokenExpirationTime));

                data.set(this.userRepository.save(data.get()));

                String resetPasswordUrl = "http://localhost:4200/#/authentication/reset-password/" + resetPasswordToken;

                try {
                    SendCredentialToMail sm = new SendCredentialToMail();

                    log.log(Level.INFO, String.format("User Email [ %s ]", data.get().getEmail()));

                    sm.sendPassWordReset(data.get().getEmail(), resetPasswordUrl);

                    log.log(Level.INFO, String.format("Reset Password URL: %s ", resetPasswordUrl));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.set(RecordCreateResponse.builder().message("Password reset token requested successfully !").statusCode(HttpStatus.OK.value()).build());

            }else{
                log.log(Level.SEVERE, String.format("Account with username [ %s ] not found ", username));

                response.set(RecordCreateResponse.builder().message( String.format("Account with username  %s  not found ", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
            }
        }, () -> {
            log.log(Level.SEVERE, String.format("Account with username [ %s ] not active ", username));

            response.set(RecordCreateResponse.builder().message( String.format("Account with username %s not active  ", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
        });

        return response.get();
    }

    public RecordCreateResponse resetPasswordTokenRequestedByEmail(@NonNull String email){
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            if (Objects.equals(user.getStatus(), "Active")){
                UserData userData =  getUserDetails(user.getId());
                String resetPasswordToken = jwtUtil.generateToken(userData);
                AtomicReference<User> data = new AtomicReference<>(user);
                Instant tokenExpirationTime = Instant.now().plusMillis(Long.parseLong(resetPasswordTokenExpiration));

                data.get().setResetPasswordToken(resetPasswordToken);
                data.get().setResetPasswordTokenExpire(Timestamp.from(tokenExpirationTime));

                data.set(this.userRepository.save(data.get()));

                String resetPasswordUrl = "http://localhost:4200/#/authentication/reset-password/" + resetPasswordToken;

                log.log(Level.INFO, String.format("Reset Password URL: %s ", resetPasswordUrl));

                try {
                    SendCredentialToMail sm = new SendCredentialToMail();

                    log.log(Level.INFO, String.format("User Email [ %s ]", data.get().getEmail()));

                    sm.sendPassWordReset(data.get().getEmail(), resetPasswordUrl);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.set(RecordCreateResponse.builder().message("Password reset token requested successfully !").statusCode(HttpStatus.OK.value()).build());

            }else{
                log.log(Level.SEVERE, String.format("Account with email [ %s ] not found ", email));

                response.set(RecordCreateResponse.builder().message( String.format("Account with email %s not found ", email)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
            }
        }, () -> {
            log.log(Level.SEVERE, String.format("Account with email [ %s ] is not active ", email));

            response.set(RecordCreateResponse.builder().message( String.format("Account with email %s is not active ", email)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
        });

        return response.get();
    }

    public RecordCreateResponse resetPasswordTokenRequestedByMobile(@NonNull String mobile){
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        userRepository.findByMobile(mobile).ifPresentOrElse(user -> {
            if (Objects.equals(user.getStatus(), "Active")){
                UserData userData =  getUserDetails(user.getId());
                String resetPasswordToken = jwtUtil.generateToken(userData);
                AtomicReference<User> data = new AtomicReference<>(user);
                Instant tokenExpirationTime = Instant.now().plusMillis(Long.parseLong(resetPasswordTokenExpiration));

                data.get().setResetPasswordToken(resetPasswordToken);
                data.get().setResetPasswordTokenExpire(Timestamp.from(tokenExpirationTime));

                data.set(this.userRepository.save(data.get()));

                String resetPasswordUrl = "http://localhost:4200/#/authentication/reset-password/" + resetPasswordToken;

                log.log(Level.INFO, String.format("Reset Password URL: %s ", resetPasswordUrl));

                try {
                    SendCredentialToMail sm = new SendCredentialToMail();

                    log.log(Level.INFO, String.format("User Email [ %s ]", data.get().getEmail()));

                    sm.sendPassWordReset(data.get().getEmail(), resetPasswordUrl);

                } catch (Exception e) {
                    log.log(Level.SEVERE, e.toString());
                }

                response.set(RecordCreateResponse.builder().message("Password reset token requested successfully !").statusCode(HttpStatus.OK.value()).build());

            }else{
                log.log(Level.SEVERE, String.format("Account with mobile [ %s ] not found ", mobile));

                response.set(RecordCreateResponse.builder().message( String.format("Account with mobile %s not found ", mobile)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
            }
        }, () -> {
            log.log(Level.SEVERE, String.format("Account with mobile [ %s ] not active ", mobile));

            response.set(RecordCreateResponse.builder().message( String.format("Account with mobile %s not active ", mobile)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
        });

        return response.get();
    }


    public RecordCreateResponse resetPassword(@NonNull String resetPasswordToken, @NonNull String password){
        AtomicReference<RecordCreateResponse> response = new AtomicReference<>();

        String username = jwtUtil.getUsernameFromToken(resetPasswordToken);

        if(username != null && !username.isEmpty()){
            userRepository.findByUsername(username).ifPresentOrElse(user -> {
                if (Objects.equals(user.getStatus(), "Active")){
                    AtomicReference<User> data = new AtomicReference<>(user);
                    if(data.get().getResetPasswordToken() != null && !data.get().getResetPasswordToken().isEmpty()){
                        try {
                            LocalDateTime tokenExpiryTime = convertTimestampToLocalDateTime(user.getResetPasswordTokenExpire(), "dd-MMM-yyyy HH:mm:ss");

                            log.log(Level.INFO, String.format("Compare Password Reset Token Time  To Current Time [ %s ]", LocalDateTime.now().isAfter(tokenExpiryTime)));
                            if(LocalDateTime.now().isAfter(tokenExpiryTime)){
                                data.get().setResetPasswordToken(null);
                                data.get().setResetPasswordTokenExpire(null);

                                log.log(Level.SEVERE, String.format("Reset password token has expired %s", username));

                                response.set(RecordCreateResponse.builder().message("Reset password token has expired, please request for a new token").statusCode(HttpStatus.BAD_REQUEST.value()).build());
                            }else{
                                data.get().setPassword( passwordUtil.encode(password));
                                data.get().setResetPasswordToken(null);
                                data.get().setResetPasswordTokenExpire(null);

                                data.set(userRepository.save(data.get()));

                                response.set(RecordCreateResponse.builder().message("Password changed successfully !").statusCode((HttpStatus.OK.value())).build());
                            }
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        response.set(RecordCreateResponse.builder().message("Token has already been used, please request for a new one").statusCode(HttpStatus.BAD_REQUEST.value()).build());
                    }
                }else{
                    log.log(Level.SEVERE, String.format("Account with username [ %s ] not active ", username));

                    response.set(RecordCreateResponse.builder().message(String.format("Account with username %s not active ", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());
                }
            }, () -> {
                /* todo:: user not found  */

                log.log(Level.SEVERE, String.format("Account with username [ %s ] not found ", username));

                response.set(RecordCreateResponse.builder().message(String.format("Account with username %s not active ", username)).statusCode(HttpStatus.BAD_REQUEST.value()).build());


            });
        }
        return response.get();
    }

    public static LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        log.log(Level.INFO, String.format("Token Expiry Time [ Time=%s]", timestamp));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        String timestampAsString = formatter.format(timestamp.toInstant());
        System.out.println(timestampAsString);

        Date date = sdf.parse(timestampAsString);
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    public EntityResponse<?> updatePassword(Long id, String currentPass, String newPass) {
        EntityResponse<String> response = new EntityResponse<>();

        try {
            if (id == null || currentPass == null || currentPass.isEmpty() || newPass == null || newPass.isEmpty()) {
                response.setMessage("id, current and new password required");
                response.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return response;
            }

            Optional<User> optional = userRepository.findById(id);

            if (optional.isEmpty()) {
                response.setMessage("user with id "+id+" not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                return response;
            }

            User user = optional.get();

            if (!user.getPassword().isEmpty()) {
                if (!user.getPassword().equals(passwordUtil.encode(currentPass))) {
                    response.setMessage("current password is wrong");
                    response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                    return response;
                }
            }

            user.setPassword(passwordUtil.encode(newPass));
            userRepository.save(user);

            response.setMessage("successful");
            response.setStatusCode(HttpStatus.OK.value());
            response.setEntity("password updated successfully");
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());

            response.setMessage("an error occurred");
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        }
        return response;
    }

}
