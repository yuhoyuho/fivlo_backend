package com.fivlo.fivlo_backend.domain.reminder.repository;

import com.fivlo.fivlo_backend.domain.reminder.entity.ForgettingPreventionReminder;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<ForgettingPreventionReminder, Long> {
    List<ForgettingPreventionReminder> findByUser(User user);

    List<ForgettingPreventionReminder> findByUserAndRepetitionDaysContaining(User user, String dayOfWeek);
}
