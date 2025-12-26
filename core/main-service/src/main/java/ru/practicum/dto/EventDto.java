package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull
    private CategoryDto category;

    @NotNull
    private UserDto initiator;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;
    private String state;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private Long views;
    private Integer confirmedRequests;
}
