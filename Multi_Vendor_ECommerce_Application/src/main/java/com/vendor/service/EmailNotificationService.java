
package com.vendor.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.vendor.entity.Order;
import com.vendor.entity.OrderItem;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${onecart.mail.from}")
    private String fromEmail;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String customerName(Order order) {
        return order.getCustomerName() != null
                ? order.getCustomerName() : "Customer";
    }

    private void sendHtmlEmail(String to, String subject, String html)
            throws Exception {

        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Customer email is missing");
        }

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message, true, StandardCharsets.UTF_8.name());

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
        System.out.println("OneCart email sent successfully to: " + to);
    }

    private String emailLayout(String title, String content) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:25px;background:#f3f8f6;
                         font-family:Arial,sans-serif;">

              <div style="max-width:600px;margin:auto;background:white;
                          border-radius:12px;overflow:hidden;">

                <div style="background:#087e78;padding:25px;
                            text-align:center;color:white;">
                  <h1 style="margin:0;">OneCart</h1>
                  <p style="margin:8px 0 0;">Your Shopping Partner</p>
                </div>

                <div style="padding:30px;color:#333;">

                  <h2 style="color:#123e3a;">%s</h2>

                  %s

                  <p style="margin-top:30px;color:#087e78;">
                    Thank you for choosing OneCart!
                  </p>

                  <p style="font-size:13px;color:#777;">
                    OneCart Customer Support
                  </p>

                </div>

                <div style="background:#e5f4f0;padding:15px;
                            text-align:center;font-size:12px;color:#555;">
                  © OneCart. All rights reserved.
                </div>

              </div>
            </body>
            </html>
            """.formatted(title, content);
    }

    // 1. Payment and order confirmation email
    public void sendOrderConfirmationEmail(Order order) {

        StringBuilder items = new StringBuilder();

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                items.append("<li>")
                     .append(item.getProductName())
                     .append(" - Quantity: ")
                     .append(item.getQuantity())
                     .append(" - ₹")
                     .append(item.getSubtotal())
                     .append("</li>");
            }
        }

        String content = """
            <p>Hello %s,</p>

            <p>Your OneCart order has been successfully created.</p>

            <p><b>Order Number:</b> %s</p>
            <p><b>Payment Method:</b> %s</p>
            <p><b>Payment Status:</b> %s</p>

            <h3>Order Items</h3>
            <ul>%s</ul>

            <h3>Total: ₹%.2f</h3>

            <p><b>Delivery Address:</b> %s</p>

            <p style="background:#fff4d6;padding:12px;">
              This is a demo/test payment confirmation.
              No real money was transferred.
            </p>
            """.formatted(
                customerName(order),
                order.getOrderNumber(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                items,
                order.getTotalAmount() != null
                        ? order.getTotalAmount() : 0.0,
                order.getShippingAddress()
        );

        try {
            sendHtmlEmail(
                order.getCustomerEmail(),
                "OneCart - Order Confirmed: " + order.getOrderNumber(),
                emailLayout("Payment Successful!", content)
            );
        } catch (Exception e) {
            System.err.println("Order confirmation email failed: "
                    + e.getMessage());
        }
    }

    // 2. Order cancellation email
    public void sendOrderCancellationAndRefundEmail(
            Order order, Double refundAmount) {

        String content = """
            <p>Hello %s,</p>

            <p>Your order <b>%s</b> has been cancelled successfully.</p>

            <p><b>Order Amount:</b> ₹%.2f</p>

            <p><b>Refund Status:</b> Pending verification</p>

            <p>No refund has been confirmed as completed.
               We will notify you separately if a refund is processed.</p>
            """.formatted(
                customerName(order),
                order.getOrderNumber(),
                refundAmount != null ? refundAmount : 0.0
        );

        try {
            sendHtmlEmail(
                order.getCustomerEmail(),
                "OneCart - Order Cancelled: " + order.getOrderNumber(),
                emailLayout("Order Cancelled", content)
            );
        } catch (Exception e) {
            System.err.println("Cancellation email failed: "
                    + e.getMessage());
        }
    }

    // 3. Return or exchange request email
    public void sendReturnOrExchangeEmail(
            Order order, String requestType, String reason) {

        String content = """
            <p>Hello %s,</p>

            <p>We have received your %s request.</p>

            <p><b>Order Number:</b> %s</p>
            <p><b>Reason:</b> %s</p>
            <p><b>Status:</b> Request received and pending review.</p>

            <p>We will notify you when your request is reviewed.</p>
            """.formatted(
                customerName(order),
                requestType,
                order.getOrderNumber(),
                reason != null ? reason : "Not specified"
        );

        try {
            sendHtmlEmail(
                order.getCustomerEmail(),
                "OneCart - " + requestType + " Request Received",
                emailLayout("Request Received", content)
            );
        } catch (Exception e) {
            System.err.println("Return email failed: " + e.getMessage());
        }
    }
}
