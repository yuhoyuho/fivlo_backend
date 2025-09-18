package com.fivlo.fivlo_backend.domain.reminder.service;

import com.fivlo.fivlo_backend.domain.reminder.dto.ReminderDto;
import com.fivlo.fivlo_backend.domain.reminder.entity.DailyReminderCompletion;
import com.fivlo.fivlo_backend.domain.reminder.entity.ForgettingPreventionReminder;
import com.fivlo.fivlo_backend.domain.reminder.repository.ReminderCompletionRepository;
import com.fivlo.fivlo_backend.domain.reminder.repository.ReminderRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.domain.user.service.CoinTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final ReminderCompletionRepository reminderCompletionRepository;
    private final CoinTransactionService coinTransactionService;

    // API 49 : 망각방지 알림 생성
    @Transactional
    public Long create(Long userId, ReminderDto.CreateReminderRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        String repetitionDays = convertDaysListToFixedString(dto.repetitionDays());

        ForgettingPreventionReminder reminder = ForgettingPreventionReminder.builder()
                .user(user)
                .title(dto.title())
                .alarmTime(dto.alarmTime())
                .repetitionDays(repetitionDays)
                .build();

        if(user.getIsPremium() && dto.locationName() != null) {
            reminder.updateLocationInfo(dto.locationName(), dto.locationAddress(), dto.locationLatitude(), dto.locationLongitude());
        }

        return reminderRepository.save(reminder).getId();
    }

    // API 50 : 망각방지 알림 조회
    @Transactional(readOnly = true)
    public ReminderDto.GetReminderListResponse getReminders(Long userId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        List<ReminderDto.GetReminderResponse> reminderResponses = reminderRepository.findByUser(user)
                .stream()
                .map(r -> new ReminderDto.GetReminderResponse(
                        r.getId(),
                        r.getTitle(),
                        r.getAlarmTime(),
                        r.getRepetitionDaysArray(),
                        r.getLocationName(),
                        r.hasLocationSet()
                ))
                .toList();

        return new ReminderDto.GetReminderListResponse(reminderResponses);
    }

    // API 51 : 망각방지 알림 수정
    @Transactional
    public String update(Long userId, Long reminderId, ReminderDto.UpdateReminderRequest dto) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        // 알림 조회
        ForgettingPreventionReminder reminder = findReminderAndCheckedByUserId(userId, reminderId);

        String repetitionDays = (dto.repetitionDays() != null) ? convertDaysListToFixedString(dto.repetitionDays()) : null;
        reminder.updateBasicInfo(dto.title(), dto.alarmTime(), repetitionDays);

        if(user.getIsPremium() && dto.locationName() != null) {
            reminder.updateLocationInfo(dto.locationName(), dto.locationAddress(), dto.locationLatitude(), dto.locationLongitude());
        }

        return "알림 항목이 성공적으로 수정되었습니다.";
    }

    // API 52 : 망각방지 알림 삭제
    @Transactional
    public String delete(Long userId, Long reminderId) {

        // 알림 조회
        ForgettingPreventionReminder reminder = findReminderAndCheckedByUserId(userId, reminderId);

        reminderCompletionRepository.deleteByReminder(reminder);
        reminderRepository.delete(reminder); // Cascade 설정에 따라 연관된 Completion도 삭제될 수 있음
        return "알림 항목이 성공적으로 삭제되었습니다.";
    }

    // API 53 : 일일 망각방지 알림 항목 완료 상태 변경
    @Transactional
    public ReminderDto.CompletionResponse complete(Long userId, Long reminderId, ReminderDto.CompletionRequest dto) {

        // 알림 조회
        ForgettingPreventionReminder reminder = findReminderAndCheckedByUserId(userId, reminderId);

        DailyReminderCompletion completion = reminderCompletionRepository.findByReminderAndCompletionDate(reminder, dto.date())
                .orElseGet(() -> DailyReminderCompletion.builder()
                        .reminder(reminder)
                        .completionDate(dto.date())
                        .build());

        completion.updateCompletionStatus(dto.isCompleted());
        reminderCompletionRepository.save(completion);

        return new ReminderDto.CompletionResponse(reminderId, completion.isCompletedToday(), "알림 항목 완료 상태가 업데이트되었습니다.");
    }

    // API 54 : 일일 체크 및 보상
    @Transactional
    public ReminderDto.DailyCheckResponse dailyCheckAndReward(Long userId, ReminderDto.DailyCheckRequest dto) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        LocalDate date = dto.date();
        String dayOfWeek = date.getDayOfWeek().name().substring(0, 1);

        List<ForgettingPreventionReminder> allReminders = reminderRepository.findByUser(user);

        List<ForgettingPreventionReminder> activeReminders = allReminders.stream()
                .filter(r -> r.isActiveOnDay(dayOfWeek))
                .toList();
        long completedCount = reminderCompletionRepository.countByReminderUserAndCompletionDateAndIsCompleted(user, date, true);

        boolean allCompleted = !activeReminders.isEmpty() && activeReminders.size() == completedCount;
        boolean coinAwarded = false;

        if(allCompleted && user.getIsPremium() && (user.getLastReminderCoinDate() == null || user.getLastReminderCoinDate().isBefore(date) )) {
            user.addCoins(1);
            user.updateLastReminderCoinDate(date);
            coinTransactionService.logTransaction(user, 1);
            coinAwarded = true;
        }

        String message = coinAwarded ? "모든 알림 항목 완료! 코인이 지급되었습니다." : "코인 지급 조건 미달";

        return new ReminderDto.DailyCheckResponse(allCompleted, coinAwarded, user.getTotalCoins(), message);
    }

    // 중복 로직을 위한 private 헬퍼 메서드
    private ForgettingPreventionReminder findReminderAndCheckedByUserId(Long userId, Long reminderId) {
        ForgettingPreventionReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new NoSuchElementException("해당 알림을 찾을 수 없습니다."));
        if (!reminder.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("이 알림에 접근할 권한이 없습니다.");
        }
        return reminder;
    }

    /**
     * MON -> M, TUE -> T ..
     */
    private static final List<String> DAYS_OF_WEEK = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    private String convertDaysListToFixedString(List<String> days) {
        if(days == null || days.isEmpty()) {
            return "-------";
        }

        char[] result = new char[7];
        Arrays.fill(result, '-'); // 기본값 : "-"

        for(String day : days) {
            int idx = DAYS_OF_WEEK.indexOf(day.toUpperCase());
            if(idx != -1) {
                result[idx] = day.charAt(0);
            }
        }

        return new String(result);
    }
}
