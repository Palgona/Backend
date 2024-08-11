package com.palgona.palgona.domain.chat;

import com.palgona.palgona.common.entity.BaseTimeEntity;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ColumnDefault("false")
    @Column(columnDefinition = "TINYINT(1)")
    private boolean isLeaveSender;

    @Setter
    @ColumnDefault("false")
    @Column(columnDefinition = "TINYINT(1)")
    private boolean isLeaveReceiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessages;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Builder
    ChatRoom(Member sender, Member receiver, Product product){
        this.sender = sender;
        this.receiver = receiver;
        this.isLeaveSender = false;
        this.isLeaveReceiver = false;
        this.product = product;
    }

    public boolean hasMember(Member member) {
        return member.equals(sender) || member.equals(receiver);
    }
}

