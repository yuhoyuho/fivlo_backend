package com.fivlo.fivlo_backend.domain.oboone.entity;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 아이템 엔티티
 * 어떤 사용자가 어떤 아이템을 소유하고 있는지, 그리고 현재 착용 중인지 여부를 추적
 */
@Entity
@Table(name = "user_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obooni_item_id", nullable = false)
    private ObooniItem obooniItem;

    @Column(name = "is_equipped", nullable = false)
    private Boolean isEquipped = false;

    // ==================== 생성자 ====================
    
    @Builder
    public UserItem(User user, ObooniItem obooniItem, Boolean isEquipped) {
        this.user = user;
        this.obooniItem = obooniItem;
        this.isEquipped = isEquipped != null ? isEquipped : false;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 아이템 착용
     */
    public void equip() {
        this.isEquipped = true;
    }

    /**
     * 아이템 착용 해제
     */
    public void unequip() {
        this.isEquipped = false;
    }

    /**
     * 착용 상태 변경
     */
    public void updateEquipStatus(Boolean isEquipped) {
        this.isEquipped = isEquipped != null ? isEquipped : false;
    }

    /**
     * 현재 착용 중인지 확인
     */
    public boolean isCurrentlyEquipped() {
        return isEquipped;
    }
}
