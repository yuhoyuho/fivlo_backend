package com.fivlo.fivlo_backend.domain.timeattack.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 미리 정의된 타임어택 목적 상수
 * 모든 사용자에게 기본으로 제공되는 목적들
 */
public class PredefinedTimeAttackGoals {

    /**
     * 미리 정의된 목적 정보
     */
    @Getter
    @AllArgsConstructor
    public enum Goal {
        OUTING_PREP("timeAttack.goal.outingPrep", "외출 준비", "Get ready to go out"),
        MEAL_PREP("timeAttack.goal.mealPrep", "식사 준비", "Preparation of a meal"),
        HOUSE_CLEANING("timeAttack.goal.houseCleaning", "집 정리하기", "Cleaning up the house");

        /** i18n 키 */
        private final String nameKey;
        /** 한국어 표시명 (참고용) */
        private final String koreanName;
        /** 영어 표시명 (참고용) */
        private final String englishName;
    }

    /**
     * 모든 미리 정의된 목적 목록 반환
     */
    public static List<Goal> getAllPredefinedGoals() {
        return Arrays.asList(Goal.values());
    }

    /**
     * 특정 nameKey가 미리 정의된 목적인지 확인
     */
    public static boolean isPredefinedGoal(String nameKey) {
        return Arrays.stream(Goal.values())
                .anyMatch(goal -> goal.getNameKey().equals(nameKey));
    }

    /**
     * nameKey로 미리 정의된 목적 정보 조회
     */
    public static Goal getByNameKey(String nameKey) {
        return Arrays.stream(Goal.values())
                .filter(goal -> goal.getNameKey().equals(nameKey))
                .findFirst()
                .orElse(null);
    }
}
