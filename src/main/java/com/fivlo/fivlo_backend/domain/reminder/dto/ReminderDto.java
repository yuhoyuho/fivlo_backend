package com.fivlo.fivlo_backend.domain.reminder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReminderDto {

    // API 49 : 망각방지 알림 생성 요청 dto
    public record CreateReminderRequest(
            String title,
            LocalTime alarmTime,
            List<String> repetitionDays,
            String locationName,
            String locationAddress,
            BigDecimal locationLatitude,
            BigDecimal locationLongitude
    ) {}

    // API 50 : 망각방지 알림 조회 응답 dto
    public record GetReminderResponse(
            Long id,
            String title,
            LocalTime alarmTime,
            List<String> repetitionDays,
            String locationName,
            Boolean isLocationSet
    ) {}

    // API 50 : 망각방지 알림 목록 최종 응답 dto
    public record GetReminderListResponse(List<GetReminderResponse> reminders) {}

    // API 51 : 망각방지 알림 수정 요청 dto
    public record UpdateReminderRequest(
            String title,
            LocalTime alarmTime,
            List<String> repetitionDays,
            String locationName,
            String locationAddress,
            BigDecimal locationLatitude,
            BigDecimal locationLongitude
    ) {}

    // API 53 : 망각방지 알림 완료 상태 변경 요청 dto
    public record CompletionRequest(
            LocalDate date,
            Boolean isCompleted
    ) {}

    // API 53 : 망각방지 알림 완료 상태 변경 응답 dto
    public record CompletionResponse(
            Long id,
            Boolean isCompleted,
            String message
    ) {}

    // API 54 : 일일 체크 요청 dto
    public record DailyCheckRequest(LocalDate date) {}

    // API 54 : 일일 체크 응답 dto
    public record DailyCheckResponse(
            Boolean isAllCompletedForDay,
            Boolean coinAwardedToday,
            Integer totalCoins,
            String message
    ) {}
}
