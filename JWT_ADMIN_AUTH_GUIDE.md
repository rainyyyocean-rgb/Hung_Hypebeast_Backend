# 🔐 JWT Authentication - Admin Only

## ✅ Đã triển khai

Hệ thống JWT authentication cho **ADMIN** đã được cài đặt hoàn chỉnh.

---

## 🎯 Tính năng

✅ **Chỉ có login cho admin** (không có register)  
✅ **JWT Token authentication**  
✅ **Protected admin endpoints** với `@PreAuthorize("hasRole('ADMIN')")`  
✅ **Auto-create admin user** khi app start lần đầu  

---

## 👤 Admin Account Mặc định

Khi app start lần đầu, hệ thống tự động tạo admin user:

```
Username: admin
Password: admin123
Email: admin@hypebeast.com
Role: ADMIN
```

**⚠️ Lưu ý:** Nên đổi password sau khi login lần đầu (có thể implement sau)

---

## 🔑 API Authentication

### **1. Login Admin**

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response (Success):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "email": "admin@hypebeast.com",
  "role": "ADMIN",
  "message": "Login successful"
}
```

**Response (Failed):**
```json
{
  "timestamp": "2026-01-18T23:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Bad credentials"
}
```

---

## 🔒 Sử dụng JWT Token

### **Cách 1: Header Authorization (Khuyến nghị)**

```http
GET /api/v1/orders/admin?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### **Cách 2: Postman**

1. Gọi API `/api/v1/auth/login` để lấy token
2. Copy giá trị `token` từ response
3. Trong tab **Authorization**:
   - Type: **Bearer Token**
   - Token: paste token vừa copy
4. Send request

### **Cách 3: cURL**

```bash
curl -X GET "http://localhost:8080/api/v1/orders/admin?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🛡️ Protected Endpoints

### **Admin-only endpoints** (Cần JWT token với role ADMIN):

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/v1/orders/admin` | Xem danh sách đơn hàng |
| PATCH | `/api/v1/orders/admin/{id}/status` | Cập nhật trạng thái đơn |

### **Public endpoints** (Không cần token):

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/v1/auth/login` | Login admin |
| POST | `/api/v1/orders` | Tạo đơn hàng (khách) |
| GET | `/api/v1/orders/track/{token}` | Tracking đơn hàng |
| POST | `/api/payment/**` | Payment webhook |
| GET | `/api/v1/products/**` | Xem sản phẩm |
| GET | `/api/v1/categories/**` | Xem categories |

---

## 📋 Ví dụ đầy đủ

### **Step 1: Login để lấy token**

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNTYxMDQwMCwiZXhwIjoxNzA1Njk2ODAwfQ.abc123...",
  "username": "admin",
  "email": "admin@hypebeast.com",
  "role": "ADMIN",
  "message": "Login successful"
}
```

### **Step 2: Sử dụng token để gọi admin API**

```bash
curl -X GET "http://localhost:8080/api/v1/orders/admin?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNTYxMDQwMCwiZXhwIjoxNzA1Njk2ODAwfQ.abc123..."
```

**Response:**
```json
{
  "content": [
    {
      "orderId": 1,
      "customerName": "Nguyễn Văn A",
      "status": "CONFIRMED",
      ...
    }
  ],
  "totalPages": 1,
  "totalElements": 10
}
```

### **Step 3: Cập nhật trạng thái đơn hàng**

```bash
curl -X PATCH "http://localhost:8080/api/v1/orders/admin/1/status?status=SHIPPING" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "orderId": 1,
  "customerName": "Nguyễn Văn A",
  "status": "SHIPPING",
  "message": "Cập nhật trạng thái thành công!"
}
```

---

## ⚙️ Cấu hình

### **application.yaml**

```yaml
jwt:
  secret: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
  expiration: ${JWT_EXPIRATION:86400000} # 24 hours
```

### **.env** (Production)

```env
# JWT Configuration
JWT_SECRET=your-super-secret-key-at-least-256-bits
JWT_EXPIRATION=86400000  # 24 hours
```

**⚠️ Production:** Phải đổi `JWT_SECRET` thành secret key mới!

---

## 🔄 JWT Token Lifecycle

```
1. Admin login → Server tạo JWT token (expires in 24h)
2. Admin lưu token (localStorage/sessionStorage)
3. Mỗi request → Gửi token trong Authorization header
4. Server verify token → Allow/Deny request
5. Token expired (sau 24h) → Admin phải login lại
```

---

## ❌ Error Handling

### **401 Unauthorized - Không có token**
```json
{
  "timestamp": "2026-01-18T23:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

**Fix:** Thêm `Authorization: Bearer {token}` vào header

### **403 Forbidden - Token hợp lệ nhưng không phải ADMIN**
```json
{
  "timestamp": "2026-01-18T23:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

**Fix:** Chỉ admin mới được access endpoint này

### **401 Unauthorized - Token expired**
```json
{
  "timestamp": "2026-01-18T23:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT expired"
}
```

**Fix:** Login lại để lấy token mới

---

## 📁 Files đã tạo

### **Entities & Enums**
- ✅ `Role.java` - Enum (USER, ADMIN)
- ✅ `User.java` - User entity with UserDetails

### **Security**
- ✅ `SecurityConfig.java` - Spring Security configuration
- ✅ `JwtAuthenticationFilter.java` - Filter để verify JWT
- ✅ `JwtUtil.java` - JWT utility (generate, validate)

### **Services**
- ✅ `AuthService.java` - Interface
- ✅ `AuthServiceImpl.java` - Login logic
- ✅ `UserDetailsServiceImpl.java` - Load user from DB

### **Controllers**
- ✅ `AuthController.java` - `/api/v1/auth/login`

### **DTOs**
- ✅ `LoginRequest.java` - Login payload
- ✅ `AuthResponse.java` - Response với token

### **Repository**
- ✅ `UserRepository.java` - JPA repository

### **Migration**
- ✅ `DataMigration.java` - Auto create admin user

---

## 🧪 Testing

### **Test 1: Login thành công**
```bash
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```
✅ Expected: 200 OK với JWT token

### **Test 2: Login sai password**
```bash
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "wrong"
}
```
❌ Expected: 401 Unauthorized

### **Test 3: Access admin endpoint không có token**
```bash
GET /api/v1/orders/admin
```
❌ Expected: 401 Unauthorized

### **Test 4: Access admin endpoint với token hợp lệ**
```bash
GET /api/v1/orders/admin
Authorization: Bearer {valid_token}
```
✅ Expected: 200 OK với data

---

## 🚀 Production Checklist

- [ ] Đổi `JWT_SECRET` thành secret key mạnh
- [ ] Đổi password admin default
- [ ] Set `JWT_EXPIRATION` phù hợp (1-24h)
- [ ] Enable HTTPS
- [ ] Add rate limiting cho `/api/v1/auth/login`
- [ ] Add refresh token mechanism (optional)
- [ ] Log failed login attempts
- [ ] Implement "Change Password" API

---

## 💡 Mở rộng (Future)

### **1. Refresh Token**
```java
// Thêm refresh token để không phải login lại thường xuyên
public class AuthResponse {
    private String accessToken;
    private String refreshToken; // Expires in 7 days
}
```

### **2. Change Password**
```http
PATCH /api/v1/auth/change-password
Authorization: Bearer {token}

{
  "oldPassword": "admin123",
  "newPassword": "new_secure_password"
}
```

### **3. Multiple Admin Users**
```http
POST /api/v1/admin/users (Admin only)

{
  "username": "admin2",
  "email": "admin2@hypebeast.com",
  "password": "password",
  "role": "ADMIN"
}
```

---

## ✅ Status: READY TO USE

**Admin authentication đã hoàn tất!**

Restart app và test ngay:
```bash
mvn spring-boot:run
```

Console sẽ hiển thị:
```
✅ [Migration] Created default admin user (username: admin, password: admin123)
```

**Happy coding!** 🎉

