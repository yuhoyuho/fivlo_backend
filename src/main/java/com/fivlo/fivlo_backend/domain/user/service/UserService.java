package com.fivlo.fivlo_backend.domain.user.service;

import com.fivlo.fivlo_backend.domain.user.dto.JoinUserRequest;
import com.fivlo.fivlo_backend.domain.user.dto.JoinUserResponse;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
        String nickname = dto.email().substring(0, dto.email().indexOf("@")) + UUID.randomUUID();

        // 사용자 저장
        User user = User.builder()
                .email(dto.email())
                .password(encodedPassword)
                .nickname(nickname)
                .build();

        User savedUser = userRepository.save(user);

        // jwt 발급
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        return new JoinUserResponse(token, savedUser.getId(), savedUser.getOnboardingType());
    }
}
