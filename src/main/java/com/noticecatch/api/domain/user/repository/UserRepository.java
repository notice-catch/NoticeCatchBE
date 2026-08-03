package com.noticecatch.api.domain.user.repository;

import com.noticecatch.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 신규 공지 알림(키워드/카테고리) 배치 대상 — 해당 대학 소속이면서 전체 알림을 켜둔 유저.
    // 키워드/카테고리 세부 플래그는 후보군이 정해진 뒤 서비스에서 대조한다.
    List<User> findByDepartment_University_IdAndAllNotificationTrue(Long universityId);
}
