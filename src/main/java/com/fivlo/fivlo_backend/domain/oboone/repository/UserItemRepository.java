package com.fivlo.fivlo_backend.domain.oboone.repository;

import com.fivlo.fivlo_backend.domain.oboone.entity.ObooniItem;
import com.fivlo.fivlo_backend.domain.oboone.entity.UserItem;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    // 사용자가 소유한 모든 아이템 조회
    List<UserItem> findByUser(User user);

    // 사용자가 특정 아이템을 이미 소유했는지 확인
    boolean existsByUserAndObooniItem(User user, ObooniItem item);

    // 특정 타입의 착용 중인 아이템 조회
    Optional<UserItem> findByUserAndObooniItem_ItemTypeAndIsEquipped(User user, ObooniItem.ItemType itemType, boolean isEquipped);
}
