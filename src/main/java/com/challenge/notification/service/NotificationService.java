package com.challenge.notification.service;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.NotificationLog;
import com.challenge.notification.domain.User;
import com.challenge.notification.repository.NotificationLogRepository;
import com.challenge.notification.repository.UserRepository;
import com.challenge.notification.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationLogRepository logRepository;
    private final List<NotificationStrategy> strategies;

    @Transactional
    public void notifyUsers(Category category, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        List<User> eligibleUsers = userRepository.findBySubscribedCategory(category);
        Map<Channel, NotificationStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(NotificationStrategy::getChannel, s -> s));

        for (User user : eligibleUsers) {
            for (Channel channel : user.getChannels()) {
                NotificationStrategy strategy = strategyMap.get(channel);
                if (strategy != null) {
                    strategy.send(user, message);
                    saveLog(user, category, channel, message);
                }
            }
        }
    }

    private void saveLog(User user, Category category, Channel channel, String message) {
        NotificationLog log = NotificationLog.builder()
                .userName(user.getName())
                .userEmail(user.getEmail())
                .category(category)
                .channel(channel)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }

    public List<NotificationLog> getHistory() {
        return logRepository.findAllByOrderByTimestampDesc();
    }

    public Page<NotificationLog> getHistory(Pageable pageable) {
        return logRepository.findAllByOrderByTimestampDesc(pageable);
    }
}
