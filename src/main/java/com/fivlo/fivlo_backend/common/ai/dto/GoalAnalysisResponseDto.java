package com.fivlo.fivlo_backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GoalAnalysisResponseDto {

    @JsonProperty("recommended_tasks")
    private List<RecommendedTaskDto> recommendedTasks;
}
