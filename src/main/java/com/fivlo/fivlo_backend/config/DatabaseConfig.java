package com.fivlo.fivlo_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 데이터베이스 설정 클래스
 * JPA, PostGIS 및 데이터베이스 관련 추가 설정
 */
@Configuration
@EnableJpaAuditing  // JPA Auditing 기능 활성화 (생성일, 수정일 자동 관리)
@EnableJpaRepositories(basePackages = "com.fivlo.fivlo_backend.domain")
public class DatabaseConfig {

    /**
     * PostGIS 관련 설정은 application.properties에서 처리
     * spring.jpa.properties.hibernate.dialect=org.hibernate.spatial.dialect.postgis.PostgisPG10Dialect
     * 
     * 추가적인 데이터베이스 설정이 필요한 경우 여기에 Bean으로 정의
     */
}