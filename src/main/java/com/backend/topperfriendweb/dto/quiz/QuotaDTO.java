package com.backend.topperfriendweb.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotaDTO {
    private int limit;
    private int used;
    private int remaining;
    private LocalDateTime resetAt; // when usage drops below limit
}
