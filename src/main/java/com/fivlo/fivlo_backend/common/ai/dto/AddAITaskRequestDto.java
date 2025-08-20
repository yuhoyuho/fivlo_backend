package com.fivlo.fivlo_backend.common.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AddAITaskRequestDto {

    private List<AITaskDto> tasks;
}
