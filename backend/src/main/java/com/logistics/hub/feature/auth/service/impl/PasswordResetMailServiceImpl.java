package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.auth.service.PasswordResetMailService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetMailServiceImpl implements PasswordResetMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetUrl) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new ValidationException("Chưa cấu hình email Gmail để gửi mật khẩu khôi phục");
        }

        try {
            log.info("Sending password reset email from {} to {}", fromEmail, toEmail);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Yeu cau dat lai mat khau - Logistics Control Hub");
            message.setText(buildContent(fullName, resetUrl));
            mailSender.send(message);
            log.info("Password reset email sent successfully to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {} using account {}. Root cause: {}", toEmail, fromEmail, ex.getMessage(), ex);
            throw new ValidationException("Không thể gửi email đặt lại mật khẩu", ex);
        }
    }

    private String buildContent(String fullName, String resetUrl) {
        String displayName = (fullName == null || fullName.isBlank()) ? "ban" : fullName;
        return "Xin chào " + displayName + ",\n\n"
                + "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản Logistics Control Hub của bạn.\n"
                + "Vui lòng mở liên kết dưới đây để đặt lại mật khẩu:\n"
                + resetUrl + "\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.\n\n"
                + "Trân trọng,\nLogistics Control Hub";
    }
}
