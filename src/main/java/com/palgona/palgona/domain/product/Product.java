package com.palgona.palgona.domain.product;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.domain.member.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private ProductState productState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Product(
            String name,
            Integer initialPrice,
            String content,
            Category category,
            LocalDateTime deadline,
            ProductState productState,
            Member member) {
        this.name = name;
        this.initialPrice = initialPrice;
        this.content = content;
        this.category = category;
        this.deadline = deadline;
        this.productState = productState;
        this.member = member;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateInitialPrice(Integer initialPrice) {
        this.initialPrice = initialPrice;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void updateDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public void updateProductState(ProductState productState) {this.productState = productState;}

    public boolean isDeadlineReached() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return currentDateTime.isAfter(this.deadline);
    }

    public boolean isOwner(Member member){
        return this.member.getId().equals(member.getId());
    }
}
