package com.palgona.palgona.domain.product;
public enum ProductState {
    ON_SALE("ON_SALE", "판매중"),
    SOLD_OUT("SOLD_OUT", "판매 완료"),
    DELETED("DELETED", "삭제됨");

    private final String key;
    private final String value;

    ProductState(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
