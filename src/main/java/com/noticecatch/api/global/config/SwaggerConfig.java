package com.noticecatch.api.global.config;

import com.noticecatch.api.global.resolver.CurrentUserId;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    static {
        // @CurrentUserId 어노테이션이 붙은 파라미터를 Swagger 문서에서 숨김
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUserId.class);
    }

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "BearerAuth";

        // API 요청 시 적용할 SecurityRequirement 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // SecurityScheme 정의 (Authorization: Bearer <JWT> 헤더 형태)
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        // API 문서 기본 정보 설정
        Info info = new Info()
                .title("공지캐치 (Notice-Catch) API 명세서")
                .description("대학 공지사항 알림 서비스 '공지캐치'의 백엔드 API 명세서입니다.")
                .version("v1.0.0");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}