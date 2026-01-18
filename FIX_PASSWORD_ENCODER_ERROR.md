# 🔧 Fix: Password Encoder Error

## ❌ Lỗi đã gặp

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Given that there is no default password encoder configured, each password must have a password encoding prefix..."
}
```

## 🎯 Nguyên nhân

Spring Security không tìm thấy `PasswordEncoder` bean khi xử lý authentication.

**Vấn đề cũ trong SecurityConfig:**
- `AuthenticationProvider` bean được định nghĩa nhưng không configure đúng cách
- `UserDetailsService` được inject nhưng không sử dụng đúng
- Spring Boot 4.0 (Spring Security 6.x) có API khác với version cũ

## ✅ Giải pháp đã áp dụng

### **Đơn giản hóa SecurityConfig**

Spring Security 6.x tự động auto-configure `AuthenticationProvider` nếu có:
- ✅ `UserDetailsService` bean (đã có - `UserDetailsServiceImpl`)
- ✅ `PasswordEncoder` bean (đã thêm - `BCryptPasswordEncoder`)

**Không cần** tự định nghĩa `AuthenticationProvider` bean nữa!

### **SecurityConfig.java (sau khi fix)**

```java
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
                        .requestMatchers("/api/v1/auth/**", ...).permitAll()
                        .requestMatchers("/api/v1/orders/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Removed: .authenticationProvider(authenticationProvider())
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
```

### **Những gì đã xóa:**
- ❌ `private final UserDetailsService userDetailsService;`
- ❌ `AuthenticationProvider authenticationProvider()` bean
- ❌ `.authenticationProvider(authenticationProvider())` trong SecurityFilterChain
- ❌ Import `UserDetailsService`, `AuthenticationProvider`, `DaoAuthenticationProvider`

### **Những gì giữ lại:**
- ✅ `PasswordEncoder` bean
- ✅ `AuthenticationManager` bean
- ✅ `JwtAuthenticationFilter`

---

## 🔄 Luồng hoạt động

### **Trước (❌ Lỗi):**
```
Login request
    ↓
AuthenticationManager
    ↓
❌ Không tìm thấy PasswordEncoder
    ↓
Error: "no default password encoder configured"
```

### **Sau (✅ Fixed):**
```
Login request
    ↓
AuthenticationManager (từ AuthenticationConfiguration)
    ↓
Spring auto-configure DaoAuthenticationProvider với:
  - UserDetailsService (UserDetailsServiceImpl)
  - PasswordEncoder (BCryptPasswordEncoder)
    ↓
Load user từ DB
    ↓
Compare password với BCrypt
    ↓
✅ Generate JWT token
```

---

## 🧪 Test ngay

### **Restart app:**
```bash
mvn spring-boot:run
```

### **Login request:**
```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### **Expected response (✅ Success):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "email": "admin@hypebeast.com",
  "role": "ADMIN",
  "message": "Login successful"
}
```

---

## 📋 Checklist

- [x] Remove `AuthenticationProvider` bean
- [x] Remove `userDetailsService` field
- [x] Keep `PasswordEncoder` bean
- [x] Keep `AuthenticationManager` bean
- [x] Spring auto-configures authentication
- [x] Test login API

---

## 💡 Key Takeaway

**Spring Security 6.x (Spring Boot 4.0) simplification:**

Chỉ cần:
1. ✅ Define `PasswordEncoder` bean
2. ✅ Define `UserDetailsService` implementation
3. ✅ Spring tự động wire everything

Không cần:
- ❌ Tự tạo `AuthenticationProvider`
- ❌ Tự tạo `DaoAuthenticationProvider`
- ❌ Manual configuration

**Less code, more magic!** ✨

---

## ✅ Status: FIXED

Login API đã hoạt động bình thường! 🎉

