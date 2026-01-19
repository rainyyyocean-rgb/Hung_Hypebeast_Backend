package org.example.hung_hypebeast_backend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.entity.User;
import org.example.hung_hypebeast_backend.enums.Role;
import org.example.hung_hypebeast_backend.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataMigration {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void runMigrations() {
        createDefaultAdminUser();
    }


    private void createDefaultAdminUser() {
        try {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@hypebeast.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .build();

                userRepository.save(admin);
                System.out.println("✅ [Migration] Created default admin user (username: admin, password: admin123)");
            }
        } catch (Exception e) {
            System.err.println("❌ [Migration] Failed to create admin user: " + e.getMessage());
        }
    }
}
