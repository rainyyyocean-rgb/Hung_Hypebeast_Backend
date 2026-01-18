# 🚀 Quick Start - Email Feature

## Bước 1: Cấu hình Email (Bắt buộc)

Mở file `.env` và cập nhật:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_URL=http://localhost:3000
```

### Lấy App Password từ Gmail:

1. Truy cập: https://myaccount.google.com/security
2. Bật **2-Step Verification** (nếu chưa có)
3. Tìm **App passwords** → Tạo mật khẩu mới
4. Chọn app: **Mail**, device: **Other (Custom name)**
5. Copy mật khẩu 16 ký tự → Paste vào `MAIL_PASSWORD`

## Bước 2: Chạy ứng dụng

```bash
mvn spring-boot:run
```

## Bước 3: Test

### Test COD (gửi email ngay):
```bash
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerName": "Nguyễn Văn A",
  "customerEmail": "test@example.com",
  "customerPhone": "0123456789",
  "shippingAddress": "123 Đường ABC, TP.HCM",
  "paymentMethod": "COD",
  "items": [
    {
      "skuId": 1,
      "quantity": 2
    }
  ]
}
```
→ Email gửi ngay lập tức ✅

### Test SePay (gửi email sau webhook):
1. Tạo đơn với `"paymentMethod": "SEPAY"`
2. Gọi webhook: `POST http://localhost:8080/api/payment/sepay-webhook?token={trackingToken}`
3. Email gửi sau khi xác nhận thanh toán ✅

## Kiểm tra Email

Mở Gmail inbox của `customerEmail` → Tìm email "Cảm ơn bạn đã mua hàng"

## ❓ Troubleshooting

### Email không gửi?
1. Check console logs
2. Verify `MAIL_USERNAME` và `MAIL_PASSWORD` đúng
3. Đảm bảo App Password được tạo từ Google

### Email vào spam?
- Kiểm tra folder Spam/Junk
- Đánh dấu "Not Spam" để lần sau vào inbox

## 📖 Đọc thêm

- **Chi tiết**: `EMAIL_FEATURE_GUIDE.md`
- **Tóm tắt**: `IMPLEMENTATION_SUMMARY.md`

