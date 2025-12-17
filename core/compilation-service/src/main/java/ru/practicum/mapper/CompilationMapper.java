package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.model.Compilation;

import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation newCompilationDtoToCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .eventIds(dto.getEvents() != null ? Set.copyOf(dto.getEvents()) : Set.of())
                .build();
    }

    public static Compilation newCompilationDtoToCompilation(NewCompilationDto dto, Set<Long> eventIds) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .eventIds(eventIds)
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(List.of())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }

    public static Compilation updateRequestToCompilation(UpdateCompilationRequest request,
                                                         Compilation existingCompilation) {
        Compilation.CompilationBuilder builder = Compilation.builder()
                .id(existingCompilation.getId())
                .title(request.getTitle() != null ? request.getTitle() : existingCompilation.getTitle())
                .pinned(request.getPinned() != null ? request.getPinned() : existingCompilation.getPinned());

        // Обрабатываем события
        if (request.getEvents() != null) {
            builder.eventIds(Set.copyOf(request.getEvents()));
        } else {
            builder.eventIds(existingCompilation.getEventIds());
        }

        return builder.build();
    }

    public static Set<Long> toEventIdsSet(List<Long> eventIds) {
        return eventIds != null ? Set.copyOf(eventIds) : Set.of();
    }

    public static List<Long> toEventIdsList(Set<Long> eventIds) {
        return eventIds != null ? List.copyOf(eventIds) : List.of();
    }
}
