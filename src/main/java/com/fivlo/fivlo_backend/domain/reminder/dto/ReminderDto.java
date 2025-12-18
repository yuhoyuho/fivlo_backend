package com.fivlo.fivlo_backend.domain.reminder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReminderDto {

        // API 49 : 망각방지 알림 생성 요청 dto
        public record CreateReminderRequest(
                        String title,
                        @JsonProperty("alarm_time") LocalTime alarmTime,
                        @JsonProperty("repetition_days") List<String> repetitionDays,
                        @JsonProperty("location_name") String locationName,
                        @JsonProperty("location_address") String locationAddress,
                        @JsonProperty("location_latitude") BigDecimal locationLatitude,
                        @JsonProperty("location_longitude") BigDecimal locationLongitude) {
        }

        // API 50 : 망각방지 알림 조회 응답 dto
        public record GetReminderResponse(
                        Long id,
                        String title,
                        @JsonProperty("alarm_time") LocalTime alarmTime,
                        @JsonProperty("repetition_days") List<String> repetitionDays,
                        @JsonProperty("location_name") String locationName,
                        @JsonProperty("is_location_set") Boolean isLocationSet,
                        @JsonProperty("is_completed") Boolean isCompleted) {
        }

        // API 50 : 망각방지 알림 목록 최종 응답 dto
        public record GetReminderListResponse(List<GetReminderResponse> reminders) {
        }

        // API 51 : 망각방지 알림 수정 요청 dto
        public record UpdateReminderRequest(
                        String title,
                        @JsonProperty("alarm_time") LocalTime alarmTime,
                        @JsonProperty("repetition_days") List<String> repetitionDays,
                        @JsonProperty("location_name") String locationName,
                        @JsonProperty("location_address") String locationAddress,
                        @JsonProperty("location_latitude") BigDecimal locationLatitude,
                        @JsonProperty("location_longitude") BigDecimal locationLongitude) {
        }

        // API 53 : 망각방지 알림 완료 상태 변경 요청 dto
        public record CompletionRequest(
                        LocalDate date,
                        @JsonProperty("is_completed") Boolean isCompleted) {
        }

        // API 53 : 망각방지 알림 완료 상태 변경 응답 dto
        public record CompletionResponse(
                        Long id,
                        @JsonProperty("is_completed") Boolean isCompleted,
                        String message) {
        }

        // API 54 : 일일 체크 요청 dto
        public record DailyCheckRequest(LocalDate date) {
        }

        // API 54 : 일일 체크 응답 dto
        public record DailyCheckResponse(
                        @JsonProperty("is_all_completed_for_day") Boolean isAllCompletedForDay,
                        @JsonProperty("coin_awarded_today") Boolean coinAwardedToday,
                        @JsonProperty("total_coins") Integer totalCoins,
                        String message) {
        }
}
