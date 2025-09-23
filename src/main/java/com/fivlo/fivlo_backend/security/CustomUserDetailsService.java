package com.fivlo.fivlo_backend.security;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 커스텀 사용자 세부정보 서비스
 * Spring Security에서 사용자 인증 시 사용자 정보를 로드하는 서비스
 * UserRepository를 통해 실제 데이터베이스에서 사용자 정보를 조회
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    /**
     * 사용자명(이메일)으로 사용자 정보 로드
     * @param email 사용자 이메일
     * @return UserDetails 구현체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        User user = userOptional.get();
        logger.debug("User found: ID={}, Email={}, Premium={}", user.getId(), user.getEmail(), user.getIsPremium());

        // 계정 활성화 여부 검증 및 복구
        if(user.getStatus() == User.Status.DEACTIVATED) {
            if(user.getDeactivatedAt() != null && user.getDeactivatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
                logger.warn("{}일이 지나 계정을 복구할 수 없습니다.", user.getDeactivatedAt());
                throw new LockedException("복구 기간이 만료된 계정입니다.");
            }
            else {
                logger.info("계정을 복구했습니다. email: {}", email);
                user.restore();
            }
        }
        else if(user.getStatus() == User.Status.DELETED) {
            logger.warn("Login attempt for a deleted account : {}", email);
            throw new LockedException("삭제된 계정입니다.");
        }

        // User 엔티티로 CustomUserDetails 객체를 생성하여 반환
        return new CustomUserDetails(user);
    }

    /**
     * 사용자 ID로 UserDetails 로드 (JWT 토큰 검증 시 사용)
     * @param userId 사용자 ID
     * @return UserDetails 구현체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        logger.debug("Loading user by ID: {}", userId);

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            logger.warn("User not found with ID: {}", userId);
            throw new UsernameNotFoundException("User not found with ID: " + userId);
        }
        
        User user = userOptional.get();
        logger.debug("User found by ID: Email={}, Premium={}", user.getEmail(), user.getIsPremium());
        
        return new CustomUserDetails(user);
    }
}
