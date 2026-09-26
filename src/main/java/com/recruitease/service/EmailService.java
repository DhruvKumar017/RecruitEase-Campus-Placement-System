package com.recruitease.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("recruitease.campus.system@gmail.com");

        message.setTo(email);
        message.setSubject("RecruitEase - Password Reset OTP");

        message.setText(
                "Hello Student,\n\n" +
                "Your RecruitEase password reset OTP is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "Do not share this OTP with anyone.\n\n" +
                "Regards,\nRecruitEase Placement Cell"
        );

       try {
    mailSender.send(message);
} catch (Exception e) {
    System.err.println("===== RECRUITEASE MAIL ERROR =====");
    e.printStackTrace();
    throw e;

    }
}
}