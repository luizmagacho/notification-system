package com.challenge.notification.strategy;

import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmailNotificationStrategyTest {

    private final EmailNotificationStrategy strategy = new EmailNotificationStrategy();

    @Test
    void getChannel_shouldReturnEmail() {
        assertEquals(Channel.EMAIL, strategy.getChannel());
    }

    @Test
    void send_shouldNotThrow() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("+123456789")
                .subscribedCategories(Set.of())
                .channels(Set.of(Channel.EMAIL))
                .build();

        assertDoesNotThrow(() -> strategy.send(user, "Hello!"));
    }

    @Test
    void send_withNullEmail_shouldNotThrow() {
        User user = User.builder()
                .name("Test User")
                .channels(Set.of(Channel.EMAIL))
                .build();

        assertDoesNotThrow(() -> strategy.send(user, "Hello!"));
    }
}
