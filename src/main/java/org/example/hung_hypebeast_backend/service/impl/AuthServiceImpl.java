package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.LoginRequest;
import org.example.hung_hypebeast_backend.dto.response.AuthResponse;
import org.example.hung_hypebeast_backend.exception.UserNotFoundException;
import org.example.hung_hypebeast_backend.mapper.AuthMapper;
import org.example.hung_hypebeast_backend.repository.UserRepository;
import org.example.hung_hypebeast_backend.service.AuthService;
import org.example.hung_hypebeast_backend.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;


    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực thông tin đăng nhập
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Lấy thông tin user từ database
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException(request.getUsername(), true));

        // 3. Tạo JWT token
        var jwtToken = jwtUtil.generateToken(user);

        // 4. Sử dụng mapper để tạo response
        return authMapper.toAuthResponse(user, jwtToken);
    }
}

