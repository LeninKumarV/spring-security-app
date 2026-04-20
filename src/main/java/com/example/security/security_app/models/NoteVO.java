package com.example.security.security_app.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteVO {

    private UUID id;
    private String content;
    private String ownerUsername;
}
