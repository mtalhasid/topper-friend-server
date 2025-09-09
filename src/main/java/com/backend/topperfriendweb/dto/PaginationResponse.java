// src/main/java/com/backend/topperfriendweb/dto/PaginationResponse.java
package com.backend.topperfriendweb.dto;

import java.util.List;

public class PaginationResponse<T> {
    private List<T> notes;
    private PaginationInfo pagination;
    private List<String> tags;

    // Getters and Setters
    public List<T> getNotes() {
        return notes;
    }

    public void setNotes(List<T> notes) {
        this.notes = notes;
    }

    public PaginationInfo getPagination() {
        return pagination;
    }

    public void setPagination(PaginationInfo pagination) {
        this.pagination = pagination;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer limit;
        private Integer totalPages;

        // Getters and Setters
        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }
    }
}