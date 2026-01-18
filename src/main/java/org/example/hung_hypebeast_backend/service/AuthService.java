package org.example.hung_hypebeast_backend.service;

import org.example.hung_hypebeast_backend.dto.request.LoginRequest;
import org.example.hung_hypebeast_backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}

