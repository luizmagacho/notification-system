package com.challenge.notification.config;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.User;
import com.challenge.notification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User user1 = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .phoneNumber("+123456789")
                    .subscribedCategories(Set.of(Category.SPORTS, Category.MOVIES))
                    .channels(Set.of(Channel.EMAIL, Channel.SMS))
                    .build();

            User user2 = User.builder()
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .phoneNumber("+987654321")
                    .subscribedCategories(Set.of(Category.FINANCE))
                    .channels(Set.of(Channel.EMAIL, Channel.PUSH))
                    .build();

            User user3 = User.builder()
                    .name("Bob Wilson")
                    .email("bob@example.com")
                    .phoneNumber("+555555555")
                    .subscribedCategories(Set.of(Category.SPORTS, Category.FINANCE, Category.MOVIES))
                    .channels(Set.of(Channel.PUSH))
                    .build();

            userRepository.saveAll(Set.of(user1, user2, user3));
        }
    }
}
