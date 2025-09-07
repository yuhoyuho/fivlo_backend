package com.fivlo.fivlo_backend.common.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoalAnalysisRequestDto {

    private String goalContent;

    private String goalType; // 'INDEFINITE' 또는 'DEFINITE'

    private String startDate;

    private String endDate;
}
