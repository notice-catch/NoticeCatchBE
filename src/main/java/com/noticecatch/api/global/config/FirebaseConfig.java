package com.noticecatch.api.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * FIREBASE_CREDENTIALS_BASE64가 설정되지 않은 환경(예: 로컬 개발)에서는
     * FCM 초기화를 건너뛰고 null 빈을 등록한다 — 실제 발송 로직에서 사용 전 null 체크 필요.
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (credentialsBase64 == null || credentialsBase64.isBlank()) {
            log.warn("firebase.credentials.base64(FIREBASE_CREDENTIALS_BASE64)가 설정되지 않아 FCM이 비활성화됩니다.");
            return null;
        }

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
