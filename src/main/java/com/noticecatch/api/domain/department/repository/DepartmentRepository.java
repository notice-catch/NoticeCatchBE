package com.noticecatch.api.domain.department.repository;

import com.noticecatch.api.domain.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // 대학교 ID에 속한 전체 학과 수 확인용
    boolean existsByUniversityId(Long universityId);

    // 대학교 ID 및 키워드 조건에 맞는 학과 목록 조회
    List<Department> findByUniversityIdAndNameContaining(Long universityId, String name);

    // 대학교 ID에 속한 전체 학과 목록 조회
    List<Department> findByUniversityId(Long universityId);
}
