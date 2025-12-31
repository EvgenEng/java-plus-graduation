package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilationPage;
        PageRequest pageRequest = PageRequest.of(from / size, size);

        if (pinned != null) {
            compilationPage = compilationRepository.findAllByPinned(pinned, pageRequest);
        } else {
            compilationPage = compilationRepository.findAll(pageRequest);
        }

        List<Compilation> compilations = compilationPage.getContent();

        // Обогащаем каждую подборку событиями через Feign
        return compilations.stream()
                .map(this::enrichCompilationWithEvents)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Подборка с id=" + compId + " не найдена"));

        return enrichCompilationWithEvents(compilation);
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        // Проверка уникальности названия
        if (!compilationRepository.findByTitleIgnoreCase(compilationDto.getTitle()).isEmpty()) {
            throw new ConditionsNotMetException(
                    "Подборка с названием " + compilationDto.getTitle() + " уже существует");
        }

        // Валидация событий через event-service
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            validateEventsExist(compilationDto.getEvents());
        }

        // Создаем подборку с ID событий
        Compilation compilation = Compilation.builder()
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned() != null ? compilationDto.getPinned() : false)
                .eventIds(compilationDto.getEvents() != null ?
                        new HashSet<>(compilationDto.getEvents()) : new HashSet<>())
                .build();

        Compilation savedCompilation = compilationRepository.save(compilation);

        return enrichCompilationWithEvents(savedCompilation);
    }

    @Transactional
    public void deleteCompilation(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new EntityNotFoundException(
                    "Подборка с id=" + compilationId + " не найдена");
        }
        compilationRepository.deleteById(compilationId);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Подборка с id=" + compilationId + " не найдена"));

        // Обновление названия
        if (updateRequest.getTitle() != null) {
            List<Compilation> existingWithTitle = compilationRepository
                    .findByTitleIgnoreCase(updateRequest.getTitle());

            if (!existingWithTitle.isEmpty() &&
                    !existingWithTitle.get(0).getId().equals(compilationId)) {
                throw new ConditionsNotMetException(
                        "Подборка с названием " + updateRequest.getTitle() + " уже существует");
            }
            compilation.setTitle(updateRequest.getTitle());
        }

        // Обновление событий
        if (updateRequest.getEvents() != null) {
            if (!updateRequest.getEvents().isEmpty()) {
                validateEventsExist(updateRequest.getEvents());
            }
            compilation.setEventIds(new HashSet<>(updateRequest.getEvents()));
        }

        // Обновление pinned статуса
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        return enrichCompilationWithEvents(updatedCompilation);
    }

    private void validateEventsExist(List<Long> eventIds) {
        try {
            List<Long> existingEventIds = eventClient.getExistingEventIds(eventIds);

            if (existingEventIds.size() != eventIds.size()) {
                // Находим ID несуществующих событий
                Set<Long> existingSet = new HashSet<>(existingEventIds);
                List<Long> nonExisting = eventIds.stream()
                        .filter(id -> !existingSet.contains(id))
                        .collect(Collectors.toList());

                throw new ConditionsNotMetException(
                        "Следующие события не существуют: " + nonExisting);
            }
        } catch (Exception e) {
            // Если event-service недоступен, можно либо:
            // 1. Бросить исключение
            // 2. Пропустить проверку (не рекомендуется)
            // 3. Использовать fallback
            throw new ConditionsNotMetException(
                    "Не удалось проверить существование событий: " + e.getMessage());
        }
    }

    private CompilationDto enrichCompilationWithEvents(Compilation compilation) {
        List<EventShortDto> events = List.of();

        if (!compilation.getEventIds().isEmpty()) {
            try {
                events = eventClient.getEventsShortByIds(
                        new ArrayList<>(compilation.getEventIds()));
            } catch (Exception e) {
                events = List.of();
                log.warn("Не удалось получить события для подборки {}: {}",
                        compilation.getId(), e.getMessage());
            }
        }

        return CompilationMapper.toCompilationDto(compilation, events);
    }
}
