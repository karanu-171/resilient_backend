package com.emtech.loanapp.Config.Http;



import com.emtech.loanapp.Auth.Role.Role;
import com.emtech.loanapp.Auth.User.UserService;
import com.emtech.loanapp.Auth.Utilities.JWTUtil;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Log
public class AuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        log.log(Level.FINE, String.format("Http validate auth [ Principal=%s ]",  authentication.getPrincipal()));

        if (authentication.getPrincipal() != null) {
            log.log(Level.FINE, String.format("Http validate auth [ Principal=%s, ]",  authentication.getPrincipal()));

            String authToken = authentication.getPrincipal().toString();

            String username = jwtUtil.getUsernameFromToken(authToken);

            List<Role> roles = this.userService.validateUser(username);
            if (roles != null && !roles.isEmpty()) {
                return Mono.just(new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                        authentication.getCredentials(),
                        roles.stream().map(Role::getAccessRights)
                                .collect(Collectors.toList()).stream().flatMap(Collection::stream)
                                .collect(Collectors.toList()).stream().map(s -> new SimpleGrantedAuthority(s.name())).distinct()
                                .collect(Collectors.toList())));
            } else {
                return Mono.just(authentication);
            }
        } else {
            log.log(Level.WARNING, String.format("Http validate auth no authenticate [ %s ]", authentication));
            return Mono.just(new UsernamePasswordAuthenticationToken("", ""));
        }
    }
}
