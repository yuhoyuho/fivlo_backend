package com.fivlo.fivlo_backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoalAnalysisRequestDto {

    @JsonProperty("goal_content")
    private String goalContent;

    @JsonProperty("goal_type")
    private String goalType; // 'INDEFINITE' 또는 'DEFINITE'

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;
}
