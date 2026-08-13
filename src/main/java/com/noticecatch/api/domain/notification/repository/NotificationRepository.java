package com.noticecatch.api.domain.notification.repository;

import com.noticecatch.api.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Slice<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 벌크 업데이트 후 영속성 컨텍스트에 남은 이전 isRead 값을 참조하지 않도록 clearAutomatically로 1차 캐시를 비운다
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}