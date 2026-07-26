package com.noticecatch.api.global.resolver;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * 인증된 요청의 유저 ID를 컨트롤러 파라미터로 주입받기 위한 어노테이션.
 * 회원(인증) 팀의 JWT 필터가 완성되면 {@link CurrentUserIdArgumentResolver} 내부 구현만
 * SecurityContext 기반으로 교체하면 되고, 이 어노테이션을 사용하는 컨트롤러 코드는 변경할 필요가 없다.
 */
@Parameter(hidden = true)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
public @interface CurrentUserId {
}
