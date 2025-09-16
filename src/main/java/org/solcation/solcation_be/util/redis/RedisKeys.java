package org.solcation.solcation_be.util.redis;

public final class RedisKeys {
    private RedisKeys() {}

    public static String snapshot(long travelId, int day)     { return "plan:snapshot:%d:%d".formatted(travelId, day); }    // 현재 편집 상태 스냅샷
    public static String stream(long travelId, int day)       { return "plan:stream:%d:%d".formatted(travelId, day); }      // op 로그
    public static String op(String opId)                      { return "plan:op:%s".formatted(opId); }                      // 멱등키
    public static String members(long travelId)               { return "plan:members:%d".formatted(travelId); }     // 현재 편집 참여중인 유저 목록
    public static String saveLock(long travelId, int day)     { return "plan:save:lock:%d:%d".formatted(travelId, day); }   // 저장 구간 락
    public static String editLock(long travelId, int day)     { return "plan:edit:lock:%d:%d".formatted(travelId, day); }   // 스냅샷 수정 관점 편집락
    public static String dirtyDays(long travelId)             { return "plan:dirtyDays:%d".formatted(travelId); }           // RSet<Integer>, 변경된 일자 목록
}
