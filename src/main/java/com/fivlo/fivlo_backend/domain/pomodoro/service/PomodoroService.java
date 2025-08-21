package com.fivlo.fivlo_backend.domain.pomodoro.service;

import com.fivlo.fivlo_backend.domain.pomodoro.dto.*;
import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroGoal;
import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroSession;
import com.fivlo.fivlo_backend.domain.pomodoro.repository.PomodoroGoalRepository;
import com.fivlo.fivlo_backend.domain.pomodoro.repository.PomodoroSessionRepository;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.CoinByPomodoroSessionResponse;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.domain.user.service.CoinTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final PomodoroGoalRepository pomodoroGoalRepository;
    private final UserRepository userRepository;
    private final CoinTransactionService coinTransactionService;

    @Transactional(readOnly = true)
    public PomodoroGoalListResponse findPomodoroGoals(Long id) {
        List<PomodoroGoalResponse> goals = pomodoroGoalRepository.findByUserId(id)
                .stream()
                .map(goal -> new PomodoroGoalResponse(goal.getId(), goal.getName(), goal.getColor()))
                .toList();

        return new PomodoroGoalListResponse(goals);
    }

    @Transactional
    public Long createGoal(Long id, @Valid PomodoroGoalCreateRequest dto) {
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
    public String update(Long id, Long goalId, @Valid PomodoroGoalCreateRequest dto) throws AccessDeniedException {

        // 포모도로 목표 조회
        PomodoroGoal findGoal = pomodoroGoalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 목표를 찾을 수 없습니다."));

        // 사용자 일치 여부 확인
        boolean isUserMatch = findGoal.getUser().getId().equals(id);
        if(!isUserMatch) {
            throw new AccessDeniedException("해당 목표를 수정할 권한이 없습니다.");
        }

        // 포모도로 목표 수정
        findGoal.update(dto.name(), dto.color());

        return "목표가 성공적으로 수정되었습니다.";
    }

    @Transactional
    public String delete(Long id, Long goalId) throws AccessDeniedException {
        PomodoroGoal pomodoroGoal = pomodoroGoalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("해당 목표를 찾을 수 없습니다."));

        if(!pomodoroGoal.getUser().getId().equals(id)) {
            throw new AccessDeniedException("해당 목표를 삭제할 권한이 없습니다.");
        }

        pomodoroGoalRepository.deleteById(goalId);
        return "목표가 성공적으로 삭제되었습니다.";
    }

    @Transactional
    public PomodoroSessionCreateResponse createSession(Long id, @Valid PomodoroSessionCreateRequest dto) {
        // 포모도로 세션 생성
        PomodoroSession session = PomodoroSession.builder()
                .user(userRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다.")))
                .pomodoroGoal(pomodoroGoalRepository.findById(dto.id())
                        .orElseThrow(() -> new NoSuchElementException("해당 목표를 찾을 수 없습니다.")))
                .build();
        pomodoroSessionRepository.save(session);

        return new PomodoroSessionCreateResponse(session.getId(), "포모도로 세션 기록이 시작되었습니다.");
    }

     // 수정해야함 (dto에 pomodoro_goal_id를 받으면 해당 세션을 종료할 수가 없음.)
     // 세션은 삭제하는게 아니라 그냥 기록을 유지하는거라서
    @Transactional
    public PomodoroSessionEndResponse endSession(Long id, @Valid PomodoroSessionEndRequest dto) {

        // durationTime 추가
        PomodoroSession session = pomodoroSessionRepository.findById(dto.pomodoroSessionId())
                .orElseThrow(() -> new NoSuchElementException("해당 세션을 찾을 수 없습니다."));

        if(!session.getUser().getId().equals(id)) {
            throw new AccessDeniedException("해당 세션을 종료할 권한이 없습니다.");
        }

        session.updateDurationInSeconds(dto.durationInSeconds());

        // 사이클 확인 후 상태 변경
        if(dto.isCycleCompleted()) {
            session.updateCycleCompletedStatus(true);
        }

        return new PomodoroSessionEndResponse(dto.pomodoroSessionId(), "세션 기록이 종료되었습니다.");
    }

    @Transactional
    public CoinByPomodoroSessionResponse earnedCoin(Long id, @Valid CoinByPomodoroSessionReqeust dto) {

        // 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        // 마지막 코인 지급일 확인
        LocalDate today = LocalDate.now();
        LocalDate lastCoinDate = user.getLastPomodoroCoinDate();

        if(lastCoinDate != null && lastCoinDate.isEqual(today)) {
            return new CoinByPomodoroSessionResponse(user.getTotalCoins(), "오늘은 이미 코인을 지급받았습니다.");
        }

        // 포모도로 세션 조회
        PomodoroSession session = pomodoroSessionRepository.findById(dto.pomodoroSessionId())
                .orElseThrow(() -> new NoSuchElementException("해당 세션을 찾을 수 없습니다."));

        // 세션을 가진 사용자의 id 확인
        if(!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("자신의 세션만 코인을 받을 수 있습니다.");
        }

        // isCycleCompleted, isPremiumUser 확인
        if(session.getIsCycleCompleted() && user.getIsPremium()) {
            user.addCoins(1);
            user.updateLastPomodoroCoinDate(today);
            coinTransactionService.logTransaction(user, 1);

            return new CoinByPomodoroSessionResponse(user.getTotalCoins(), "코인이 성공적으로 지급되었습니다.");
        }
        else {
            return new CoinByPomodoroSessionResponse(user.getTotalCoins(), "코인이 지급되지 않았습니다.");
        }
    }
}
