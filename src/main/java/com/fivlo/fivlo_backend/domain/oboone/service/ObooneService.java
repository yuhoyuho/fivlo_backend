package com.fivlo.fivlo_backend.domain.oboone.service;

import com.fivlo.fivlo_backend.domain.oboone.dto.ObooneDto;
import com.fivlo.fivlo_backend.domain.oboone.entity.ObooniItem;
import com.fivlo.fivlo_backend.domain.oboone.entity.UserItem;
import com.fivlo.fivlo_backend.domain.oboone.repository.ObooniItemRepository;
import com.fivlo.fivlo_backend.domain.oboone.repository.UserItemRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.domain.user.service.CoinTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObooneService {

    private final ObooniItemRepository obooniItemRepository;
    private final UserItemRepository userItemRepository;
    private final UserRepository userRepository;
    private final CoinTransactionService coinTransactionService;

    // API 36 : 상점 아이템 목록 조회
    @Transactional(readOnly = true)
    public ObooneDto.ShopItemListResponse getShopItem() {
        List<ObooneDto.ShopItemResponse> items = obooniItemRepository.findAll().stream()
                .map(item -> new ObooneDto.ShopItemResponse(
                        item.getId(), item.getName(), item.getPrice(), item.getImageUrl(), item.getItemType()))
                .toList();
        return new ObooneDto.ShopItemListResponse(items);
    }

    // API 36-2 : 상점 아이템 추가
    @Transactional
    public Long addItem(ObooneDto.addItemRequest request) {
        ObooniItem item = ObooniItem.builder()
                .name(request.name())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .itemType(request.itemType())
                .build();

        return obooniItemRepository.save(item).getId();
    }

    // API 37 : 오분이 아이템 구매
    @Transactional
    public ObooneDto.PurchaseResponse purchaseItem(Long userId, ObooneDto.PurchaseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        ObooniItem item = obooniItemRepository.findById(request.obooniItemId())
                .orElseThrow(() -> new NoSuchElementException("해당 아이템을 찾을 수 없습니다."));

        if(userItemRepository.existsByUserAndObooniItem(user, item)) {
            throw new IllegalArgumentException("이미 가지고 있는 아이템입니다.");
        }

        if(!user.useCoins(item.getPrice())) {
            throw new IllegalArgumentException("코인이 부족합니다.");
        }

        coinTransactionService.logTransaction(user, item.getPrice() * -1);

        UserItem userItem = UserItem.builder()
                .user(user)
                .obooniItem(item)
                .build();
        userItemRepository.save(userItem);

        return new ObooneDto.PurchaseResponse("아이템 구매가 완료되었습니다.", user.getTotalCoins());
    }

    // API 38 : 사용자가 소유한 아이템 목록 조회
    @Transactional(readOnly = true)
    public ObooneDto.ClosetResponse getCloset(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        // 사용자가 소유한 모든 아이템 조회
        List<UserItem> userItems = userItemRepository.findByUserWithObooniItem(user);

        List<ObooneDto.ClosetItemResponse> clothingItems = new ArrayList<>();
        List<ObooneDto.ClosetItemResponse> accessoryItems = new ArrayList<>();

        // 3. 단순한 for-loop를 사용하여 아이템을 분류하고 DTO로 변환합니다.
        for (UserItem userItem : userItems) {
            ObooneDto.ClosetItemResponse dto = new ObooneDto.ClosetItemResponse(
                    userItem.getId(),
                    userItem.getObooniItem().getName(),
                    userItem.getObooniItem().getImageUrl(),
                    userItem.getIsEquipped()
            );

            if (userItem.getObooniItem().isClothing()) {
                clothingItems.add(dto);
            } else if (userItem.getObooniItem().isAccessory()) {
                accessoryItems.add(dto);
            }
        }

        // 4. 최종 응답 DTO를 생성하여 반환합니다.
        return new ObooneDto.ClosetResponse(clothingItems, accessoryItems);
    }

    // API 39 : 아이템 착용
    @Transactional
    public String equipItem(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        UserItem itemToEquip = findUserItemAndVerifyOwnership(userId, itemId);

        // 같은 타입의 기존 착용 아이템이 있다면 해제 (모자를 쓰고 있다면 그걸 벗기고 새로운 모자 착용)
        userItemRepository.findByUserAndObooniItem_ItemTypeAndIsEquipped(user, itemToEquip.getObooniItem().getItemType(), true)
                .ifPresent(UserItem::unequip);

        itemToEquip.equip();
        return "아이템 착용이 완료되었습니다.";
    }

    // API 40 : 아이템 착용 해제
    @Transactional
    public String unequipItem(Long userId, Long itemId) {
        UserItem itemToUnequip = findUserItemAndVerifyOwnership(userId, itemId);
        itemToUnequip.unequip();
        return "아이템 착용이 해제되었습니다.";
    }

    // ------- 헬퍼 메서드 ------- //
    private UserItem findUserItemAndVerifyOwnership(Long userId, Long itemId) {
        UserItem userItem = userItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("해당 아이템을 찾을 수 없습니다."));
        if (!userItem.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 아이템에 대한 소유권이 없습니다.");
        }
        return userItem;
    }
}
