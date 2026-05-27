package com.challenge.notification.service;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.Channel;
import com.challenge.notification.domain.NotificationLog;
import com.challenge.notification.domain.User;
import com.challenge.notification.repository.NotificationLogRepository;
import com.challenge.notification.repository.UserRepository;
import com.challenge.notification.strategy.NotificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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
        lenient().when(emailStrategy.getChannel()).thenReturn(Channel.EMAIL);
        lenient().when(smsStrategy.getChannel()).thenReturn(Channel.SMS);
        
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

    @Test
    void shouldThrowExceptionIfMessageIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                notificationService.notifyUsers(Category.SPORTS, null));
    }

    @Test
    void getHistory_shouldReturnPaginatedResults() {
        // Arrange
        NotificationLog log = NotificationLog.builder()
                .id(1L)
                .userName("John")
                .userEmail("john@example.com")
                .category(Category.SPORTS)
                .channel(Channel.EMAIL)
                .message("Test")
                .timestamp(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificationLog> expectedPage = new PageImpl<>(List.of(log), pageable, 1);
        when(logRepository.findAllByOrderByTimestampDesc(pageable)).thenReturn(expectedPage);

        // Act
        Page<NotificationLog> result = notificationService.getHistory(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getUserName());
        verify(logRepository).findAllByOrderByTimestampDesc(pageable);
    }

    @Test
    void getHistory_withEmptyResults_shouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificationLog> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(logRepository.findAllByOrderByTimestampDesc(pageable)).thenReturn(emptyPage);

        // Act
        Page<NotificationLog> result = notificationService.getHistory(pageable);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
