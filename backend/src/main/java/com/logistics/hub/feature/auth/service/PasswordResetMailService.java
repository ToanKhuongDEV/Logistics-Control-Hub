package com.logistics.hub.feature.auth.service;

public interface PasswordResetMailService {

    void sendPasswordResetEmail(String toEmail, String fullName, String resetUrl);
}
