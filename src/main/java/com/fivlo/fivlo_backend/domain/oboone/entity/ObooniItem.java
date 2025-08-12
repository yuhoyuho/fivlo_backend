package com.fivlo.fivlo_backend.domain.oboone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오분이 아이템 엔티티
 * 오분이 상점에서 판매하는 모든 옷과 액세서리 아이템의 정보를 저장
 */
@Entity
@Table(name = "obooni_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ObooniItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    // ==================== 생성자 ====================
    
    @Builder
    public ObooniItem(String name, Integer price, String imageUrl, ItemType itemType) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.itemType = itemType;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 아이템 정보 수정
     */
    public void update(String name, Integer price, String imageUrl, ItemType itemType) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (price != null && price > 0) {
            this.price = price;
        }
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrl = imageUrl;
        }
        if (itemType != null) {
            this.itemType = itemType;
        }
    }

    /**
     * 옷 아이템인지 확인
     */
    public boolean isClothing() {
        return itemType == ItemType.CLOTHING;
    }

    /**
     * 액세서리 아이템인지 확인
     */
    public boolean isAccessory() {
        return itemType == ItemType.ACCESSORY;
    }

    // ==================== Enum 클래스 ====================
    
    /**
     * 아이템 종류
     */
    public enum ItemType {
        CLOTHING("옷"),
        ACCESSORY("액세서리");

        private final String description;

        ItemType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
