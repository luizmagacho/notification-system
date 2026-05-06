package com.challenge.notification.controller;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.NotificationLog;
import com.challenge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For development convenience
public class NotificationApiController {

    private final NotificationService notificationService;

    @GetMapping("/categories")
    public ResponseEntity<Category[]> getCategories() {
        return ResponseEntity.ok(Category.values());
    }

    @GetMapping("/history")
    public ResponseEntity<List<NotificationLog>> getHistory() {
        return ResponseEntity.ok(notificationService.getHistory());
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> notifyUsers(@RequestBody NotificationRequest request) {
        try {
            notificationService.notifyUsers(request.getCategory(), request.getMessage());
            return ResponseEntity.ok(Map.of("message", "Notifications sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @lombok.Data
    public static class NotificationRequest {
        private Category category;
        private String message;
    }
}
