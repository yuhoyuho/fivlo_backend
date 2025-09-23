package com.fivlo.fivlo_backend.domain.user.service;

import com.fivlo.fivlo_backend.domain.user.auth.dto.ReissueRequestDto;
import com.fivlo.fivlo_backend.domain.user.auth.dto.TokenResponseDto;
import com.fivlo.fivlo_backend.domain.user.auth.entity.RefreshEntity;
import com.fivlo.fivlo_backend.domain.user.auth.repository.RefreshRepository;
import com.fivlo.fivlo_backend.domain.user.dto.*;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import com.fivlo.fivlo_backend.security.CustomUserDetailsService;
import com.fivlo.fivlo_backend.security.JwtTokenProvider;
import com.fivlo.fivlo_backend.security.oauth2.OAuth2TokenVerifier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final List<OAuth2TokenVerifier> tokenVerifiers;
    private final CoinTransactionService coinTransactionService;
    private final RefreshRepository refreshRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * 이메일 회원가입 로직
     * @param dto
     * @return
     */
    @Transactional
    public JoinUserResponse join(JoinUserRequest dto) {

        // 이메일 중복 검증
        Optional<User> find = userRepository.findByEmail(dto.email());
        if (find.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다. email : " +  dto.email());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.password());

        // 닉네임 설정 (초기값 : email + random uuid)
        String nickname = "사용자_" + UUID.randomUUID();

        // 사용자 저장
        User user = User.builder()
                .email(dto.email())
                .password(encodedPassword)
                .nickname(nickname)
                .alarmStatus(true)
                .status(User.Status.ACTIVE)
                .isPremium(true)
                .build();

        User savedUser = userRepository.save(user);

        // jwt access 발급
        String access = jwtTokenProvider.generateAccessToken(savedUser.getId());

        // jwt refresh 발급
        String refresh = jwtTokenProvider.generateRefreshToken(savedUser.getId());

        // Redis에 refresh 토큰 저장
        RefreshEntity redisToken = new RefreshEntity(savedUser.getId(), refresh);
        refreshRepository.save(redisToken);

        return new JoinUserResponse(access, refresh, savedUser.getId(), savedUser.getOnboardingType());
    }

    /**
     * 온보딩 생성/수정 로직
     * @param id
     * @param onboardingType
     * @return
     */
    @Transactional
    public User.OnboardingType updateOnboardingType(Long id, User.OnboardingType onboardingType) {

        // 아이디로 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID : " + id));

        // DB에 업데이트, 더티체킹으로 자동 저장
        user.updateOnboardingType(onboardingType);

        return user.getOnboardingType();
    }

    /**
     * 언어 설정 로직
     */
    @Transactional
    public User.Language updateLanguage(Long id, User.Language language) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID : " + id));

        user.updateLanguage(language);

        return user.getLanguage();
    }

    /**
     * 알람 on/off 상태 변경
     */
    @Transactional
    public String updateAlarmStatus(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID : " + id));

        user.updateAlarmStatus();

        return "알림 상태 변경 완료! (현재 상태 : " + user.getAlarmStatus() + ")";
    }

    /**
     * 사용자 정보 조회 로직
     * @param userDetails
     * @return
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(CustomUserDetails userDetails) {

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. Email : " + userDetails.getUsername() ));

        return new UserInfoResponse(user.getId(), user.getNickname(), user.getProfileImageUrl(), user.getOnboardingType(), user.getIsPremium(), user.getTotalCoins(), user.getAlarmStatus());
    }

    /**
     * 사용자 정보 수정 로직
     * @param userDetails
     * @param dto
     * @return
     */
    @Transactional
    public String updateUserInfo(CustomUserDetails userDetails, UpdateUserRequest dto) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. Email : " + userDetails.getUsername()));

        user.updateProfile(dto.nickname(), dto.profileImageUrl());
        return "프로필 정보가 성공적으로 수정되었습니다.";
    }

    /**
     * 사용자 탈퇴 로직
     */
    @Transactional
    public String deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID : " + id));

        user.deactivate();
        return "회원 탈퇴 요청이 성공적으로 처리되었습니다.";
    }

    /**
     * 소셜 로그인 처리 로직
     * @param dto
     * @return
     */
    @Transactional
    public SocialLoginResponse socialLogin(@Valid SocialLoginRequest dto) {

        // 소셜 로그인 제공자에 맞는 토큰 검증 방식 찾기
        OAuth2TokenVerifier verifier = tokenVerifiers.stream()
                .filter(v -> v.supports(dto.provider()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 로그인 방식입니다."));

        // 토큰 검증 및 사용자 정보 획득
        User user = verifier.verifyAndGetOrCreate(dto.token());

        // 계정 활성화 여부 검증 및 복구
        if(user.getStatus() == User.Status.DEACTIVATED) {
            if(user.getDeactivatedAt() != null && user.getDeactivatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
                logger.warn("{}일이 지나 계정을 복구할 수 없습니다.", user.getDeactivatedAt());
                throw new LockedException("복구 기간이 만료된 계정입니다.");
            }
            else {
                user.restore();
            }
        }
        else if(user.getStatus() == User.Status.DELETED) {
            logger.warn("Login attempt for a deleted account : {}", user.getEmail());
            throw new LockedException("삭제된 계정입니다.");
        }

        // 신규 사용자인지 검증
        boolean isNewUser = user.getOnboardingType() == null;

        // jwt access 발급
        String access = jwtTokenProvider.generateAccessToken(user.getId());

        // jwt refresh 발급
        String refresh = jwtTokenProvider.generateRefreshToken(user.getId());

        // Redis에 refresh 토큰 저장
        RefreshEntity redisToken = new RefreshEntity(user.getId(), refresh);
        refreshRepository.save(redisToken);

        return new SocialLoginResponse(isNewUser, access, refresh, user.getId(), user.getOnboardingType());
    }

    /**
     * 출석 시 코인 지급 처리 로직
     */
    @Transactional
    public String checkAttendanceAndReward(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();
        LocalDate lastAttendance = user.getLastAttendanceCoinDate();

        // 마지막 출석일과 today 비교
        if(lastAttendance != null && lastAttendance.isEqual(today)) {
            return "이미 출석 코인을 받았습니다.";
        }

        // 출석 보상으로 코인 지급 (프리미엄 회원만)
        if(user.getIsPremium()) {
            user.addCoins(1);
            coinTransactionService.logTransaction(user, 1);
            user.updateLastAttendanceCoinDate(today);
            return "출석을 하셨네요. 오분이가 코인을 드리겠습니다. 오늘도 파이!!";
        }
        else {
            return "프리미엄 회원만 코인을 받을 수 있습니다.";
        }
    }

    /**
     * Refresh 토큰 재발급 로직
     */
    @Transactional
    public TokenResponseDto reissue(ReissueRequestDto request) {
        // refresh 유효성 검사
        if(!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // access 토큰에서 userId 가져오기
        Long userId = jwtTokenProvider.getUserIdFromToken(request.accessToken());

        // Redis에서 userId 기반 refresh 조회
        RefreshEntity refreshToken = refreshRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그아웃된 사용자입니다."));

        // refresh 일치 검사
        if(!refreshToken.getToken().equals(request.refreshToken())) {
            throw new IllegalArgumentException("토큰 정보가 일치하지 않습니다.");
        }

        // 새로운 refresh 토큰 생성
        TokenResponseDto newTokenDto = jwtTokenProvider.generateTokenDto(userId);

        // Redis 업데이트
        RefreshEntity newRefreshToken = new RefreshEntity(userId, newTokenDto.refresh());
        refreshRepository.save(newRefreshToken);

        return newTokenDto;
    }
}
