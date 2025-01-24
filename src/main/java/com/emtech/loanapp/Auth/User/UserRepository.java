package com.emtech.loanapp.Auth.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(@NonNull String username);

    Optional<User> findByEmail(@NonNull String e);

    Optional<User> findByMobile(@NonNull String mobile);

    List<User> findByStatus(@NonNull String status);

    @Query(value = "select count(*) from  users",nativeQuery = true)
    Integer countUsers();

    @Query(value = "select count(*) from  users where status != 'Active'",nativeQuery = true)
    Integer inactiveUsers();
    @Query(value = "SELECT  u.* FROM users u  JOIN user_role ur ON u.id = ur.user JOIN roles r ON ur.role = r.id WHERE   r.name =:roleName",nativeQuery = true)
    List<User> getUsersByRoleName(@NonNull String roleName);
    @Query(value = "SELECT r.name FROM users u JOIN user_role ur ON u.id=ur.user JOIN roles r ON r.id=ur.role WHERE u.id=:staffId",nativeQuery = true)
    String getUserRole(Long staffId);
}
