package com.challenge.notification.repository;

import com.challenge.notification.domain.Category;
import com.challenge.notification.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN u.subscribedCategories c WHERE c = :category")
    List<User> findBySubscribedCategory(@Param("category") Category category);
}
