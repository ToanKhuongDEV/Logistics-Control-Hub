package com.logistics.hub.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MailConfigDebugRunner implements CommandLineRunner {

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:}")
    private String mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Override
    public void run(String... args) {
        log.info("Mail config debug -> host: {}, port: {}, username: {}, passwordLength: {}",
                mailHost,
                mailPort,
                mask(mailUsername),
                mailPassword == null ? 0 : mailPassword.length());
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }
}
