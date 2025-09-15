package com.backend.topperfriendweb.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateNoteRequest {
    private Long userId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "PDF link is required")
    private String pdfLink;
    
    private List<String> tags;
    
    @NotNull(message = "PDF option is required")
    private String pdfOption;
}