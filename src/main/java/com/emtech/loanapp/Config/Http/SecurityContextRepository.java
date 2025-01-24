package com.emtech.loanapp.Config.Http;

import com.emtech.loanapp.Auth.Utilities.CurrentUserContext;
import com.emtech.loanapp.Auth.Utilities.JWTUtil;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@Component
@AllArgsConstructor
@Log
public class SecurityContextRepository implements ServerSecurityContextRepository {
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTUtil jwtUtil;

    private final UserDetailsService userDetailsService;
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        String username;
        ServerHttpRequest request = swe.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String authToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authToken = authHeader.substring(7);

            log.log(Level.WARNING, "Bearer token found and is valid.");
            username = jwtUtil.getUsernameFromToken(authToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            CurrentUserContext.setCurrentUserContext(userDetails);

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println("user retrieved, the user data is "+userDetails.getUsername());
        } else {
            log.log(Level.WARNING, "couldn't find bearer string, will ignore the header.");
        }
        if (authToken != null) {
            Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);

            return this.authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
        } else {
            return Mono.empty();
        }
    }
}
