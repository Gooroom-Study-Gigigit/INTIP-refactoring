package kr.inuappcenterportal.inuportal.domain.cafeteria.model;

import lombok.Getter;

@Getter
public enum MealType {
    BREAKFAST("아침", 1),
    LUNCH("점심", 2),
    DINNER("저녁", 3);

    private final String value;
    private final int intValue;

    MealType(String value, int intValue) {
        this.value = value;
        this.intValue = intValue;
    }

    public static int getSize() {
        return MealType.values().length;
    }

}
