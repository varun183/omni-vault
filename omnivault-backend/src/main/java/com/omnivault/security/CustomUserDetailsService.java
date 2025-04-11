    package com.omnivault.security;

    import com.omnivault.domain.model.User;
    import com.omnivault.exception.ResourceNotFoundException;
    import com.omnivault.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.UUID;

    /**
     * Custom implementation of Spring Security's UserDetailsService.
     * Provides methods to load user details for authentication and authorization,
     * supporting both username/email-based and ID-based user lookups.
     */
    @Service
    @RequiredArgsConstructor
    public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        /**
         * Loads a user by username or email for authentication.
         * Attempts to find a user by either username or email.
         * Throws an exception if no user is found.
         *
         * @param usernameOrEmail Username or email to search for
         * @return UserDetails for the found user
         * @throws UsernameNotFoundException if no user exists with the given credentials
         */
        @Override
        @Transactional
        public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

            return UserPrincipal.create(user);
        }

        /**
         * Loads a user by their unique identifier.
         * Used for loading user details after initial authentication,
         * such as when retrieving user information from a token.
         *
         * @param id Unique identifier of the user
         * @return UserDetails for the found user
         * @throws ResourceNotFoundException if no user exists with the given ID
         */
        @Transactional
        public UserDetails loadUserById(UUID id) {
            User user = userRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User", "id", id));

            return UserPrincipal.create(user);
        }
    }