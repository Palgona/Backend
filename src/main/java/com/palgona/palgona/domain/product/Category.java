package com.palgona.palgona.domain.product;


import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Category {
    DIGITAL_DEVICE("DIGITAL_DEVICE", "디지털 기기"),
    FURNITURE("FURNITURE", "가구"),
    CLOTHING("CLOTHING", "의류"),
    FOOD("FOOD", "식품"),
    BOOK("BOOK", "도서"),
    OTHER("OTHER", "기타");

    private final String key;
    private final String value;

    public static Category from(String name) {
        return Arrays.stream(values())
                .filter(category -> category.getKey().equals(name))
                .findAny()
                .orElse(null);
    }

    Category(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
