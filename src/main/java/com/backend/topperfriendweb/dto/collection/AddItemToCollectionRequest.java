package com.backend.topperfriendweb.dto.collection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddItemToCollectionRequest {
    @NotBlank(message = "Item type is required")
    private String itemType;
    
    @NotNull(message = "Item ID is required")
    private Long itemId;
}
