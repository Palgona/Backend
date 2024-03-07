package com.palgona.palgona.domain.product;

public enum SortType {
    LATEST("LATEST", "최신순"),
    DEADLINE("DEADLINE", "마감순"),
    HIGHEST_PRICE("HIGH_PRICE", "가격 높은순"),
    LOWEST_PRICE("LOWEST_PRICE", "가격 낮은순"),
    BOOK_MARK("BOOK_MARK", "북마크순");


    private String title;
    private String value;

    private SortType(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }
}
