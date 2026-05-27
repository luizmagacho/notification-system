package com.challenge.notification.repository;

import com.challenge.notification.domain.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findAllByOrderByTimestampDesc();

    Page<NotificationLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
