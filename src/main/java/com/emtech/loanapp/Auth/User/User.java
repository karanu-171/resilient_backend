package com.emtech.loanapp.Auth.User;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "{id}")
@DynamicUpdate
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "user_id", columnNames = {"id"}),
        @UniqueConstraint(name = "user_email", columnNames = {"email"}),
        @UniqueConstraint(name = "User_mobile", columnNames = {"mobile"}),
        @UniqueConstraint(name = "user_name", columnNames = {"user_name"}),
})

public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_name")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name="email")
    private String email;

    private String pickUpLocation;

    @Column(name="mobile")
    private String mobile;

    @Column(name="password")
    private String password;

    @Column(name = "status")
    private String status;

    @CreationTimestamp
    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    @Column(name = "creation_date", nullable = false)
    private Timestamp creationDate;

    @Basic
    @UpdateTimestamp
    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    @Column(name = "update_date", nullable = false)
    private Timestamp updateDate;

    @Basic
    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    @Column(name = "delete_date")
    private Timestamp deletedDate;

    @Column(name = "login_status")
    private Integer isLoggedIn;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @JsonFormat(pattern = "dd-MMM-yyyy HH:mm:ss")
    @Column(name = "reset_password_token_expire")
    private Timestamp resetPasswordTokenExpire;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
