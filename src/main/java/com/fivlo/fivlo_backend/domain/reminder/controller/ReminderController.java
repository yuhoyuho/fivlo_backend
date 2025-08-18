package com.fivlo.fivlo_backend.domain.reminder.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.reminder.dto.ReminderDto;
import com.fivlo.fivlo_backend.domain.reminder.service.ReminderService;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    /** 알림 생성
     * HTTP : POST
     * EndPoint : /api/v1/reminders
     */
    @PostMapping(Routes.REMINDERS_BASE)
    public ResponseEntity<Long> createReminder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReminderDto.CreateReminderRequest dto) {

        return ResponseEntity.status(201).body(reminderService.create(userDetails.getUser().getId(), dto));
    }

    /**
     * 알림 조회
     * HTTP : GET
     * EndPoint : /api/v1/reminders
     */
    @GetMapping(Routes.REMINDERS_BASE)
    public ResponseEntity<ReminderDto.GetReminderListResponse> getReminders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(reminderService.getReminders(userDetails.getUser().getId()));
    }

    /**
     * 알림 수정
     * HTTP : PATCH
     * EndPoint : /api/v1/reminders/{reminderId}
     */
    @PatchMapping(Routes.REMINDERS_BY_ID)
    public ResponseEntity<String> updateReminder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reminderId,
            @Valid @RequestBody ReminderDto.UpdateReminderRequest dto) {

        return ResponseEntity.ok(reminderService.update(userDetails.getUser().getId(), reminderId, dto));
    }

    /**
     * 알림 삭제
     * HTTP : DELETE
     * EndPoint : /api/v1/reminders/{reminderId}
     */
    @DeleteMapping(Routes.REMINDERS_BY_ID)
    public ResponseEntity<String> deleteReminder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reminderId) {

        return ResponseEntity.ok(reminderService.delete(userDetails.getUser().getId(), reminderId));
    }

    /**
     * 완료 상태 변경
     * HTTP : PATCH
     * EndPoint : /api/v1/reminders/{reminderId}/complete
     */
    @PatchMapping(Routes.REMINDERS_COMPLETE_BY_ID)
    public ResponseEntity<ReminderDto.CompletionResponse> completeReminder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reminderId,
            @Valid @RequestBody ReminderDto.CompletionRequest dto) {

        return ResponseEntity.ok(reminderService.complete(userDetails.getUser().getId(), reminderId, dto));
    }

    /**
     * 완료 확인 및 코인 지급
     * HTTP : POST
     * EndPoint : /api/v1/reminders/daily-check-and-reward
     */
    @PostMapping(Routes.REMINDERS_DAILY_CHECK_AND_REWARD)
    public ResponseEntity<ReminderDto.DailyCheckResponse> dailyCheckAndReward(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReminderDto.DailyCheckRequest dto) {

        return ResponseEntity.ok(reminderService.dailyCheckAndReward(userDetails.getUser().getId(), dto));
    }
}
