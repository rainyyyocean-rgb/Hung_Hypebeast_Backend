package org.example.hung_hypebeast_backend.mapper;

import org.example.hung_hypebeast_backend.dto.response.AuthResponse;
import org.example.hung_hypebeast_backend.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper để chuyển đổi giữa User entity và AuthResponse DTO
 */
@Component
public class AuthMapper {

    /**
     * Chuyển đổi User entity sang AuthResponse
     *
     * @param user User entity
     * @param jwtToken JWT token được tạo
     * @return AuthResponse
     */
    public AuthResponse toAuthResponse(User user, String jwtToken) {
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
