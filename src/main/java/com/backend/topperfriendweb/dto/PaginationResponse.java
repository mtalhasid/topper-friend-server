// src/main/java/com/backend/topperfriendweb/dto/PaginationResponse.java
package com.backend.topperfriendweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    private List<T> notes;
    private PaginationInfo pagination;
    private List<String> tags;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer limit;
        private Integer totalPages;
    }
}