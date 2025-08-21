package com.fivlo.fivlo_backend.domain.user.service;

import com.fivlo.fivlo_backend.domain.user.dto.*;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import com.fivlo.fivlo_backend.security.JwtTokenProvider;
import com.fivlo.fivlo_backend.security.oauth2.OAuth2TokenVerifier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
                .isPremium(true)
                .build();

        User savedUser = userRepository.save(user);

        // jwt 발급
        String token = jwtTokenProvider.generateToken(savedUser.getId());

        return new JoinUserResponse(token, savedUser.getId(), savedUser.getOnboardingType());
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
     * 사용자 정보 조회 로직
     * @param userDetails
     * @return
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(CustomUserDetails userDetails) {

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. Email : " + userDetails.getUsername() ));

        return new UserInfoResponse(user.getId(), user.getNickname(), user.getProfileImageUrl(), user.getOnboardingType(), user.getIsPremium(), user.getTotalCoins());
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

        // 신규 사용자인지 검증
        boolean isNewUser = user.getOnboardingType() == null;

        // jwt 발급
        String token = jwtTokenProvider.generateToken(user.getId());

        return new SocialLoginResponse(isNewUser, token, user.getId(), user.getOnboardingType());
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
}
