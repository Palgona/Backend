package com.palgona.palgona.domain.member;

public enum Status {
    ACTIVE("STATUS_ACTIVE", "일반 사용자"),
    BLOCKED("STATUS_BLOCKED", "차단된 사용자"),
    DELETE("STATUS_DELETE", "탈퇴한 사용자");

    private final String key;
    private final String title;

    private Status(String key, String title) {
        this.key = key;
        this.title = title;
    }
}
