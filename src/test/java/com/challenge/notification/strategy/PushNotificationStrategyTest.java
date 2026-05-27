package com.challenge.notification.strategy;

import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PushNotificationStrategyTest {

    private final PushNotificationStrategy strategy = new PushNotificationStrategy();

    @Test
    void getChannel_shouldReturnPush() {
        assertEquals(Channel.PUSH, strategy.getChannel());
    }

    @Test
    void send_shouldNotThrow() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("+123456789")
                .subscribedCategories(Set.of())
                .channels(Set.of(Channel.PUSH))
                .build();

        assertDoesNotThrow(() -> strategy.send(user, "Hello via Push!"));
    }

    @Test
    void send_withNullName_shouldNotThrow() {
        User user = User.builder()
                .channels(Set.of(Channel.PUSH))
                .build();

        assertDoesNotThrow(() -> strategy.send(user, "Hello via Push!"));
    }
}
