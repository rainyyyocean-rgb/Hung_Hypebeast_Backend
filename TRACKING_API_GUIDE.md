# 📍 API Tracking Đơn Hàng

## Endpoint mới đã tạo

### GET `/api/v1/orders/track/{trackingToken}`

API này cho phép khách hàng tracking trạng thái đơn hàng bằng tracking token.

## 📝 Thông tin API

### Request
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/orders/track/{trackingToken}`
- **Path Variable:** 
  - `trackingToken` (String) - Mã tracking của đơn hàng

### Response Success (200 OK)

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

### Response Error (500 Internal Server Error)

```json
{
  "timestamp": "2026-01-18T22:23:10.852",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Không tìm thấy đơn hàng với mã tracking: invalid-token",
  "path": "/api/v1/orders/track/invalid-token"
}
```

## 🎯 Trạng thái đơn hàng

| Status Code | Tên trạng thái | Mô tả |
|-------------|----------------|-------|
| `PENDING` | Chờ thanh toán | Đơn hàng chờ khách thanh toán (SePay) |
| `PAID` | Đã thanh toán - Đang chuẩn bị hàng | Đã nhận tiền, đang chuẩn bị hàng |
| `SHIPPING` | Đang giao hàng | Đơn đang trên đường giao |
| `DELIVERED` | Đã giao hàng thành công | Khách đã nhận hàng |
| `CANCELLED` | Đã hủy | Đơn hàng bị hủy |

## 🔗 Link trong Email

Email gửi cho khách hàng sẽ chứa link:
```
http://localhost:8080/api/v1/orders/track/{trackingToken}
```

Khi khách hàng click vào link, họ sẽ nhận được JSON response với đầy đủ thông tin đơn hàng.

## 📋 Ví dụ sử dụng

### Ví dụ 1: Tracking với cURL
```bash
curl -X GET "http://localhost:8080/api/v1/orders/track/abc-123-xyz-789"
```

### Ví dụ 2: Tracking với Browser
Mở trình duyệt và truy cập:
```
http://localhost:8080/api/v1/orders/track/abc-123-xyz-789
```

Browser sẽ hiển thị JSON response.

### Ví dụ 3: Tracking với Postman
```
GET http://localhost:8080/api/v1/orders/track/abc-123-xyz-789
```

## 🔧 Implementation Details

### Controller
```java
@GetMapping("/track/{trackingToken}")
public ResponseEntity<OrderResponse> trackOrder(@PathVariable String trackingToken) {
    return ResponseEntity.ok(orderService.trackOrderByToken(trackingToken));
}
```

### Service
```java
@Override
@Transactional(readOnly = true)
public OrderResponse trackOrderByToken(String trackingToken) {
    // 1. Tìm đơn hàng theo tracking token
    Order order = orderRepository.findByTrackingToken(trackingToken)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

    // 2. Force load items
    order.getItems().size();

    // 3. Trả về thông tin
    return OrderResponse.builder()
            .orderId(order.getId())
            .status(order.getStatus().name())
            .message("Trạng thái đơn hàng: " + getStatusMessage(order.getStatus()))
            // ... other fields
            .build();
}
```

## 🚀 Luồng hoạt động

### Luồng COD:
```
1. Khách đặt hàng (POST /api/v1/orders)
2. Hệ thống tạo tracking token
3. Email gửi với link tracking
4. Khách click link → GET /api/v1/orders/track/{token}
5. Hệ thống trả về JSON với trạng thái hiện tại
```

### Luồng SePay:
```
1. Khách đặt hàng (POST /api/v1/orders) → PENDING
2. Khách thanh toán qua SePay
3. Webhook xác nhận → Status = PAID
4. Email gửi với link tracking
5. Khách click link → GET /api/v1/orders/track/{token}
6. Hệ thống trả về JSON với trạng thái PAID
```

## ⚙️ Cấu hình

### Không cần cấu hình thêm!

Email service sẽ tự động sử dụng `server.port` từ application.yaml:
```yaml
server:
  port: ${SERVER_PORT}  # Default 8080
```

Link tracking được generate tự động:
```
http://localhost:{SERVER_PORT}/api/v1/orders/track/{trackingToken}
```

## 📊 So sánh: Frontend vs Backend API

| Aspect | Frontend URL (Cũ) | Backend API (Mới) |
|--------|-------------------|-------------------|
| URL | `http://localhost:3000/tracking/{token}` | `http://localhost:8080/api/v1/orders/track/{token}` |
| Dependency | Cần frontend chạy | Chỉ cần backend |
| Response | HTML page | JSON data |
| Sử dụng | End user friendly | API headless |
| Error | Refuse to connect nếu frontend down | Luôn hoạt động nếu backend up |

## ✅ Ưu điểm của Backend API

1. **Không phụ thuộc frontend:** Backend API luôn hoạt động
2. **RESTful:** Tuân thủ chuẩn API
3. **Headless:** Phù hợp với kiến trúc headless
4. **Flexible:** Frontend/Mobile app có thể consume API này
5. **Simple:** Chỉ cần 1 service thay vì 2

## 🧪 Testing

### Test Case 1: Tracking đơn hàng hợp lệ
```bash
# 1. Tạo đơn hàng
POST http://localhost:8080/api/v1/orders
{
  "customerName": "Test",
  "customerEmail": "test@example.com",
  "paymentMethod": "COD",
  ...
}

# Response:
{
  "trackingToken": "abc-123-xyz"
  ...
}

# 2. Tracking đơn hàng
GET http://localhost:8080/api/v1/orders/track/abc-123-xyz

# Response: 200 OK với đầy đủ thông tin
```

### Test Case 2: Tracking token không tồn tại
```bash
GET http://localhost:8080/api/v1/orders/track/invalid-token

# Response: 500 Error
{
  "message": "Không tìm thấy đơn hàng với mã tracking: invalid-token"
}
```

### Test Case 3: Tracking sau khi cập nhật trạng thái
```bash
# 1. Admin cập nhật trạng thái
PATCH http://localhost:8080/api/v1/orders/admin/123/status?status=SHIPPING

# 2. Khách tracking lại
GET http://localhost:8080/api/v1/orders/track/abc-123-xyz

# Response: Status = SHIPPING
{
  "status": "SHIPPING",
  "message": "Trạng thái đơn hàng: Đang giao hàng"
}
```

## 🎨 Frontend Integration (Tùy chọn)

Nếu muốn tạo UI đẹp cho tracking, frontend có thể:

```javascript
// React/Vue/Angular example
async function trackOrder(token) {
  const response = await fetch(
    `http://localhost:8080/api/v1/orders/track/${token}`
  );
  const data = await response.json();
  
  // Display data in UI
  console.log(data.status); // "PAID"
  console.log(data.message); // "Trạng thái đơn hàng: Đã thanh toán..."
}
```

## 📝 Notes

- API này là **public**, không cần authentication
- Tracking token đóng vai trò như một "secret key" để xem đơn hàng
- Nên giữ tracking token bảo mật, không share public
- Có thể thêm rate limiting để tránh spam

## 🔐 Security Considerations (Future)

Có thể cải thiện:
- [ ] Add rate limiting
- [ ] Add CAPTCHA cho public endpoint
- [ ] Encrypt tracking token
- [ ] Add expiration time cho token
- [ ] Log tracking access

## Status: ✅ READY TO USE

