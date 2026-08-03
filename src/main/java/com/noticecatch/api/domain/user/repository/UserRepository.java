package com.noticecatch.api.domain.user.repository;

import com.noticecatch.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 키워드 알림 배치 대상 — 해당 대학 소속이면서 전체/키워드 알림을 켜둔 유저
    List<User> findByDepartment_University_IdAndAllNotificationTrueAndKeywordNotificationTrue(Long universityId);
}
