package com.backend.topperfriendweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, message, data);
    }

    public static <T> CommonResponse<T> success(String message) {
        return new CommonResponse<>(true, message, null);
    }

    public static <T> CommonResponse<T> error(String message) {
        return new CommonResponse<>(false, message, null);
    }
}