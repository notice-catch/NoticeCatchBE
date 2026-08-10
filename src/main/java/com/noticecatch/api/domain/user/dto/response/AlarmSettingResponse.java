package com.noticecatch.api.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.noticecatch.api.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlarmSettingResponse {

    @Getter(AccessLevel.NONE)
    private final boolean isAll;

    @Getter(AccessLevel.NONE)
    private final boolean isClosing;

    @Getter(AccessLevel.NONE)
    private final boolean isKeyword;

    private final boolean scholarship;
    private final boolean extracurricular;
    private final boolean academic;
    private final boolean employment;

    // Lombok이 boolean isXxx 필드에 대해 생성하는 getter(isXxx())를 Jackson이
    // "isXxx"/"xxx" 두 프로퍼티로 중복 인식하는 걸 막기 위해 getter를 직접 선언하고
    // @JsonProperty를 getter에만 붙인다 (필드에 붙이면 여전히 중복 발생).
    @JsonProperty("isAll")
    public boolean isAll() {
        return isAll;
    }

    @JsonProperty("isClosing")
    public boolean isClosing() {
        return isClosing;
    }

    @JsonProperty("isKeyword")
    public boolean isKeyword() {
        return isKeyword;
    }

    public static AlarmSettingResponse from(User user) {
        return new AlarmSettingResponse(
                Boolean.TRUE.equals(user.getAllNotification()),
                Boolean.TRUE.equals(user.getClosingNotification()),
                Boolean.TRUE.equals(user.getKeywordNotification()),
                Boolean.TRUE.equals(user.getScholarship()),
                Boolean.TRUE.equals(user.getExtracurricular()),
                Boolean.TRUE.equals(user.getAcademic()),
                Boolean.TRUE.equals(user.getEmployment())
        );
    }
}
