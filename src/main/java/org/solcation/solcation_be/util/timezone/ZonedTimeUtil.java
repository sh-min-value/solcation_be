package org.solcation.solcation_be.util.timezone;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

@Component
public final class ZonedTimeUtil {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private ZonedTimeUtil() {}

    /**
     * 하루 단위
     * N년 N월 N일 0시 - N년 N일 24시
     * @param day
     * @return
     */
    public static ZonedTimeRange day(LocalDate day) {
        ZonedDateTime start = day.atStartOfDay(ZONE_ID);
        ZonedDateTime end = start.plusDays(1);
        return new ZonedTimeRange(start.toInstant(), end.toInstant());
    }

    /**
     * 주 단위, 월요일이 주의 시작이라고 고정
     * N년 N월 N일 월요일 0시 - N년 N월 N+7일 월요일 0시
     * @param day
     * @return
     */
    public static ZonedTimeRange week(LocalDate day) {
        ZonedDateTime start = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZONE_ID);
        ZonedDateTime end = start.plusWeeks(1);

        return new ZonedTimeRange(start.toInstant(), end.toInstant());
    }

    /**
     * 월 단위
     * N년 N월 1일 0시 - N년 N+1월 1일 0시
     * @param ym
     * @return
     */
    public static ZonedTimeRange month(YearMonth ym) {
        ZonedDateTime start = ym.atDay(1).atStartOfDay(ZONE_ID);
        ZonedDateTime end = start.plusMonths(1);
        return new ZonedTimeRange(start.toInstant(), end.toInstant());
    }

    /**
     * 사용자 지정 기간
     * @param start
     * @param end
     * @return
     */
    public static ZonedTimeRange custom(LocalDate start, LocalDate end) {
        ZonedDateTime startZdt = start.atStartOfDay(ZONE_ID);
        ZonedDateTime endZdt = end.plusDays(1).atStartOfDay(ZONE_ID);
        return new ZonedTimeRange(startZdt.toInstant(), endZdt.toInstant());
    }

    /**
     * 현재 날짜
     */
    public static LocalDate now() {
        return LocalDate.now(ZONE_ID);
    }
}
