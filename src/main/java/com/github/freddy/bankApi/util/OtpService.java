package com.github.freddy.bankApi.util;

import java.security.SecureRandom;

public class OtpService {

    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    public String generateOtp() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));  // 0-9
        }
        return otp.toString();  // ex: "483920"
    }
}