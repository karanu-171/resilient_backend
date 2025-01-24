package com.emtech.loanapp.Auth.Utilities;

import com.emtech.loanapp.Auth.User.User;
import com.emtech.loanapp.Auth.User.UserRepository;
import com.emtech.loanapp.Auth.UserRole.UserRole;
import com.emtech.loanapp.Auth.UserRole.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserInfo {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepo;

    @Autowired
    public UserInfo(UserRepository userRepository, UserRoleRepository userRoleRepo) {
        this.userRepository = userRepository;
        this.userRoleRepo = userRoleRepo;
    }

    public static String username() {
        String username = "";
        if (CurrentUserContext.getCurrentUserContext() != null) {
            return CurrentUserContext.getCurrentUserContext().getUsername();
        } else {
            return username;
        }
    }

    public String userRole() {
        Optional<User> optionalUser = userRepository.findByUsername(UserInfo.username());
        if (optionalUser.isEmpty()) {
            return "";
        }

        User user = optionalUser.get();

        Optional<UserRole> optional = userRoleRepo.findByUser(user);

        if (optional.isEmpty()) {
            return "";
        }

        return optional.get().getRole().getName();
    }

    public static User user() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            System.out.println("more details "+authentication.getPrincipal());
            if (authentication.getPrincipal() instanceof User user) {
                return user;
            }
        }
        return null;
    }
}
