package com.palgona.palgona.domain.image;


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
public class Image extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(targetEntity = Member.class)
    @JoinColumn(name = "memberId", foreignKey = @ForeignKey(name = "fk_image_to_member"))
    private Member member;

    @Builder
    public Image(String imageUrl, Member member){
        this.imageUrl = imageUrl;
        this.member = member;
    }

}