package com.palgona.palgona.domain.mileage;

public enum MileageState {
    CHARGE("CHARGE", "충전"),
    USE("USE", "사용");

    private final String key;
    private final String value;

    MileageState(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
