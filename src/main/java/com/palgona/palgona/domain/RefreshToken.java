package com.palgona.palgona.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private String token;

    @Column(nullable = false, unique = true)
    private String socialId;

    @Builder
    public RefreshToken(String token, String socialId) {
        this.token = token;
        this.socialId = socialId;
    }
}
