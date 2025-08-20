package com.fivlo.fivlo_backend.domain.growth.repository;

import com.fivlo.fivlo_backend.domain.growth.entity.GrowthAlbum;
import com.fivlo.fivlo_backend.domain.task.entity.Task;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 성장앨범 리포지토리
 * 성장앨범 데이터 접근을 위한 인터페이스
 */
@Repository
public interface GrowthAlbumRepository extends JpaRepository<GrowthAlbum, Long> {

    /**
     * 특정 Task의 성장앨범 조회
     */
    Optional<GrowthAlbum> findByTask(Task task);

    /**
     * 특정 사용자의 특정 월 성장앨범 조회 (캘린더용)
     */
    @Query("SELECT ga FROM GrowthAlbum ga " +
           "JOIN ga.task t " +
           "WHERE t.user = :user " +
           "AND EXTRACT(YEAR FROM ga.createdAt) = :year " +
           "AND EXTRACT(MONTH FROM ga.createdAt) = :month " +
           "ORDER BY ga.createdAt ASC")
    List<GrowthAlbum> findByUserAndYearMonth(@Param("user") User user, 
                                           @Param("year") int year, 
                                           @Param("month") int month);

    /**
     * 특정 사용자의 성장앨범을 카테고리별로 조회
     */
    @Query("SELECT ga FROM GrowthAlbum ga " +
           "JOIN FETCH ga.task t " +
           "JOIN FETCH t.category c " +
           "WHERE t.user = :user " +
           "ORDER BY c.name ASC, ga.createdAt DESC")
    List<GrowthAlbum> findByUserGroupByCategory(@Param("user") User user);

    /**
     * 특정 사용자의 특정 성장앨범 조회
     */
    @Query("SELECT ga FROM GrowthAlbum ga " +
           "JOIN ga.task t " +
           "WHERE t.user = :user AND ga.id = :albumId")
    Optional<GrowthAlbum> findByUserAndId(@Param("user") User user, @Param("albumId") Long albumId);
}
