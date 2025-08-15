package com.fivlo.fivlo_backend.domain.pomodoro.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.PomodoroCreateRequest;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.PomodoroGoalResponse;
import com.fivlo.fivlo_backend.domain.pomodoro.service.PomodoroService;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PomodoroCreateRequest dto) {

        return ResponseEntity.status(201).body(pomodoroService.create(userDetails.getUser().getId(), dto));
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
    public ResponseEntity<String> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @PathVariable Long goalId, @Valid @RequestBody PomodoroCreateRequest dto) {

        return ResponseEntity.ok(pomodoroService.update(userDetails.getUser().getId(), goalId, dto));
    }

    /**
     * 포모도로 목표 삭제
     * HTTP : DELETE
     * EndPoint : /api/v1/pomodoro/goals/{goalId}
     */
    @DeleteMapping(Routes.POMODORO_GOALS_BY_ID)
    public ResponseEntity<String> delete(@PathVariable Long goalId) {
        return ResponseEntity.ok(pomodoroService.delete(goalId));
    }
}
