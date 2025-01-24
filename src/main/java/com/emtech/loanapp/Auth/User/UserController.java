package com.emtech.loanapp.Auth.User;

import com.emtech.loanapp.Auth.Data.Http.Request.Auth.UserCreateRequest;
import com.emtech.loanapp.Auth.Data.Http.Response.Auth.RecordCreateResponse;
import com.emtech.loanapp.Auth.Data.Http.Response.Auth.UserResponse;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Objects;


@Log
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/admin/api/v1/users")
public class UserController {
    @Autowired
    UserService userService;

//    @PreAuthorize(value = "hasAuthority('CREATE_USER')")
    @RequestMapping(
            path = "/create-user",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<RecordCreateResponse>> createUser(@RequestBody UserCreateRequest body){
        RecordCreateResponse response = this.userService.createUser(body.getUsername(), body.getFirstName(), body.getLastName(), body.getEmail(), body.getMobile(), body.getRole());

        if(!Objects.equals(response.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())){
            return Mono.just(ResponseEntity.status(response.getStatusCode()).body(response));
        }else {
            return Mono.just(ResponseEntity.internalServerError().body(response));
        }
    }


    @GetMapping("users-by-role-name")
    public Mono<ResponseEntity<UserResponse>> getUsersByRoleName(@NonNull String roleName){
        UserResponse users = this.userService.getUsersByRoleName(roleName);

        if(users != null){
            return Mono.just(ResponseEntity.ok().body(users));
        }else {
            return Mono.just(ResponseEntity.notFound().build());
        }
    }
}
