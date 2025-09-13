package com.backend.topperfriendweb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCollectionRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
}
