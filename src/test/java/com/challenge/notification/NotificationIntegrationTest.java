package com.challenge.notification;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.NotificationLog;
import com.challenge.notification.domain.User;
import com.challenge.notification.repository.NotificationLogRepository;
import com.challenge.notification.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationLogRepository logRepository;


    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .name("Integration Test User")
                .email("integration@example.com")
                .phoneNumber("+111111111")
                .subscribedCategories(Set.of(Category.SPORTS))
                .channels(Set.of(Channel.EMAIL, Channel.SMS))
                .build();
        userRepository.save(user);
    }

    @Test
    void fullFlow_sendAndRetrieveHistory() throws Exception {
        // Step 1: Send a notification
        String sendJson = """
                { "category": "SPORTS", "message": "Goal scored!" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notifications sent successfully!"));

        // Step 2: Verify it appears in history (user has EMAIL + SMS channels = 2 logs)
        mockMvc.perform(get("/api/notifications/history")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].message").value("Goal scored!"))
                .andExpect(jsonPath("$.content[0].category").value("SPORTS"));
    }

    @Test
    void send_withBlankMessage_shouldReturn400WithValidationError() throws Exception {
        String json = """
                { "category": "SPORTS", "message": "" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    @Test
    void send_withMissingCategory_shouldReturn400() throws Exception {
        String json = """
                { "message": "Test message" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").exists());
    }

    @Test
    void send_withInvalidEnumValue_shouldReturn400() throws Exception {
        String json = """
                { "category": "NONEXISTENT", "message": "Test" }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed Request"));
    }

    @Test
    void history_withPagination_shouldReturnCorrectPage() throws Exception {
        // Send multiple notifications to create more than one page
        for (int i = 0; i < 5; i++) {
            String json = String.format("""
                    { "category": "SPORTS", "message": "Message %d" }
                    """, i);

            mockMvc.perform(post("/api/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        // 5 messages × 2 channels = 10 logs. Request page of size 3
        mockMvc.perform(get("/api/notifications/history")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(4));

        // Last page should have 1 element
        mockMvc.perform(get("/api/notifications/history")
                        .param("page", "3")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void categories_shouldReturnAllValues() throws Exception {
        mockMvc.perform(get("/api/notifications/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(Category.values().length)));
    }
}
