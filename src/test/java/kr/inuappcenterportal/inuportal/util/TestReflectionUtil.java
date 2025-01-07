package kr.inuappcenterportal.inuportal.util;

import java.lang.reflect.Field;

public class TestReflectionUtil {
    // 객체의 ID 필드에 값 주입
    public static void setId(Object target, Long id) {
        setField(target, "id", id);
    }

    // 객체의 특정 필드에 값 주입
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = getFieldFromClass(target.getClass(), fieldName);
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(target, value);
            field.setAccessible(isAccessible);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(fieldName + " 설정 중 오류 발생", e);
        }
    }

    // 클래스 계층 구조를 따라 특정 필드 검색
    private static Field getFieldFromClass(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName + " 필드를 찾을 수 없습니다.");
    }
}
