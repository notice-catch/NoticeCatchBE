package com.noticecatch.api.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.base64:}")
    private String credentialsBase64;

    // FIREBASE_CREDENTIALS_BASE64 미설정 시(예: 로컬 개발) 이 빈 자체가 등록되지 않는다.
    // 소비자(FcmSender)는 ObjectProvider<FirebaseMessaging>로 빈의 부재를 정상적으로 처리한다.
    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${firebase.credentials.base64:}')")
    public FirebaseMessaging firebaseMessaging() throws IOException {
        byte[] decodedCredentials = Base64.getDecoder().decode(credentialsBase64);
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedCredentials));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        FirebaseApp app = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(options)
                : FirebaseApp.getInstance();

        return FirebaseMessaging.getInstance(app);
    }
}
