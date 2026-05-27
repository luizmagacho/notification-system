package com.challenge.notification.controller;

import com.challenge.notification.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotNull(message = "Category is required")
    private Category category;

    @NotBlank(message = "Message cannot be blank")
    private String message;
}
