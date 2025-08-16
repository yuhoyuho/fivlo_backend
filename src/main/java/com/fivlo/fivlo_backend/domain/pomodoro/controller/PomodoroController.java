package com.fivlo.fivlo_backend.domain.pomodoro.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.*;
import com.fivlo.fivlo_backend.domain.pomodoro.service.PomodoroService;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.CoinByPomodoroSessionResponse;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;

    /**
     * 포모도로 목표 생성
     * HTTP : POST
     * EndPoint : /api/v1/pomodoro/goals
     */
    @PostMapping(Routes.POMODORO_GOALS)
    public ResponseEntity<Long> createGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PomodoroGoalCreateRequest dto) {

        return ResponseEntity.status(201).body(pomodoroService.createGoal(userDetails.getUser().getId(), dto));
    }

    /**
     * 포모도로 목표 목록 조회 ************ 미완성 ************
     * HTTP : GET
     * EndPoint : /api/v1/pomodoro/goals
     * PomodoroGoalResponse -> 조회된 PomodoroGoal 객체를 넣음 (프론트에서 필요한 값만 꺼내서 사용. ex) id, color ..)
     */
    @GetMapping(Routes.POMODORO_GOALS)
    public ResponseEntity<PomodoroGoalResponse> getAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(pomodoroService.findPomodoroGoals(userDetails.getUser().getId()));
    }

    /**
     * 포모도로 목표 수정
     * HTTP : PATCH
     * EndPoint : /api/v1/pomodoro/goals/{goalId}
     */
    @PatchMapping(Routes.POMODORO_GOALS_BY_ID)
    public ResponseEntity<String> updateGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long goalId, @Valid @RequestBody PomodoroGoalCreateRequest dto) throws AccessDeniedException {

        return ResponseEntity.ok(pomodoroService.update(userDetails.getUser().getId(), goalId, dto));
    }

    /**
     * 포모도로 목표 삭제
     * HTTP : DELETE
     * EndPoint : /api/v1/pomodoro/goals/{goalId}
     */
    @DeleteMapping(Routes.POMODORO_GOALS_BY_ID)
    public ResponseEntity<String> deleteGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long goalId) throws AccessDeniedException {
        return ResponseEntity.ok(pomodoroService.delete(userDetails.getUser().getId(), goalId));
    }

    /**
     * 포모도로 세션 기록 시작
     * HTTP : POST
     * EndPoint : /api/v1/pomodoro/sessions/start
     */
    @PostMapping(Routes.POMODORO_SESSIONS_START)
    public ResponseEntity<PomodoroSessionCreateResponse> createSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PomodoroSessionCreateRequest dto) {
        return ResponseEntity.status(201).body(pomodoroService.createSession(userDetails.getUser().getId(), dto));
    }

    /**
     * 포모도로 세션 종료
     * HTTP : POST
     * EndPoint : /api/v1/pomodoro/sessions/end
     * pomodoroGoalId를 받을거면 OneToOne 관계여야함
     * ManyToOne 관계라면 pomodoroSessionId를 받아야함
     */
    @PostMapping(Routes.POMODORO_SESSIONS_END)
    public ResponseEntity<PomodoroSessionEndResponse> endSession(@Valid @RequestBody PomodoroSessionEndRequest dto) {
        return ResponseEntity.ok(pomodoroService.endSession(dto));
    }

    /**
     * 포모도로 코인 지급
     * HTTP : POST
     * EndPoint : /api/v1/pomodoro/coins
     * 위와 마찬가지로 pomodoroSessionId를 요청에서 받아야함 (user 엔티티에는 pomodoroSession 필드가 없음)
     * 그래야 해당 세션을 조회하고 코인 지급 로직을 구현할 수 있음
     */
    @PostMapping(Routes.POMODORO_COINS)
    public ResponseEntity<CoinByPomodoroSessionResponse> earnedCoin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CoinByPomodoroSessionReqeust dto) {

        return ResponseEntity.ok(pomodoroService.earnedCoin(userDetails.getUser().getId(), dto));
    }
}
