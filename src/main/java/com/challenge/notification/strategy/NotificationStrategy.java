package com.challenge.notification.strategy;

import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.User;

public interface NotificationStrategy {
    void send(User user, String message);
    Channel getChannel();
}
