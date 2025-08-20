package com.fivlo.fivlo_backend.domain.growth.service;

import com.fivlo.fivlo_backend.domain.growth.dto.*;
import com.fivlo.fivlo_backend.domain.growth.entity.GrowthAlbum;
import com.fivlo.fivlo_backend.domain.growth.repository.GrowthAlbumRepository;
import com.fivlo.fivlo_backend.domain.task.entity.Task;
import com.fivlo.fivlo_backend.domain.task.repository.TaskRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 성장앨범 서비스
 * 성장앨범 관리에 관한 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GrowthAlbumService {

    private final GrowthAlbumRepository growthAlbumRepository;
    private final TaskRepository taskRepository;

    /**
     * API 18: Task 완료 후 성장앨범 항목 생성
     * 성장앨범 연동 Task 완료 시 사진과 메모를 저장합니다.
     */
    @Transactional
    public GrowthAlbumCreateResponse createGrowthAlbum(User user, Long taskId, GrowthAlbumCreateRequest request) {
        log.info("성장앨범 생성 시작 - userId: {}, taskId: {}, photoUrl: {}", 
                user.getId(), taskId, request.getPhotoUrl());

        // Task 존재 및 권한 확인
        Task task = taskRepository.findByUserAndIdWithCategory(user, taskId)
                .orElseThrow(() -> {
                    log.warn("Task를 찾을 수 없음 - userId: {}, taskId: {}", user.getId(), taskId);
                    return new IllegalArgumentException("해당 Task를 찾을 수 없습니다.");
                });

        // 성장앨범 연동 여부 확인
        if (!task.getIsLinkedToGrowthAlbum()) {
            log.warn("성장앨범 연동되지 않은 Task - userId: {}, taskId: {}", user.getId(), taskId);
            throw new IllegalArgumentException("해당 Task는 성장앨범과 연동되지 않았습니다.");
        }

        // 이미 성장앨범이 존재하는지 확인 (1:1 관계)
        Optional<GrowthAlbum> existingAlbum = growthAlbumRepository.findByTask(task);
        if (existingAlbum.isPresent()) {
            log.warn("이미 성장앨범이 존재함 - userId: {}, taskId: {}, albumId: {}", 
                    user.getId(), taskId, existingAlbum.get().getId());
            throw new IllegalArgumentException("해당 Task의 성장앨범이 이미 존재합니다.");
        }

        // 성장앨범 생성
        GrowthAlbum growthAlbum = GrowthAlbum.builder()
                .task(task)
                .photoUrl(request.getPhotoUrl())
                .memo(request.getMemo())
                .build();

        GrowthAlbum savedAlbum = growthAlbumRepository.save(growthAlbum);

        log.info("성장앨범 생성 완료 - userId: {}, taskId: {}, albumId: {}", 
                user.getId(), taskId, savedAlbum.getId());

        return GrowthAlbumCreateResponse.success(savedAlbum);
    }

    /**
     * API 19: 성장앨범 캘린더 보기 조회
     * 특정 월의 성장앨범을 캘린더 형식으로 조회합니다.
     */
    public GrowthAlbumCalendarResponse getGrowthAlbumCalendar(User user, int year, int month) {
        log.info("성장앨범 캘린더 조회 시작 - userId: {}, year: {}, month: {}", 
                user.getId(), year, month);

        List<GrowthAlbum> albums = growthAlbumRepository.findByUserAndYearMonth(user, year, month);

        List<GrowthAlbumCalendarResponse.CalendarEntry> entries = albums.stream()
                .map(album -> GrowthAlbumCalendarResponse.CalendarEntry.builder()
                        .date(album.getCreatedAt().toLocalDate())
                        .thumbnailUrl(generateThumbnailUrl(album.getPhotoUrl()))
                        .albumId(album.getId())
                        .build())
                .collect(Collectors.toList());

        log.info("성장앨범 캘린더 조회 완료 - userId: {}, year: {}, month: {}, 항목 수: {}", 
                user.getId(), year, month, entries.size());

        return GrowthAlbumCalendarResponse.builder()
                .albumEntries(entries)
                .build();
    }

    /**
     * API 20: 성장앨범 카테고리별 보기 조회
     * 성장앨범 항목들을 카테고리별로 그룹화하여 조회합니다.
     */
    public GrowthAlbumCategoryResponse getGrowthAlbumByCategories(User user) {
        log.info("성장앨범 카테고리별 조회 시작 - userId: {}", user.getId());

        List<GrowthAlbum> albums = growthAlbumRepository.findByUserGroupByCategory(user);

        // 카테고리별로 그룹화
        Map<String, List<GrowthAlbum>> groupedByCategory = albums.stream()
                .filter(album -> album.getTask().getCategory() != null)
                .collect(Collectors.groupingBy(
                        album -> album.getTask().getCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // DTO 변환
        List<GrowthAlbumCategoryResponse.CategoryGroup> categoryGroups = groupedByCategory.entrySet().stream()
                .map(entry -> {
                    String categoryName = entry.getKey();
                    List<GrowthAlbum> categoryAlbums = entry.getValue();
                    
                    // 카테고리 ID (첫 번째 앨범의 카테고리 ID 사용)
                    Long categoryId = categoryAlbums.get(0).getTask().getCategory().getId();

                    List<GrowthAlbumCategoryResponse.AlbumPhoto> albumPhotos = categoryAlbums.stream()
                            .map(album -> GrowthAlbumCategoryResponse.AlbumPhoto.builder()
                                    .thumbnailUrl(generateThumbnailUrl(album.getPhotoUrl()))
                                    .albumId(album.getId())
                                    .build())
                            .collect(Collectors.toList());

                    return GrowthAlbumCategoryResponse.CategoryGroup.builder()
                            .id(categoryId)
                            .name(categoryName)
                            .albumPhotos(albumPhotos)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("성장앨범 카테고리별 조회 완료 - userId: {}, 카테고리 수: {}", 
                user.getId(), categoryGroups.size());

        return GrowthAlbumCategoryResponse.builder()
                .categories(categoryGroups)
                .build();
    }

    /**
     * API 21: 특정 성장앨범 항목 상세 조회
     * 사용자가 사진을 클릭했을 때, 원본 사진과 메모 등 상세 정보를 조회합니다.
     */
    public GrowthAlbumDetailResponse getGrowthAlbumDetail(User user, Long albumId) {
        log.info("성장앨범 상세 조회 시작 - userId: {}, albumId: {}", user.getId(), albumId);

        GrowthAlbum album = growthAlbumRepository.findByUserAndId(user, albumId)
                .orElseThrow(() -> {
                    log.warn("성장앨범을 찾을 수 없음 - userId: {}, albumId: {}", user.getId(), albumId);
                    return new IllegalArgumentException("해당 성장앨범을 찾을 수 없습니다.");
                });

        log.info("성장앨범 상세 조회 완료 - userId: {}, albumId: {}", user.getId(), albumId);

        return GrowthAlbumDetailResponse.from(album);
    }

    /**
     * 썸네일 URL 생성
     * 현재는 원본 URL을 그대로 반환하지만, 향후 S3 Lambda로 썸네일 생성 가능
     */
    private String generateThumbnailUrl(String originalUrl) {
        // TODO: 향후 AWS Lambda로 썸네일 생성 로직 구현
        return originalUrl;
    }
}

