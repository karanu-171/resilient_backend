package com.emtech.loanapp;


import com.emtech.loanapp.Auth.Role.Role;
import com.emtech.loanapp.Auth.Role.RoleRepository;
import com.emtech.loanapp.Auth.Role.RoleService;
import com.emtech.loanapp.Auth.User.User;
import com.emtech.loanapp.Auth.User.UserRepository;
import com.emtech.loanapp.Auth.UserRole.UserRole;
import com.emtech.loanapp.Auth.UserRole.UserRoleRepository;
import com.emtech.loanapp.Auth.Utilities.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@SpringBootApplication()
@Slf4j
public class LoanAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanAppApplication.class, args);
        Date currentDate = Calendar.getInstance(TimeZone.getDefault()).getTime();
        log.info("Current date = "+ currentDate);

        log.info("Up and Running ...");
    }
    @Component
    public class AdminData implements CommandLineRunner {

        @Autowired
        private UserRepository repository;
        @Autowired
        private UserRoleRepository userRoleRepository;


        @Autowired
        private RoleRepository roleRepository;
        @Autowired
        private RoleService roleService;

        @Autowired
        private PasswordUtil passwordUtil;


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        Date modified_on =new Date();
        SimpleDateFormat formatter
                = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = new Date();

        // getting the object of the Timestamp class
        Timestamp ts = new Timestamp(date.getTime());

        //Add Roles (ROLE_ADMIN and ROLE_USER)
        void addAdminRole() {
            log.info("creating admin role ...");

            Role role = new Role();
            role.setName("ROLE_ADMIN");
            role.setAccessRights(roleService.getaccessRights());
            role.setStatus(1);
//          role.setCreationDate(Timestamp.valueOf(formatter.format(ts)));

            roleRepository.save(role);
            log.info("Admin role created.");
        }

        //Default admin records
        void addAdmin() {
            log.info("creating admin user...");
            User user = new User();
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(adminRole);
            user.setFirstName("John");
            user.setLastName("Karanu");
            user.setUsername("Karanu");
            user.setEmail("karanujohn171@gmail.com");
            user.setMobile("0702477708");
            user.setIsLoggedIn(0);
            user.setStatus("Active");
            user.setPassword(passwordUtil.encode("1234"));
            repository.save(user);

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setStatus(1);
            userRole.setRole(adminRole);

            userRoleRepository.save(userRole);
            log.info("admin user created.");
        }

        @Override
        public void run(String... args) throws Exception {
            int countusers = repository.countUsers();
            int countroles = roleRepository.countroles();

            if (countroles < 1) {
                addAdminRole();
            }

            if (countusers < 1) {
                addAdmin();
            }

        }
    }
}
