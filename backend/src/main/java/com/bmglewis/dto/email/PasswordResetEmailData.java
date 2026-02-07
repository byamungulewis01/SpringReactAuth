package com.bmglewis.dto.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetEmailData {
    private String userName;
    private String resetUrl;
    private String expiryMinutes;
    private String supportEmail;
}