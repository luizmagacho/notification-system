package com.challenge.notification.service;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.User;
import com.challenge.notification.repository.NotificationLogRepository;
import com.challenge.notification.repository.UserRepository;
import com.challenge.notification.strategy.NotificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationLogRepository logRepository;

    @Mock
    private NotificationStrategy emailStrategy;

    @Mock
    private NotificationStrategy smsStrategy;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        when(emailStrategy.getChannel()).thenReturn(Channel.EMAIL);
        when(smsStrategy.getChannel()).thenReturn(Channel.SMS);
        
        notificationService = new NotificationService(
                userRepository, 
                logRepository, 
                List.of(emailStrategy, smsStrategy)
        );
    }

    @Test
    void shouldNotifyUsersWithSubscribedCategoryAndChannels() {
        // Arrange
        User user = User.builder()
                .name("John")
                .email("john@example.com")
                .subscribedCategories(Set.of(Category.SPORTS))
                .channels(Set.of(Channel.EMAIL, Channel.SMS))
                .build();

        when(userRepository.findBySubscribedCategory(Category.SPORTS)).thenReturn(List.of(user));

        // Act
        notificationService.notifyUsers(Category.SPORTS, "Test Message");

        // Assert
        verify(emailStrategy, times(1)).send(eq(user), eq("Test Message"));
        verify(smsStrategy, times(1)).send(eq(user), eq("Test Message"));
        verify(logRepository, times(2)).save(any());
    }

    @Test
    void shouldNotNotifyIfUserNotSubscribedToCategory() {
        // Arrange
        when(userRepository.findBySubscribedCategory(Category.FINANCE)).thenReturn(List.of());

        // Act
        notificationService.notifyUsers(Category.FINANCE, "Test Message");

        // Assert
        verify(emailStrategy, never()).send(any(), any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionIfMessageIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
                notificationService.notifyUsers(Category.SPORTS, ""));
        assertThrows(IllegalArgumentException.class, () -> 
                notificationService.notifyUsers(Category.SPORTS, "   "));
    }
}
