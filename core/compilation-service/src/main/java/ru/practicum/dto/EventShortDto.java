package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private LocalDateTime eventDate;
    private Boolean paid;
    private String state;
    private Long views;
    private Long confirmedRequests;
    // private Long categoryId;
    // private Long initiatorId;
    // private String description;
    // private Integer participantLimit;
    // private Boolean requestModeration;
    // private LocalDateTime createdOn;
    // private LocalDateTime publishedOn;
    // private Double lat;
    // private Double lon;
}