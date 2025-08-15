package com.fivlo.fivlo_backend.domain.user.repository;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 리포지토리
 * User 엔티티에 대한 데이터베이스 접근을 담당
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * @param email 사용자 이메일
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 소셜 ID와 제공자로 사용자 조회
     * @param socialId 소셜 로그인 고유 ID
     * @param socialProvider 소셜 로그인 제공자
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findBySocialIdAndSocialProvider(String socialId, User.SocialProvider socialProvider);

    /**
     * 이메일 존재 여부 확인
     * @param email 사용자 이메일
     * @return 존재 여부 (boolean)
     */
    boolean existsByEmail(String email);

    /**
     * 소셜 ID와 제공자로 사용자 존재 여부 확인
     * @param socialId 소셜 로그인 고유 ID
     * @param socialProvider 소셜 로그인 제공자
     * @return 존재 여부 (boolean)
     */
    boolean existsBySocialIdAndSocialProvider(String socialId, User.SocialProvider socialProvider);

    /**
     * 프리미엄 사용자 목록 조회
     * @return 프리미엄 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.isPremium = true")
    java.util.List<User> findAllPremiumUsers();

    /**
     * 특정 날짜에 포모도로 코인을 지급받지 않은 프리미엄 사용자 조회
     * @param date 확인할 날짜
     * @return 해당 조건의 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.isPremium = true AND (u.lastPomodoroCoinDate IS NULL OR u.lastPomodoroCoinDate < :date)")
    java.util.List<User> findPremiumUsersWithoutPomodoroCoinOnDate(@Param("date") LocalDate date);

    /**
     * 총 코인이 특정 금액 이상인 사용자 조회
     * @param minCoins 최소 코인 수량
     * @return 해당 조건의 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.totalCoins >= :minCoins ORDER BY u.totalCoins DESC")
    java.util.List<User> findUsersWithMinCoins(@Param("minCoins") Integer minCoins);
}
