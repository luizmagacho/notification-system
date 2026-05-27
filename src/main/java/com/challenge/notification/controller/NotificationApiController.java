package com.challenge.notification.controller;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.NotificationLog;
import com.challenge.notification.service.NotificationService;
import com.challenge.notification.service.SseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationApiController {

    private final NotificationService notificationService;
    private final SseService sseService;

    @GetMapping("/categories")
    public ResponseEntity<Category[]> getCategories() {
        return ResponseEntity.ok(Category.values());
    }

    @GetMapping("/history")
    public ResponseEntity<Page<NotificationLog>> getHistory(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getHistory(pageable));
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> notifyUsers(@Valid @RequestBody NotificationRequest request) {
        notificationService.notifyUsers(request.getCategory(), request.getMessage());
        sseService.broadcast(notificationService.getHistory());
        return ResponseEntity.ok(Map.of("message", "Notifications sent successfully!"));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseService.register();
    }
}
