# ✅ Cập nhật: API Tracking Đơn Hàng

## 🎯 Vấn đề đã giải quyết

**Vấn đề ban đầu:**
- Link trong email trỏ đến frontend: `http://localhost:3000/tracking/{token}`
- Frontend chưa có route này → **Refuse to connect**
- Phụ thuộc vào frontend → Không phù hợp với kiến trúc headless

**Giải pháp:**
- ✅ Tạo API endpoint backend để tracking: `GET /api/v1/orders/track/{token}`
- ✅ API trả về JSON với thông tin đơn hàng
- ✅ Không cần frontend, hoạt động độc lập
- ✅ Phù hợp với kiến trúc headless/API-first

---

## 📝 Thay đổi đã thực hiện

### 1. **OrderService.java** - Thêm method mới
```java
// Hàm tracking đơn hàng theo token (cho khách hàng)
OrderResponse trackOrderByToken(String trackingToken);
```

### 2. **OrderServiceImpl.java** - Implementation
```java
@Override
@Transactional(readOnly = true)
public OrderResponse trackOrderByToken(String trackingToken) {
    Order order = orderRepository.findByTrackingToken(trackingToken)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    
    order.getItems().size(); // Force load items
    
    return OrderResponse.builder()
            .orderId(order.getId())
            .status(order.getStatus().name())
            .message("Trạng thái đơn hàng: " + getStatusMessage(order.getStatus()))
            // ... other fields
            .build();
}

private String getStatusMessage(OrderStatus status) {
    return switch (status) {
        case PENDING -> "Chờ thanh toán";
        case PAID -> "Đã thanh toán - Đang chuẩn bị hàng";
        case SHIPPING -> "Đang giao hàng";
        case DELIVERED -> "Đã giao hàng thành công";
        case CANCELLED -> "Đã hủy";
    };
}
```

### 3. **OrderController.java** - Thêm endpoint
```java
// API tracking đơn hàng theo token (Dành cho khách hàng)
@GetMapping("/track/{trackingToken}")
public ResponseEntity<OrderResponse> trackOrder(@PathVariable String trackingToken) {
    return ResponseEntity.ok(orderService.trackOrderByToken(trackingToken));
}
```

### 4. **EmailServiceImpl.java** - Cập nhật link
**Trước:**
```java
@Value("${app.frontend.url:http://localhost:3000}")
private String frontendUrl;

String trackingUrl = frontendUrl + "/tracking/" + order.getTrackingToken();
```

**Sau:**
```java
@Value("${server.port:8080}")
private String serverPort;

String trackingUrl = "http://localhost:" + serverPort + "/api/v1/orders/track/" + order.getTrackingToken();
```

### 5. **application.yaml** - Xóa frontend URL
```yaml
# Đã xóa phần này:
# app:
#   frontend:
#     url: ${FRONTEND_URL:http://localhost:3000}
```

### 6. **.env** - Xóa FRONTEND_URL
```env
# Đã xóa:
# FRONTEND_URL=http://localhost:3000
```

---

## 🔗 API Endpoint mới

### Endpoint
```
GET /api/v1/orders/track/{trackingToken}
```

### Request Example
```bash
GET http://localhost:8080/api/v1/orders/track/abc-123-xyz-789
```

### Response Example (200 OK)
```json
{
  "orderId": 123,
  "customerName": "Nguyễn Văn A",
  "customerPhone": "0123456789",
  "customerEmail": "test@example.com",
  "shippingAddress": "123 ABC, TP.HCM",
  "totalAmount": 1500000,
  "status": "PAID",
  "paymentMethod": "COD",
  "trackingToken": "abc-123-xyz-789",
  "createdAt": "2026-01-18T15:30:00",
  "message": "Trạng thái đơn hàng: Đã thanh toán - Đang chuẩn bị hàng"
}
```

### Response Example (Error)
```json
{
  "timestamp": "2026-01-18T22:23:10.852",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Không tìm thấy đơn hàng với mã tracking: invalid-token",
  "path": "/api/v1/orders/track/invalid-token"
}
```

---

## 📧 Link trong Email

Email giờ sẽ chứa link:
```
http://localhost:8080/api/v1/orders/track/{trackingToken}
```

Khi khách hàng click vào:
- ✅ Browser sẽ hiển thị JSON với thông tin đơn hàng
- ✅ Không còn "Refuse to connect"
- ✅ Luôn hoạt động khi backend đang chạy

---

## 🎯 Trạng thái đơn hàng

| Status | Message hiển thị |
|--------|------------------|
| `PENDING` | Chờ thanh toán |
| `PAID` | Đã thanh toán - Đang chuẩn bị hàng |
| `SHIPPING` | Đang giao hàng |
| `DELIVERED` | Đã giao hàng thành công |
| `CANCELLED` | Đã hủy |

---

## 🧪 Test

### Test 1: Tạo đơn và tracking
```bash
# 1. Tạo đơn COD
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerName": "Test User",
  "customerEmail": "test@example.com",
  "customerPhone": "0123456789",
  "shippingAddress": "123 ABC",
  "paymentMethod": "COD",
  "items": [{"skuId": 1, "quantity": 1}]
}

# Response sẽ có trackingToken, ví dụ: "abc-123-xyz"

# 2. Check email → Click link tracking

# 3. Hoặc test trực tiếp:
GET http://localhost:8080/api/v1/orders/track/abc-123-xyz

# Response: JSON với status = PAID (COD tự động PAID)
```

### Test 2: Tracking sau webhook
```bash
# 1. Tạo đơn SePay
POST http://localhost:8080/api/v1/orders
{
  "paymentMethod": "SEPAY",
  ...
}

# 2. Gọi webhook
POST http://localhost:8080/api/payment/sepay-webhook?token=abc-123-xyz

# 3. Tracking
GET http://localhost:8080/api/v1/orders/track/abc-123-xyz

# Response: status = PAID với message tương ứng
```

---

## 📊 So sánh

| Aspect | Trước (Frontend) | Sau (Backend API) |
|--------|------------------|-------------------|
| URL | `/tracking/{token}` | `/api/v1/orders/track/{token}` |
| Host | localhost:3000 | localhost:8080 |
| Dependency | Cần frontend | Chỉ cần backend |
| Response | HTML | JSON |
| Error | Refuse to connect | Proper JSON error |
| Architecture | Full-stack | Headless/API-first |

---

## ✅ Files đã thay đổi

- ✅ `OrderService.java` - Thêm method trackOrderByToken
- ✅ `OrderServiceImpl.java` - Implementation tracking + getStatusMessage
- ✅ `OrderController.java` - Thêm GET endpoint /track/{token}
- ✅ `EmailServiceImpl.java` - Đổi URL từ frontend → backend
- ✅ `application.yaml` - Xóa app.frontend.url
- ✅ `.env` - Xóa FRONTEND_URL

## 📚 Documentation

- ✅ `TRACKING_API_GUIDE.md` - Hướng dẫn chi tiết API tracking

---

## 🚀 Status

**✅ HOÀN TẤT**

API tracking đã sẵn sàng sử dụng:
- Link trong email hoạt động bình thường
- Không còn lỗi "Refuse to connect"
- Phù hợp với kiến trúc headless
- Trả về JSON chuẩn REST API

---

## 💡 Lưu ý

### Production
Khi deploy production, cần update hardcoded URL trong EmailServiceImpl:

**Hiện tại:**
```java
String trackingUrl = "http://localhost:" + serverPort + "/api/v1/orders/track/" + order.getTrackingToken();
```

**Production (nên dùng):**
```java
@Value("${app.base.url:http://localhost:8080}")
private String baseUrl;

String trackingUrl = baseUrl + "/api/v1/orders/track/" + order.getTrackingToken();
```

Và trong `application.yaml`:
```yaml
app:
  base:
    url: ${APP_BASE_URL:http://localhost:8080}
```

Trong production `.env`:
```env
APP_BASE_URL=https://api.yourdomain.com
```

### Mobile App / Frontend Integration
Nếu có mobile app hoặc frontend, có thể consume API này:
```javascript
fetch(`http://localhost:8080/api/v1/orders/track/${token}`)
  .then(res => res.json())
  .then(data => {
    console.log(data.status); // "PAID", "SHIPPING", etc.
    console.log(data.message); // "Đã thanh toán - Đang chuẩn bị hàng"
  });
```

---

**Hoàn thành!** 🎉

