package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInfoDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private String state;
    private Long categoryId;
    private Long initiatorId;
    private Long participantLimit;
    private Long confirmedRequests;
    private Boolean paid;
    private Boolean requestModeration;
}
