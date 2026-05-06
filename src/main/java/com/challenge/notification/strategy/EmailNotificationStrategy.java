package com.challenge.notification.strategy;

import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationStrategy implements NotificationStrategy {
    @Override
    public void send(User user, String message) {
        log.info("Sending EMAIL to {}: {}", user.getEmail(), message);
    }

    @Override
    public Channel getChannel() {
        return Channel.EMAIL;
    }
}
