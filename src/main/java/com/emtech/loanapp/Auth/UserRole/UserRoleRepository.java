package com.emtech.loanapp.Auth.UserRole;

import com.emtech.loanapp.Auth.Role.Role;
import com.emtech.loanapp.Auth.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByUserAndRole(@NonNull User u, @NonNull Role r);

    Optional<UserRole> findByUser(@NonNull User u);

    List<UserRole> findAllByUser(@NonNull User u);

 List<UserRole> findAllByUserAndStatus(@NonNull User user,  Integer s);


}
