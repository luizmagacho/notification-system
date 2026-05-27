package com.challenge.notification.controller;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.NotificationLog;
import com.challenge.notification.service.NotificationService;
import com.challenge.notification.service.SseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationApiController.class)
class NotificationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private SseService sseService;


    // ==================== POST /api/notifications/send ====================

    @Test
    void send_withValidRequest_shouldReturn200() throws Exception {
        String json = """
                { "category": "SPORTS", "message": "Breaking news!" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notifications sent successfully!"));

        verify(notificationService).notifyUsers(eq(Category.SPORTS), eq("Breaking news!"));
    }

    @Test
    void send_withBlankMessage_shouldReturn400() throws Exception {
        String json = """
                { "category": "SPORTS", "message": "" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.message").value("Message cannot be blank"));

        verify(notificationService, never()).notifyUsers(any(), any());
    }

    @Test
    void send_withNullCategory_shouldReturn400() throws Exception {
        String json = """
                { "message": "Test message" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.category").value("Category is required"));

        verify(notificationService, never()).notifyUsers(any(), any());
    }

    @Test
    void send_withNullMessage_shouldReturn400() throws Exception {
        String json = """
                { "category": "SPORTS" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.message").value("Message cannot be blank"));

        verify(notificationService, never()).notifyUsers(any(), any());
    }

    @Test
    void send_withInvalidCategory_shouldReturn400() throws Exception {
        String json = """
                { "category": "INVALID_CATEGORY", "message": "Test" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed Request"));

        verify(notificationService, never()).notifyUsers(any(), any());
    }

    @Test
    void send_withMalformedJson_shouldReturn400() throws Exception {
        String json = "{ invalid json }";

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed Request"));

        verify(notificationService, never()).notifyUsers(any(), any());
    }

    @Test
    void send_whenServiceThrowsIllegalArgument_shouldReturn400() throws Exception {
        doThrow(new IllegalArgumentException("Message cannot be empty"))
                .when(notificationService).notifyUsers(any(), any());

        String json = """
                { "category": "SPORTS", "message": "  " }
                """;

        // @NotBlank catches whitespace-only strings, but testing service-level validation too
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/notifications/history ====================

    @Test
    void history_shouldReturnPaginatedResults() throws Exception {
        NotificationLog log = NotificationLog.builder()
                .id(1L)
                .userName("John")
                .userEmail("john@example.com")
                .category(Category.SPORTS)
                .channel(Channel.EMAIL)
                .message("Test message")
                .timestamp(LocalDateTime.of(2026, 5, 26, 10, 0, 0))
                .build();

        PageImpl<NotificationLog> page = new PageImpl<>(
                List.of(log), PageRequest.of(0, 20), 1);
        when(notificationService.getHistory(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/notifications/history")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userName").value("John"))
                .andExpect(jsonPath("$.content[0].category").value("SPORTS"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void history_withDefaultPagination_shouldReturn200() throws Exception {
        PageImpl<NotificationLog> page = new PageImpl<>(
                List.of(), PageRequest.of(0, 20), 0);
        when(notificationService.getHistory(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/notifications/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ==================== GET /api/notifications/categories ====================

    @Test
    void categories_shouldReturnAllCategories() throws Exception {
        mockMvc.perform(get("/api/notifications/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(Category.values().length))
                .andExpect(jsonPath("$[0]").value("SPORTS"))
                .andExpect(jsonPath("$[1]").value("FINANCE"))
                .andExpect(jsonPath("$[2]").value("MOVIES"));
    }
}
