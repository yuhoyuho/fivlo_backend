package com.fivlo.fivlo_backend.domain.pomodoro.service;

import com.fivlo.fivlo_backend.domain.pomodoro.dto.PomodoroCreateRequest;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.PomodoroGoalResponse;
import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroGoal;
import com.fivlo.fivlo_backend.domain.pomodoro.repository.PomodoroGoalRepository;
import com.fivlo.fivlo_backend.domain.pomodoro.repository.PomodoroSessionRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final PomodoroGoalRepository pomodoroGoalRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PomodoroGoalResponse findPomodoroGoals(Long id) {
        List<PomodoroGoal> pomodoroGoalList = pomodoroGoalRepository.findByUserId(id);
        return new PomodoroGoalResponse(pomodoroGoalList);
    }

    @Transactional
    public Long create(Long id, @Valid PomodoroCreateRequest dto) {
        User findUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        PomodoroGoal goal = PomodoroGoal.builder()
                .user(findUser)
                .name(dto.name())
                .color(dto.color())
                .build();

        pomodoroGoalRepository.save(goal);
        return goal.getId();
    }

    @Transactional
    public String update(Long id, Long goalId, @Valid PomodoroCreateRequest dto) {

        // 포모도로 목표 조회
        PomodoroGoal findGoal = pomodoroGoalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 목표를 찾을 수 없습니다."));

        // 사용자 일치 여부 확인
        boolean isUserMatch = findGoal.getUser().getId().equals(id);
        if(!isUserMatch) {
            throw new IllegalArgumentException("사용자 조회 중 오류가 발생했습니다.");
        }

        // 포모도로 목표 수정
        findGoal.update(dto.name(), dto.color());

        return "목표가 성공적으로 수정되었습니다.";
    }

    @Transactional
    public String delete(Long goalId) {
        pomodoroGoalRepository.deleteById(goalId);
        return "목표가 성공적으로 삭제되었습니다.";
    }
}
