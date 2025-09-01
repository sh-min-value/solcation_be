package org.solcation.solcation_be.util.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
    code -> Enum 매핑
 */
public final class LegacyEnumUtils {
    private LegacyEnumUtils() {}

    private static final Map<Class<?>, Map<Integer, ?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E> & LegacyCommonType> E of(Class<E> enumType, int code) {
        var map = (Map<Integer, E>) CACHE.computeIfAbsent(enumType, cls ->
                Stream.of(enumType.getEnumConstants()).collect(
                        Collectors.toMap(LegacyCommonType::getCode, e -> e)
                )
        );

        E e = map.get(code);

        if(e == null) {
            throw new IllegalArgumentException(enumType.getSimpleName() + " unknown code: " + code);
        }

        return e;
    }
}
