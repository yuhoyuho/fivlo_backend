package com.fivlo.fivlo_backend.domain.oboone.repository;

import com.fivlo.fivlo_backend.domain.oboone.entity.ObooniItem;
import com.fivlo.fivlo_backend.domain.oboone.entity.UserItem;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    // 사용자가 소유한 모든 아이템 조회
    List<UserItem> findByUser(User user);

    // 사용자가 특정 아이템을 이미 소유했는지 확인
    boolean existsByUserAndObooniItem(User user, ObooniItem item);

    // 특정 타입의 착용 중인 아이템 조회
    Optional<UserItem> findByUserAndObooniItem_ItemTypeAndIsEquipped(User user, ObooniItem.ItemType itemType, boolean isEquipped);

    // UserItem을 조회할 때 연관된 ObooniItem도 함께 즉시 로딩합니다.
    @Query("SELECT ui FROM UserItem ui JOIN FETCH ui.obooniItem WHERE ui.user = :user")
    List<UserItem> findByUserWithObooniItem(@Param("user") User user);
}
