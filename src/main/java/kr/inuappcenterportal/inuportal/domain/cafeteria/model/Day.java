package kr.inuappcenterportal.inuportal.domain.cafeteria.model;

import java.util.Arrays;

public enum Day {
    MON(1),
    TUE(2),
    WED(3),
    THU(4),
    FRI(5),
    SAT(6),
    SUN(7),
    ;

    private final int sign;

    Day(int sign) {
        this.sign = sign;
    }

    public static Day findBySign(int comparedSign) {
        return Arrays.stream(Day.values())
                .filter(day -> day.sign == comparedSign)
                .findAny()
                .orElse(null);
    }
}
