package com.fivlo.fivlo_backend.domain.user.service;

import com.fivlo.fivlo_backend.domain.user.dto.JoinUserRequest;
import com.fivlo.fivlo_backend.domain.user.dto.JoinUserResponse;
import com.fivlo.fivlo_backend.domain.user.dto.UpdateUserRequest;
import com.fivlo.fivlo_backend.domain.user.dto.UserInfoResponse;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import com.fivlo.fivlo_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

        // 이메일로 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID : " + id));

        // DB에 업데이트, 더티체킹으로 자동 저장
        user.updateOnboardingType(onboardingType);

        return user.getOnboardingType();
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(CustomUserDetails userDetails) {

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. Email : " + userDetails.getUsername() ));

        return new UserInfoResponse(user.getId(), user.getNickname(), user.getProfileImageUrl(), user.getOnboardingType(), user.getIsPremium(), user.getTotalCoins());
    }

    @Transactional
    public String updateUserInfo(CustomUserDetails userDetails, UpdateUserRequest dto) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. Email : " + userDetails.getUsername()));

        user.updateProfile(dto.nickname(), dto.profileImageUrl());
        return "프로필 정보가 성공적으로 수정되었습니다.";
    }
}
