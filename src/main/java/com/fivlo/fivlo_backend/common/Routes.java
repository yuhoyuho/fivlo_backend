package com.fivlo.fivlo_backend.common;

/**
 * API 경로 상수 클래스
 * 앱의 모든 API 엔드포인트를 중앙에서 관리
 */
public class Routes {
    
    // ==================== 기본 설정 ====================
    public static final String API_BASE = "/api/v1";
    
    // ==================== 인증 및 유저 정보 관리 ====================
    public static final String AUTH_BASE = API_BASE + "/auth";
    public static final String AUTH_SOCIAL_LOGIN = AUTH_BASE + "/social-login";
    public static final String AUTH_SIGNUP = AUTH_BASE + "/signup";
    public static final String AUTH_SIGNIN = AUTH_BASE + "/signin";
    public static final String AUTH_REFRESH = AUTH_BASE + "/reissue";
    
    public static final String USERS_BASE = API_BASE + "/users";
    public static final String USERS_ONBOARDING = USERS_BASE + "/onboarding";
    public static final String USERS_ME = USERS_BASE + "/me";
    public static final String USERS_COINS = USERS_BASE + "/coins";
    public static final String USERS_PREMIUM = USERS_BASE + "/premium";
    public static final String USERS_ATTENDANCE = USERS_BASE + "/attendance";
    
    // ==================== Task 및 카테고리 관리 ====================
    public static final String TASKS_BASE = API_BASE + "/tasks";
    public static final String TASKS_COMPLETE = "/complete";  // {taskId}/complete
    public static final String TASKS_COINS = TASKS_BASE + "/coins";  // Task 완료 코인 지급

    
    public static final String CATEGORIES_BASE = API_BASE + "/categories";
    
    // ==================== AI 목표 설정 기능 ====================
    public static final String AI_BASE = API_BASE + "/ai";
    public static final String AI_GOALS = AI_BASE + "/goals";
    public static final String AI_GOALS_TASKS = AI_GOALS + "/tasks";
    
    // ==================== 성장앨범 관리 ====================
    public static final String GROWTH_ALBUM_BASE = API_BASE + "/growth-album";
    public static final String GROWTH_ALBUM_CALENDAR = GROWTH_ALBUM_BASE + "/calendar";
    public static final String GROWTH_ALBUM_CATEGORIES = GROWTH_ALBUM_BASE + "/categories";
    public static final String GROWTH_ALBUM_UPLOAD = GROWTH_ALBUM_BASE + "/upload/presigned-url";
    // ==================== 포모도로 및 집중도 분석 ====================
    public static final String POMODORO_BASE = API_BASE + "/pomodoro";
    public static final String POMODORO_GOALS = POMODORO_BASE + "/goals";
    public static final String POMODORO_SESSIONS_START = POMODORO_BASE + "/sessions/start";
    public static final String POMODORO_SESSIONS_END = POMODORO_BASE + "/sessions/end";
    public static final String POMODORO_COINS = POMODORO_BASE + "/coins";
    
    public static final String ANALYSIS_BASE = API_BASE + "/analysis";
    public static final String ANALYSIS_DAILY = ANALYSIS_BASE + "/daily";
    public static final String ANALYSIS_WEEKLY = ANALYSIS_BASE + "/weekly";
    public static final String ANALYSIS_MONTHLY = ANALYSIS_BASE + "/monthly";
    public static final String ANALYSIS_MONTHLY_AI_SUGGESTIONS = ANALYSIS_MONTHLY + "/ai-suggestions";
    public static final String ANALYSIS_GOALS = ANALYSIS_BASE + "/goals";
    
    // ==================== 오분이 상점 및 커스터마이징 ====================
    public static final String OBOONE_BASE = API_BASE + "/oboone";
    public static final String OBOONE_ITEM = OBOONE_BASE + "/item";
    public static final String OBOONE_SHOP = OBOONE_BASE + "/shop";
    public static final String OBOONE_PURCHASE = OBOONE_BASE + "/purchase";
    public static final String OBOONE_CLOSET = OBOONE_BASE + "/closet";
    public static final String OBOONE_EQUIP = OBOONE_BASE + "/equip";
    public static final String OBOONE_UNEQUIP = OBOONE_BASE + "/unequip";
    
    // ==================== 타임어택 기능 (API 41-48) ====================
    public static final String TIME_ATTACK_BASE = API_BASE + "/time-attack";
    public static final String TIME_ATTACK_GOALS = TIME_ATTACK_BASE + "/goals";
    public static final String TIME_ATTACK_RECOMMEND_STEPS = TIME_ATTACK_BASE + "/recommend-steps";
    public static final String TIME_ATTACK_SESSIONS = TIME_ATTACK_BASE + "/sessions";
    
    // API 48: 마지막 추천 단계 조회 (AI 캐싱용)
    public static final String TIME_ATTACK_GOALS_LAST_STEPS = TIME_ATTACK_GOALS + "/{goalId}/last-recommended-steps";
    
    // 제거된 API: 개별 Step 관리 기능은 세션 단위로 통합됨
    // TIME_ATTACK_STEPS - 더 이상 개별 단계 관리하지 않음
    
    // ==================== 망각방지 알림 시스템 ====================
    public static final String GEO_BASE = API_BASE + "/geo";
    public static final String GEO_SEARCH_ADDRESS = GEO_BASE + "/search-address";
    
    public static final String REMINDERS_BASE = API_BASE + "/reminders";
    public static final String REMINDERS_COMPLETE = "/complete";  // {reminderId}/complete
    public static final String REMINDERS_DAILY_CHECK_AND_REWARD = REMINDERS_BASE + "/daily-check-and-reward";
    
    // ==================== 동적 경로 패턴 ====================
    // Task 관련
    public static final String TASKS_BY_ID = TASKS_BASE + "/{taskId}";
    public static final String TASKS_COMPLETE_BY_ID = TASKS_BASE + "/{taskId}" + TASKS_COMPLETE;
    public static final String TASKS_GROWTH_ALBUM = TASKS_BASE + "/{taskId}/growth-album";
    
    // Category 관련
    public static final String CATEGORIES_BY_ID = CATEGORIES_BASE + "/{categoryId}";
    
    // Growth Album 관련
    public static final String GROWTH_ALBUM_BY_ID = GROWTH_ALBUM_BASE + "/{albumId}";
    
    // Pomodoro 관련
    public static final String POMODORO_GOALS_BY_ID = POMODORO_GOALS + "/{goalId}";
    
    // Analysis 관련
    public static final String ANALYSIS_GOALS_BY_ID = ANALYSIS_GOALS + "/{goalId}";
    
    // Oboone 관련
    public static final String OBOONE_EQUIP_BY_ID = OBOONE_EQUIP + "/{userItemId}";
    public static final String OBOONE_UNEQUIP_BY_ID = OBOONE_UNEQUIP + "/{userItemId}";
    
    // Time Attack 관련
    public static final String TIME_ATTACK_GOALS_BY_ID = TIME_ATTACK_GOALS + "/{goalId}";
    // TIME_ATTACK_STEPS_BY_ID - 개별 단계 관리 API 제거로 삭제
    // Reminders 관련
    public static final String REMINDERS_BY_ID = REMINDERS_BASE + "/{reminderId}";
    public static final String REMINDERS_COMPLETE_BY_ID = REMINDERS_BASE + "/{reminderId}" + REMINDERS_COMPLETE;
}
