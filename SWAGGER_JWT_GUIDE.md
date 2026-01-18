# 📚 Swagger UI với JWT Authentication

## ✅ Đã cấu hình

Swagger UI đã được cấu hình để hỗ trợ JWT authentication cho các API admin.

---

## 🌐 Truy cập Swagger UI

Sau khi start app, truy cập:

```
http://localhost:8080/swagger-ui/index.html
```

Hoặc:

```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Cách sử dụng JWT trong Swagger

### **Bước 1: Login để lấy JWT Token**

1. Trong Swagger UI, tìm endpoint **`POST /api/v1/auth/login`**
2. Click **"Try it out"**
3. Nhập credentials:
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
4. Click **"Execute"**
5. Copy giá trị `token` từ response

**Response mẫu:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNTYxMDQwMCwiZXhwIjoxNzA1Njk2ODAwfQ...",
  "username": "admin",
  "email": "admin@hypebeast.com",
  "role": "ADMIN",
  "message": "Login successful"
}
```

### **Bước 2: Authorize trong Swagger**

1. Ở góc trên bên phải Swagger UI, click nút **"Authorize"** 🔓
2. Trong popup hiện lên:
   - Paste JWT token vừa copy vào ô **"Value"**
   - **KHÔNG** thêm chữ "Bearer " phía trước (Swagger tự động thêm)
3. Click **"Authorize"**
4. Click **"Close"**

**Lúc này icon sẽ đổi thành 🔒 (đã authorize)**

### **Bước 3: Test Admin APIs**

Bây giờ bạn có thể test các API admin:

#### **1. Xem danh sách đơn hàng**
```
GET /api/v1/orders/admin
```
- Thử với params: `page=0`, `size=10`, `sort=id`

#### **2. Cập nhật trạng thái đơn**
```
PATCH /api/v1/orders/admin/{id}/status
```
- Thử với `id=1`, `status=SHIPPING`

---

## 📸 Screenshot Flow

```
┌─────────────────────────────────────────────────┐
│ 1. Login Endpoint                               │
│    POST /api/v1/auth/login                      │
│    ↓                                             │
│    Response: {token: "eyJhbG..."}               │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 2. Click "Authorize" button (top right)         │
│    🔓 → Paste token → Authorize                │
│    ↓                                             │
│    Icon changes to 🔒                           │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 3. Try Admin APIs                               │
│    - GET /api/v1/orders/admin                   │
│    - PATCH /api/v1/orders/admin/{id}/status     │
│    ↓                                             │
│    ✅ Success (with JWT in header)              │
└─────────────────────────────────────────────────┘
```

---

## 🎯 Visual Guide

### **Trước khi Authorize:**
```
🔓 Authorize
```
Các endpoint admin sẽ trả về **401 Unauthorized**

### **Sau khi Authorize:**
```
🔒 Authorize (click to logout)
```
Các endpoint admin sẽ hoạt động bình thường ✅

---

## 🔍 Chi tiết cấu hình

### **SwaggerConfig.java**

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Hypebeast E-commerce API")
                .version("1.0")
                .description("API with JWT Authentication"))
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

**Giải thích:**
- **SecurityScheme:** Định nghĩa loại authentication (HTTP Bearer)
- **SecurityRequirement:** Apply globally cho tất cả endpoints
- **bearerFormat:** Specify JWT format

---

## 📋 Endpoints trong Swagger

### **Public Endpoints** (không cần token):
- ✅ `POST /api/v1/auth/login` - Login
- ✅ `POST /api/v1/orders` - Tạo đơn hàng
- ✅ `GET /api/v1/orders/track/{token}` - Tracking
- ✅ `GET /api/v1/products/**` - Xem sản phẩm
- ✅ `POST /api/payment/**` - Payment webhook

### **Protected Endpoints** (cần JWT token ADMIN):
- 🔒 `GET /api/v1/orders/admin` - Xem danh sách đơn
- 🔒 `PATCH /api/v1/orders/admin/{id}/status` - Cập nhật status

Swagger sẽ tự động gửi JWT token trong header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 🧪 Test Flow hoàn chỉnh

### **Scenario 1: Admin xem danh sách đơn hàng**

```bash
# 1. Login
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# Response: Copy token

# 2. Authorize with token in Swagger

# 3. Get orders
GET /api/v1/orders/admin?page=0&size=10

# Response: 200 OK với danh sách đơn hàng
```

### **Scenario 2: Admin cập nhật trạng thái**

```bash
# 1. Authorize (nếu chưa)

# 2. Update status
PATCH /api/v1/orders/admin/1/status?status=SHIPPING

# Response: 200 OK
{
  "orderId": 1,
  "status": "SHIPPING",
  "message": "Cập nhật trạng thái thành công!"
}
```

---

## ❌ Troubleshooting

### **1. Token hết hạn (401 Unauthorized)**
```json
{
  "message": "JWT expired"
}
```
**Fix:** 
- Login lại để lấy token mới
- Re-authorize trong Swagger

### **2. Quên authorize (401 Unauthorized)**
```json
{
  "message": "Full authentication is required"
}
```
**Fix:**
- Click nút "Authorize" 🔓
- Paste token
- Authorize

### **3. Token không đúng format (401 Unauthorized)**
```json
{
  "message": "JWT malformed"
}
```
**Fix:**
- Đảm bảo copy đúng token từ response
- KHÔNG thêm "Bearer " vào đầu token trong Swagger

### **4. Không phải admin (403 Forbidden)**
```json
{
  "message": "Access Denied"
}
```
**Fix:**
- Chỉ user có role ADMIN mới được access
- Đảm bảo login bằng account `admin`

---

## 🎨 Swagger UI Features

### **Try it out:**
- Click để test endpoint ngay trong browser
- Không cần Postman/cURL

### **Models:**
- Xem structure của Request/Response DTOs
- Auto-complete khi nhập JSON

### **Responses:**
- Xem example responses
- HTTP status codes

### **Authorize button:**
- Quản lý JWT token centrally
- Tự động inject vào tất cả requests

---

## 🔧 Customization (Optional)

### **Thêm description cho endpoints:**

```java
@Operation(summary = "Get all orders", 
           description = "Admin only - Get paginated list of orders")
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin")
public ResponseEntity<Page<OrderResponse>> getOrdersForAdmin(...) {
    // ...
}
```

### **Thêm example values:**

```java
@Schema(example = "admin")
private String username;

@Schema(example = "admin123")
private String password;
```

### **Group endpoints by tags:**

```java
@Tag(name = "Admin Orders", description = "Order management APIs for admin")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    // ...
}
```

---

## 📊 So sánh

| Tool | Pros | Cons |
|------|------|------|
| **Swagger UI** | ✅ Interactive UI<br>✅ Built-in auth<br>✅ Documentation | ⚠️ Basic features |
| **Postman** | ✅ Advanced features<br>✅ Collections<br>✅ Tests | ⚠️ Separate app |
| **cURL** | ✅ Lightweight<br>✅ Scriptable | ⚠️ Command line only |

**Khuyến nghị:** 
- Development: **Swagger UI** (quick testing)
- Advanced testing: **Postman**
- CI/CD: **cURL scripts**

---

## ✅ Summary

**Đã cấu hình:**
- ✅ Swagger UI với JWT authentication
- ✅ Authorize button để manage token
- ✅ Tự động inject token vào protected endpoints
- ✅ Interactive API testing

**Truy cập:**
```
http://localhost:8080/swagger-ui/index.html
```

**Login credentials:**
```
Username: admin
Password: admin123
```

**Happy testing!** 🎉

