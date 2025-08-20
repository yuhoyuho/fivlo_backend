package com.fivlo.fivlo_backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AITaskDto {

    private String content;

    @JsonProperty("due_date")
    private String dueDate;

    @JsonProperty("repeat_type") // "DAILY"
    private String repeatType;

    @JsonProperty("end_date")
    private String endDate; // null 또는 "YYYY-MM-DD"

    @JsonProperty("is_linked_to_growth_album")
    private Boolean isLinkedToGrowthAlbum;
}
