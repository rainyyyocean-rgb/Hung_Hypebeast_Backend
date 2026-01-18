package org.example.hung_hypebeast_backend.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.entity.OrderItem;
import org.example.hung_hypebeast_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public void sendOrderConfirmationEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Cảm ơn bạn đã mua hàng - Đơn hàng #" + order.getId());

            String emailContent = buildEmailContent(order);
            helper.setText(emailContent, true);

            mailSender.send(message);
            System.out.println("✅ Đã gửi email xác nhận đơn hàng cho: " + order.getCustomerEmail());

        } catch (MessagingException e) {
            System.err.println("❌ Lỗi khi gửi email cho đơn hàng #" + order.getId() + ": " + e.getMessage());
            // Không throw exception để không ảnh hưởng đến luồng chính
        }
    }

    private String buildEmailContent(Order order) {
        String trackingUrl = "http://localhost:" + serverPort + "/api/v1/orders/track/" + order.getTrackingToken();

        StringBuilder itemsList = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            itemsList.append(String.format(
                    "<tr>" +
                            "<td style='padding: 10px; border-bottom: 1px solid #eee;'>%s</td>" +
                            "<td style='padding: 10px; border-bottom: 1px solid #eee;'>%s - %s</td>" +
                            "<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: center;'>%d</td>" +
                            "<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: right;'>%,.0f₫</td>" +
                            "</tr>",
                    item.getProductName(),
                    item.getSize(),
                    item.getColor(),
                    item.getQuantity(),
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            ));
        }

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 20px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px;">Cảm ơn bạn đã mua hàng! 🎉</h1>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="font-size: 16px; color: #333; margin: 0 0 20px;">Xin chào <strong>%s</strong>,</p>
                                            <p style="font-size: 14px; color: #666; line-height: 1.6; margin: 0 0 20px;">
                                                Chúng tôi đã nhận được đơn hàng của bạn và đang xử lý. Cảm ơn bạn đã tin tưởng và lựa chọn sản phẩm của chúng tôi!
                                            </p>

                                            <!-- Order Info -->
                                            <table width="100%%" cellpadding="10" style="margin: 20px 0; border: 1px solid #eee; border-radius: 4px;">
                                                <tr>
                                                    <td style="background-color: #f9f9f9; padding: 15px;">
                                                        <strong>Mã đơn hàng:</strong> #%s<br>
                                                        <strong>Phương thức thanh toán:</strong> %s<br>
                                                        <strong>Địa chỉ giao hàng:</strong> %s<br>
                                                        <strong>Tổng tiền:</strong> <span style="color: #667eea; font-size: 18px; font-weight: bold;">%,.0f₫</span>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Order Items -->
                                            <h3 style="color: #333; margin: 30px 0 15px;">Chi tiết đơn hàng:</h3>
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="border: 1px solid #eee; border-radius: 4px;">
                                                <thead>
                                                    <tr style="background-color: #f9f9f9;">
                                                        <th style="padding: 10px; text-align: left; border-bottom: 2px solid #667eea;">Sản phẩm</th>
                                                        <th style="padding: 10px; text-align: left; border-bottom: 2px solid #667eea;">Phân loại</th>
                                                        <th style="padding: 10px; text-align: center; border-bottom: 2px solid #667eea;">SL</th>
                                                        <th style="padding: 10px; text-align: right; border-bottom: 2px solid #667eea;">Thành tiền</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    %s
                                                </tbody>
                                            </table>
                                            
                                            <!-- Tracking Button -->
                                            <div style="text-align: center; margin: 40px 0;">
                                                <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; padding: 15px 40px; border-radius: 50px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 6px rgba(102, 126, 234, 0.4);">
                                                    🔍 Theo dõi đơn hàng
                                                </a>
                                            </div>
                                            
                                            <p style="font-size: 14px; color: #666; line-height: 1.6; margin: 20px 0 0;">
                                                Hoặc truy cập link sau để theo dõi trạng thái đơn hàng:<br>
                                                <a href="%s" style="color: #667eea; word-break: break-all;">%s</a>
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f9f9f9; padding: 20px 30px; text-align: center; border-top: 1px solid #eee;">
                                            <p style="font-size: 12px; color: #999; margin: 0;">
                                                Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.<br>
                                                Email: support@hypebeast.com | Hotline: 1900-xxxx
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
                order.getCustomerName(),
                order.getId(),
                order.getPaymentMethod().name().equals("COD") ? "Thanh toán khi nhận hàng (COD)" : "Chuyển khoản",
                order.getShippingAddress(),
                order.getTotalAmount(),
                itemsList,
                trackingUrl,
                trackingUrl,
                trackingUrl
        );
    }
}

