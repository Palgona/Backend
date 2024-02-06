package com.palgona.palgona.domain.product;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer initialPrice;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @ManyToOne(targetEntity = Member.class)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_product_to_member"))
    private Member member;

    @Builder
    public Product(String name, Integer initialPrice, String content, Category category, LocalDateTime deadline, Member member) {
        this.name = name;
        this.initialPrice = initialPrice;
        this.content = content;
        this.category = category;
        this.deadline = deadline;
        this.member = member;
    }
}
