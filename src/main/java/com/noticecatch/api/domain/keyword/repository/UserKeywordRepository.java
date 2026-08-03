package com.noticecatch.api.domain.keyword.repository;

import com.noticecatch.api.domain.keyword.entity.UserKeyword;
import com.noticecatch.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findAllByUser(User user);

    // 알림 배치가 후보 유저 전원의 키워드를 한 번에 조회할 때 사용 (유저별 개별 조회 방지)
    List<UserKeyword> findAllByUserIn(List<User> users);

    long countByUser(User user);

    void deleteAllByUser(User user);
}
