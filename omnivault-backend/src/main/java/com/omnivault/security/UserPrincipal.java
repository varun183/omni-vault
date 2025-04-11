package com.omnivault.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.omnivault.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserDetails implementation for Spring Security authentication.
 * Adapts the application's User model to Spring Security's UserDetails interface,
 * providing user authentication and authorization information.
 */
@Data
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private String firstName;
    private String lastName;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates a UserPrincipal from a User entity.
     * Converts a domain User object into a UserPrincipal with default USER role.
     *
     * @param user The domain User object to convert
     * @return A UserPrincipal instance representing the user
     */
    public static UserPrincipal create(User user) {
        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    /**
     * Returns the user's authorities (roles/permissions).
     *
     * @return Collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the user's password for authentication.
     *
     * @return The user's password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used for authentication.
     *
     * @return The username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has not expired.
     *
     * @return true, as account expiration is not implemented
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is not locked.
     *
     * @return true, as account locking is not implemented
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials have not expired.
     *
     * @return true, as credential expiration is not implemented
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is enabled.
     *
     * @return true, as account is considered always enabled
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}