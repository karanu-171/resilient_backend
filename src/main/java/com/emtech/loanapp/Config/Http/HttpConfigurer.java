package com.emtech.loanapp.Config.Http;


import com.emtech.loanapp.Auth.User.UserService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;


import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Log
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class HttpConfigurer {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Bean
    @Primary
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        String message = String.format("%s Unauthorized access denied", HttpStatus.UNAUTHORIZED.value());
        http.exceptionHandling()
                .authenticationEntryPoint((swe, e) -> {
                    swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    swe.getResponse().getHeaders().add("Content-Type", "application/json");

                    log.info(message);
                    return swe.getResponse().writeWith(Mono.just(swe.getResponse().bufferFactory().wrap(message.getBytes())));
                })
                .accessDeniedHandler((swe, e) -> {
                    swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    swe.getResponse().getHeaders().add("Content-Type", "application/json");

                    log.info(message);
                    return swe.getResponse().writeWith(Mono.just(swe.getResponse().bufferFactory().wrap(message.getBytes())));
                })
                .and()
                .cors()
                .and()
                .requestCache().requestCache(NoOpServerRequestCache.getInstance())
                .and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .httpBasic().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/swagger-*/**", "/v2/api-docs/**", "/v3/api-docs/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/**").permitAll()
                .pathMatchers(HttpMethod.PUT, "/api/v1/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/api/v1/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/admin/api/v1/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/admin/api/v1/**").permitAll()
                .pathMatchers(HttpMethod.PUT, "/admin/api/v1/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/admin/api/v1/**").permitAll()
                .anyExchange()
                .authenticated();
        return http.build();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowCredentials(true);

       configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173/")); //developing
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Origin: *","Access-Control-Allow-Credentials:  Origin, Content-Type, X-Auth-Token, Authorization, Accept")); // developing
        configuration.setAllowedHeaders(Arrays.asList("X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization")); //deploying
        configuration.setExposedHeaders(List.of("X-Get-Header"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost", "http://localhost:80","http://172.16.1.10:4012", "https://resilient-frontend-green.vercel.app", "http://197.155.71.138:4012"));

        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:4012", "http://172.16.1.10:4012", "https://resilient-frontend-green.vercel.app", "http://197.155.71.138:4012"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET","POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization"));
//        corsConfig.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Origin: *","Access-Control-Allow-Credentials:  Origin, Content-Type, X-Auth-Token"));
        corsConfig.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin: *","Access-Control-Allow-Credentials:  Origin, Content-Type, X-Auth-Token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }


}
