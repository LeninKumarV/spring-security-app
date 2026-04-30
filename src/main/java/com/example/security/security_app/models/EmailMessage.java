package com.example.security.security_app.models;

import com.example.security.security_app.entity.EmailType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {

    private String    toEmail;
    private String    username;
    private String    token;
    private EmailType emailType;
    private String    requestId;
    private LocalDateTime createdAt;
    private String tempPassword;
    private String    invitedBy;
    private String    emailExpiry;
}