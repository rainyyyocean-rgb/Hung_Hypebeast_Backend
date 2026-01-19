package org.example.hung_hypebeast_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/error",                    // Spring Boot error page
                                "/api/v1/auth/**",           // Login/Register
                                "/api/v1/orders",            // Tạo đơn hàng (public)
                                "/api/v1/orders/track/**",   // Tracking (public)
                                "/api/v1/payments/**",       // Payment webhook (public)
                                "/api/v1/products/**",       // Xem sản phẩm (public)
                                "/api/v1/categories/**",     // Xem categories (public)
                                "/api/v1/cart/**",           // Cart (public)
                                "/swagger-ui/**",            // Swagger
                                "/swagger-ui.html",          // Swagger UI
                                "/v3/api-docs/**",           // API Docs
                                "/v3/api-docs"               // API Docs root
                        ).permitAll()
                        // Admin endpoints
                        .requestMatchers("/api/v1/orders/admin/**").hasRole("ADMIN")
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // ✅ Xử lý exception 401 và 403 - trả về JSON thay vì empty response
                .exceptionHandling(exception -> exception
                        // 401 Unauthorized - Thiếu token hoặc token không hợp lệ
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("timestamp", LocalDateTime.now().toString());
                            errorResponse.put("status", 401);
                            errorResponse.put("error", "Unauthorized");
                            errorResponse.put("message", "Yêu cầu xác thực. Vui lòng đăng nhập và gửi JWT token trong header Authorization");
                            errorResponse.put("path", request.getRequestURI());

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(errorResponse));
                        })
                        // 403 Forbidden - Có token nhưng không đủ quyền
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("timestamp", LocalDateTime.now().toString());
                            errorResponse.put("status", 403);
                            errorResponse.put("error", "Forbidden");
                            errorResponse.put("message", "Bạn không có quyền truy cập tài nguyên này. Yêu cầu quyền ADMIN");
                            errorResponse.put("path", request.getRequestURI());

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(errorResponse));
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

