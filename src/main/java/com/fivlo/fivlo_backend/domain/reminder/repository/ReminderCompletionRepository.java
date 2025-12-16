package com.fivlo.fivlo_backend.domain.reminder.repository;

import com.fivlo.fivlo_backend.domain.reminder.entity.DailyReminderCompletion;
import com.fivlo.fivlo_backend.domain.reminder.entity.ForgettingPreventionReminder;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReminderCompletionRepository extends JpaRepository<DailyReminderCompletion, Long> {
    Optional<DailyReminderCompletion> findByReminderAndCompletionDate(ForgettingPreventionReminder reminder,
            LocalDate date);

    List<DailyReminderCompletion> findByReminderInAndCompletionDate(List<ForgettingPreventionReminder> reminders,
            LocalDate date);

    long countByReminderUserAndCompletionDateAndIsCompleted(User user, LocalDate date, boolean isCompleted);

    void deleteByReminder(ForgettingPreventionReminder reminder);
}
