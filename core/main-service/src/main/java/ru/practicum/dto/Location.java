package ru.practicum.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @NotNull(message = "Широта не может быть null")
    @DecimalMin(value = "-90.0", message = "Широта должна быть в диапазоне от -90 до 90")
    @DecimalMax(value = "90.0", message = "Широта должна быть в диапазоне от -90 до 90")
    private Double lat;

    @NotNull(message = "Долгота не может быть null")
    @DecimalMin(value = "-180.0", message = "Долгота должна быть в диапазоне от -180 до 180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть в диапазоне от -180 до 180")
    private Double lon;
}
