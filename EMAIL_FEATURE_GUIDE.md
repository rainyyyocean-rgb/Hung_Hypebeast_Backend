# Email Notification Feature - Implementation Guide

## 📧 Tính năng Email xác nhận đơn hàng

Hệ thống đã được tích hợp tính năng gửi email tự động "Cảm ơn đã mua hàng" với link tracking trạng thái đơn hàng.

## 🎯 Chức năng chính

### 1. **Đơn hàng COD (Cash On Delivery)**
- Gửi email **ngay sau khi đặt hàng thành công**
- Email chứa thông tin đơn hàng đầy đủ
- Link tracking để theo dõi trạng thái đơn hàng

### 2. **Đơn hàng thanh toán chuyển khoản (SePay)**
- Gửi email **sau khi xác nhận thanh toán thành công** qua webhook
- Email chứa thông tin đơn hàng đầy đủ
- Link tracking để theo dõi trạng thái đơn hàng

## 🔧 Cấu hình

### 1. Cập nhật file `.env`

Thêm các biến môi trường sau:

```env
# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Frontend URL
FRONTEND_URL=http://localhost:3000
```

### 2. Cấu hình Gmail (Khuyến nghị)

Nếu sử dụng Gmail:

1. Bật **2-Factor Authentication** cho tài khoản Gmail
2. Tạo **App Password**:
   - Truy cập: https://myaccount.google.com/security
   - Chọn "App passwords" 
   - Tạo mật khẩu cho ứng dụng "Mail"
   - Sử dụng mật khẩu này cho `MAIL_PASSWORD`

### 3. Cấu hình SMTP khác (Tùy chọn)

Bạn có thể sử dụng các dịch vụ SMTP khác:

#### **SendGrid**
```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
```

#### **Mailgun**
```env
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=your-mailgun-username
MAIL_PASSWORD=your-mailgun-password
```

#### **Outlook/Office 365**
```env
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USERNAME=your-email@outlook.com
MAIL_PASSWORD=your-password
```

## 📝 Nội dung Email

Email được gửi đi bao gồm:

1. **Thông tin khách hàng**: Tên, email, địa chỉ giao hàng
2. **Thông tin đơn hàng**: 
   - Mã đơn hàng
   - Phương thức thanh toán
   - Tổng tiền
3. **Chi tiết sản phẩm**: 
   - Tên sản phẩm
   - Size, màu sắc
   - Số lượng
   - Thành tiền
4. **Link tracking**: Button và link để theo dõi trạng thái đơn hàng

## 🔗 Link Tracking

Format link tracking: `{FRONTEND_URL}/tracking/{trackingToken}`

Ví dụ: `http://localhost:3000/tracking/abc-123-xyz-789`

## 📂 Cấu trúc Code

### Files đã tạo/sửa:

1. **EmailService.java** - Interface cho service gửi email
2. **EmailServiceImpl.java** - Implementation của EmailService
3. **OrderServiceImpl.java** - Thêm logic gửi email cho đơn COD
4. **PaymentServiceImpl.java** - Thêm logic gửi email sau webhook thanh toán
5. **application.yaml** - Thêm cấu hình SMTP và frontend URL
6. **.env** - Thêm biến môi trường cho email

### Luồng hoạt động:

#### Đơn COD:
```
Tạo đơn hàng → Trừ kho → Lưu database → Gửi email → Trả response
```

#### Đơn chuyển khoản:
```
Tạo đơn hàng → Trừ kho → Lưu database → Chờ webhook
                                          ↓
                            Webhook → Cập nhật PAID → Gửi email
```

## 🧪 Test Email

### Test với MailHog (Development)

1. Install MailHog:
```bash
# Windows (using Chocolatey)
choco install mailhog

# macOS (using Homebrew)
brew install mailhog

# hoặc download từ: https://github.com/mailhog/MailHog
```

2. Chạy MailHog:
```bash
mailhog
```

3. Cấu hình .env:
```env
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=test
MAIL_PASSWORD=test
```

4. Truy cập web UI: http://localhost:8025

## 🎨 Template Email

Email sử dụng HTML template với:
- Gradient background đẹp mắt
- Responsive design
- Button CTA nổi bật
- Table hiển thị sản phẩm rõ ràng
- Brand colors (Purple gradient)

## ⚠️ Lưu ý

1. **Error Handling**: Email service được wrap trong try-catch để không ảnh hưởng đến luồng đặt hàng chính
2. **Async Processing**: Nếu cần xử lý lượng lớn email, nên chuyển sang async processing với Spring `@Async`
3. **Rate Limiting**: Chú ý giới hạn gửi email của SMTP provider
4. **Security**: 
   - Không commit `.env` file lên Git
   - Sử dụng App Password, không dùng password thật
   - Enable SSL/TLS cho SMTP

## 🚀 Production Checklist

- [ ] Cấu hình SMTP provider thực tế (SendGrid, AWS SES, etc.)
- [ ] Cập nhật FRONTEND_URL thành domain production
- [ ] Test gửi email thành công
- [ ] Kiểm tra email không bị vào spam
- [ ] Setup monitoring cho email service
- [ ] Cấu hình retry mechanism cho failed emails
- [ ] Add email templates cho các trường hợp khác (hủy đơn, cập nhật trạng thái, etc.)

## 📧 Mở rộng

Có thể mở rộng thêm:

1. **Email hủy đơn**: Khi đơn hàng bị hủy
2. **Email cập nhật trạng thái**: Khi đơn hàng thay đổi trạng thái
3. **Email nhắc nhở**: Nhắc thanh toán cho đơn PENDING
4. **Email marketing**: Gửi khuyến mãi, sản phẩm mới

## 🐛 Troubleshooting

### Email không được gửi

1. Kiểm tra logs trong console
2. Verify SMTP credentials
3. Kiểm tra firewall/network
4. Test SMTP connection:
```bash
telnet smtp.gmail.com 587
```

### Email vào spam

1. Setup SPF, DKIM, DMARC records
2. Sử dụng domain email chuyên nghiệp
3. Tránh từ ngữ spam trong subject/content
4. Warm up email domain

## 📞 Support

Nếu có vấn đề, kiểm tra:
- Application logs
- SMTP server status
- Email configuration trong application.yaml
- Environment variables

