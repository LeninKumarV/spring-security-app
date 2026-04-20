package com.example.security.security_app.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Email
    private String email;

    @Size(min = 1, max = 50)
    private String firstName;

    @Size(min = 1, max = 50)
    private String lastName;
}