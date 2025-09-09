package org.solcation.solcation_be.domain.travel.util;

import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public final class Positioning {
    private static final MathContext MC = new MathContext(38, RoundingMode.HALF_UP);

    //중간에 삽입될때
    public static BigDecimal mid(BigDecimal a, BigDecimal b) {
        return a.add(b).divide(new BigDecimal("2"), MC);
    }

    //맨 앞 삽입
    public static BigDecimal before(BigDecimal next) {
        return next.divide(new BigDecimal("2"), MC);
    }

    //맨 뒤 삽입
    public static BigDecimal after(BigDecimal prev) {
        return prev.add(BigDecimal.ONE, MC);
    }

    /* ===== 유틸 ===== */
    private PlanDetailDTO find(List<PlanDetailDTO> list, String crdtId) {
        return list.stream().filter(x -> crdtId.equals(x.getCrdtId()))
                .findFirst().orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }

    private BigDecimal computePos(List<PlanDetailDTO> all, int day, String prev, String next) {
        var alive = all.stream()
                .filter(x -> !x.isTombstone() && x.getPdDay() == day)
                .sorted(Comparator.comparing(a -> new BigDecimal(a.getPosition())))
                .toList();

        if (prev == null && next == null) {
            if (alive.isEmpty()) return new BigDecimal("1");
            var tail = alive.get(alive.size()-1);
            return Positioning.after(new BigDecimal(tail.getPosition()));
        }
        if (prev == null) {
            var n = alive.stream().filter(x -> x.getCrdtId().equals(next)).findFirst().orElseThrow();
            return Positioning.before(new BigDecimal(n.getPosition()));
        }
        if (next == null) {
            var p = alive.stream().filter(x -> x.getCrdtId().equals(prev)).findFirst().orElseThrow();
            return Positioning.after(new BigDecimal(p.getPosition()));
        }
        var p = alive.stream().filter(x -> x.getCrdtId().equals(prev)).findFirst().orElseThrow();
        var n = alive.stream().filter(x -> x.getCrdtId().equals(next)).findFirst().orElseThrow();
        var mid = Positioning.mid(new BigDecimal(p.getPosition()), new BigDecimal(n.getPosition()));
        if (mid.compareTo(new BigDecimal(p.getPosition())) <= 0 || mid.compareTo(new BigDecimal(n.getPosition())) >= 0) {
            BigDecimal step = new BigDecimal("10"), cur = step;
            for (var a : alive) { a.setPosition(cur.toPlainString()); cur = cur.add(step); }
            return computePos(all, day, prev, next);
        }
        return mid;
    }

    private String norm(String s) { if (s==null) return null; var t=s.trim(); return (t.isEmpty()||"0".equals(t)||"null".equalsIgnoreCase(t))?null:t; }

}
