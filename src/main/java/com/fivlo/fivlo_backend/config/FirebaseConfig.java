package com.fivlo.fivlo_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("google-services.json");

            if(serviceAccount == null) {
                throw new IOException("Firebase 키 파일 찾을 수 없습니다.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // FirebaseApp이 초기화되지 않았을 경우에만 초기화
            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch(IOException e) {
            throw new RuntimeException("Firebase 초기화 중 오류가 발생했습니다.");
        }
    }
}
