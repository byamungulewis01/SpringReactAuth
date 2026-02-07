package com.bmglewis.dto.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailContext {
    private String recipientName;
    private String recipientEmail;
    private String subject;
    private Object templateData;
}