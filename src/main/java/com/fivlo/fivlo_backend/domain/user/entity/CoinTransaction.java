package com.fivlo.fivlo_backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 코인 거래 엔티티
 * 사용자가 코인을 획득하거나 사용하는 내역을 기록하는 테이블
 */
@Entity
@Table(name = "coin_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @CreatedDate
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    // ==================== 생성자 ====================
    
    @Builder
    public CoinTransaction(User user, Integer amount) {
        this.user = user;
        this.amount = amount;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 거래가 지급인지 확인
     */
    public boolean isCredit() {
        return amount > 0;
    }

    /**
     * 거래가 사용인지 확인
     */
    public boolean isDebit() {
        return amount < 0;
    }
}
