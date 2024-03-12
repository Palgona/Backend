package com.palgona.palgona.domain.mileage;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MileageHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer before;

    @Column(nullable = false)
    private Integer after;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MileageState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    MileageHistory(
            int before,
            int after,
            int amount,
            MileageState state,
            Member member
    ){
        this.before = before;
        this.after = after;
        this.amount = amount;
        this.state = state;
        this.member = member;
    }
}