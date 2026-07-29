package com.noticecatch.api.global.resolver;

import com.noticecatch.api.global.apiPayload.code.GeneralErrorCode;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code JwtAuthenticationFilter}가 SecurityContext에 채워둔 인증 정보(principal = userId)를 꺼내
 * {@code @CurrentUserId}가 붙은 컨트롤러 파라미터에 주입한다.
 */
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TEMP_USER_ID_HEADER = "X-USER-ID";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ProjectException(GeneralErrorCode.UNAUTHORIZED);
        }
        try {
            return (Long) authentication.getPrincipal();
        } catch (ClassCastException e) {
            throw new ProjectException(GeneralErrorCode.UNAUTHORIZED);
        }
    }
}
