package com.fivlo.fivlo_backend.common.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AITaskDto {

    private String content;

    private String dueDate;

    // "DAILY"
    private String repeatType;

    private String endDate; // null 또는 "YYYY-MM-DD"

    private Boolean isLinkedToGrowthAlbum;
}
