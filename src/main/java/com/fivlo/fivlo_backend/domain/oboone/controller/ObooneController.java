package com.fivlo.fivlo_backend.domain.oboone.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.oboone.dto.ObooneDto;
import com.fivlo.fivlo_backend.domain.oboone.service.ObooneService;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ObooneController {

    private final ObooneService obooneService;

    /** 오분이 상점 아이템 목록 조회
     * HTTP : GET
     * EndPoint : /api/v1/oboone/shop
     */
    @GetMapping(Routes.OBOONE_SHOP)
    public ResponseEntity<ObooneDto.ShopItemListResponse> getShopItems() {
        return ResponseEntity.ok(obooneService.getShopItem());
    }

    /** 오분이 상점 아이템 추가
     * HTTP : POST
     * EndPoint : /api/v1/oboone/item
     */
    @PostMapping(Routes.OBOONE_ITEM)
    public ResponseEntity<Long> addItem(
            @RequestBody ObooneDto.addItemRequest request) {
        return ResponseEntity.status(201).body(obooneService.addItem(request));
    }

    /** 오분이 아이템 구매
     * HTTP : POST
     * EndPoint : /api/v1/oboone/purchase
     */
    @PostMapping("/api/v1/oboone/purchase")
    public ResponseEntity<ObooneDto.PurchaseResponse> purchaseItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ObooneDto.PurchaseRequest request) {

        return ResponseEntity.ok(obooneService.purchaseItem(userDetails.getUser().getId(), request));
    }

    /** 사용자가 소유한 아이템 목록 조회
     * HTTP : GET
     * EndPoint : /api/v1/oboone/closet
     */
    @GetMapping(Routes.OBOONE_CLOSET)
    public ResponseEntity<ObooneDto.ClosetResponse> getCloset(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(obooneService.getCloset(userDetails.getUser().getId()));
    }

    /** 오분이 아이템 착용
     * HTTP : PATCH
     * EndPoint : /api/v1/oboone/equip/{userItemId}
     */
    @PatchMapping(Routes.OBOONE_EQUIP + "/{userItemId}")
    public ResponseEntity<String> equipItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userItemId) {
        return ResponseEntity.ok(obooneService.equipItem(userDetails.getUser().getId(), userItemId));
    }

    /** 오분이 아이템 착용 해제
     * HTTP : PATCH
     * EndPoint : /api/v1/oboone/unequip/{userItemId}
     */
    @PatchMapping(Routes.OBOONE_UNEQUIP + "/{userItemId}")
    public ResponseEntity<String> unequipItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userItemId) {
        return ResponseEntity.ok(obooneService.unequipItem(userDetails.getUser().getId(), userItemId));
    }
}
