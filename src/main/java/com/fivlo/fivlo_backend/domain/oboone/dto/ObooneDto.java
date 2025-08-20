package com.fivlo.fivlo_backend.domain.oboone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fivlo.fivlo_backend.domain.oboone.entity.ObooniItem;

import java.util.List;

public class ObooneDto {

    // API 36 : 상점 아이템 조회 응답
    public record ShopItemResponse(
            Long id,
            String name,
            Integer price,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("item_type") ObooniItem.ItemType itemType
    ) {}

    public record ShopItemListResponse(
            List<ShopItemResponse> items
    ) {}

    // API 36-2 : 아이템 생성 요청
    public record addItemRequest(
            String name,
            Integer price,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("item_type") ObooniItem.ItemType itemType
    ) {}

    // API 37 : 아이템 구매 요청
    public record PurchaseRequest(
            @JsonProperty("oboone_item_id") Long obooniItemId
    ) {}

    // API 37 : 아이템 구매 응답
    public record PurchaseResponse(
            String message,
            @JsonProperty("new_total_coins") Integer newTotalCoins
    ) {}

    // API 38 : 사용자 옷장 아이템 목록 조회 응답 (개별 아이템)
    public record ClosetItemResponse(
            Long id, // 사용자 id
            String name,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("is_equipped") boolean isEquipped
    ) {}

    // API 38 : 사용자 옷장 아이템 목록 최종 응답 (악세사리 포함 -> 현재는 없음)
    public record ClosetResponse(
            @JsonProperty("clothing_items") List<ClosetItemResponse> clothingItems,
            @JsonProperty("accessory_items") List<ClosetItemResponse> accessoryItems
    ) {}
}
