package com.emtech.loanapp.Auth.Utilities;

import com.emtech.loanapp.Auth.User.User;
import com.emtech.loanapp.Auth.User.UserRepository;
import com.emtech.loanapp.Configurations.Utils.exceptions.ResourceAbsentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {
    @Autowired
    private final UserRepository userRepository;
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if (userRepository.findByUsername(username).isPresent()) {
                User user;
                user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceAbsentException("user with name "+username+" not found"));
                return user;
            } else {
                throw new ResourceAbsentException("User not found");
            }
        };
    }
}
