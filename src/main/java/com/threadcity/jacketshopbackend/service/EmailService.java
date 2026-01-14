package com.threadcity.jacketshopbackend.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final SendGrid  sendgrid;

    @Value("${sendgrid.from.email}")
    private String defaultFromEmail;

    @Value("${sendgrid.from.name}")
    private String defaultFromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public Response sendHtmlEmail(String to, String subject, String htmlContent) throws IOException {
        return sendHtmlEmail(defaultFromEmail, defaultFromName, to, subject, htmlContent);
    }

    public Response sendHtmlEmail(String fromEmail, String fromName,
                                  String to, String subject, String htmlContent) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, subject, toEmail, content);

        return sendMail(mail);
    }

    public Response sendPasswordResetEmail(String to, String resetToken) throws IOException {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        String subject = "Đặt lại mật khẩu - ThreadCity";
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Đặt lại mật khẩu</h2>
                <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản ThreadCity của mình.</p>
                <p>Nhấn vào nút bên dưới để đặt lại mật khẩu:</p>
                <br>
                <a href="%s" style="background-color: #4CAF50; color: white; padding: 12px 24px;
                   text-decoration: none; border-radius: 4px; display: inline-block;">
                    Đặt lại mật khẩu
                </a>
                <br><br>
                <p style="color: #666;">
                    Hoặc copy link này vào trình duyệt:<br>
                    <a href="%s">%s</a>
                </p>
                <p style="color: #666; font-size: 12px;">
                    Link này sẽ hết hạn sau 1 giờ.<br>
                    Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                </p>
                <hr>
                <p style="font-size: 12px; color: #888;">
                    © 2026 ThreadCity. All rights reserved.
                </p>
            </body>
            </html>
            """, resetUrl, resetUrl, resetUrl);

        return sendHtmlEmail(to, subject, htmlContent);
    }

    private Response sendMail(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendgrid.api(request);

        log.info("SendGrid Response - Status: {}, Body: {}",
                response.getStatusCode(), response.getBody());

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            log.info("Email sent successfully!");
        } else {
            log.error("Failed to send email. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
        }

        return response;
    }
}
