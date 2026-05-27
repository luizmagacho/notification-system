package com.challenge.notification.strategy;

import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SmsNotificationStrategyTest {

    private final SmsNotificationStrategy strategy = new SmsNotificationStrategy();

    @Test
    void getChannel_shouldReturnSms() {
        assertEquals(Channel.SMS, strategy.getChannel());
    }

    @Test
    void send_shouldNotThrow() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("+123456789")
                .subscribedCategories(Set.of())
                .channels(Set.of(Channel.SMS))
                .build();

        assertDoesNotThrow(() -> strategy.send(user, "Hello via SMS!"));
    }

    @Test
    void send_withNullPhoneNumber_shouldNotThrow() {
        User user = User.builder()
                .name("Test User")
                .channels(Set.of(Channel.SMS))
                .build();

        assertDoesNotThrow(() -> strategy.send(user, "Hello via SMS!"));
    }
}
