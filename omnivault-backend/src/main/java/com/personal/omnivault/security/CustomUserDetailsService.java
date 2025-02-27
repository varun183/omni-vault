    package com.personal.omnivault.security;

    import com.personal.omnivault.domain.model.User;
    import com.personal.omnivault.exception.ResourceNotFoundException;
    import com.personal.omnivault.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        @Transactional
        public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

            return UserPrincipal.create(user);
        }

        @Transactional
        public UserDetails loadUserById(UUID id) {
            User user = userRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User", "id", id));

            return UserPrincipal.create(user);
        }
    }