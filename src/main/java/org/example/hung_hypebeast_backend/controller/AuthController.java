package org.example.hung_hypebeast_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.LoginRequest;
import org.example.hung_hypebeast_backend.dto.response.AuthResponse;
import org.example.hung_hypebeast_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API xác thực và đăng nhập")
public class AuthController {

    private final AuthService authService;



    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập",
            description = """
                    API đăng nhập vào hệ thống.
                    
                    **Tài khoản admin mặc định:**
                    - Username: `admin`
                    - Password: `admin123`
                    
                    Sau khi đăng nhập thành công, bạn sẽ nhận được JWT token.
                    Sử dụng token này trong header Authorization: Bearer {token} cho các API yêu cầu authentication.
                    """
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

