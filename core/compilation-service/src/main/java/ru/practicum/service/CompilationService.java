package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        return compilationRepository.findCompilations(pinned, from, size)
                .stream()
                .map(CompilationMapper::toCompilationDto)
                .toList();
    }

    public CompilationDto getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .map(CompilationMapper::toCompilationDto)
                .orElse(null);
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        if (!compilationRepository.findByTitleIgnoreCase(compilationDto.getTitle()).isEmpty()) {
            throw new RuntimeException("Подборка с названием " + compilationDto.getTitle() + " уже существует");
        }

        Set<Event> events = new HashSet<>();
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            List<Event> existingEvents = eventRepository.findAllById(compilationDto.getEvents());

            Set<Long> existingIds = existingEvents.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());

            Set<Event> stubEvents = compilationDto.getEvents().stream()
                    .filter(eventId -> !existingIds.contains(eventId))
                    .map(eventId -> Event.builder()
                            .id(eventId)
                            .title("Event " + eventId)
                            .annotation("Event description " + eventId)
                            .eventDate(LocalDateTime.now().plusDays(eventId % 30))
                            .paid(eventId % 2 == 0)
                            .state("PUBLISHED")
                            .build())
                    .collect(Collectors.toSet());

            eventRepository.saveAll(stubEvents);

            events.addAll(existingEvents);
            events.addAll(stubEvents);
        }

        return CompilationMapper.toCompilationDto(
                compilationRepository.save(
                        CompilationMapper.newCompilationDtoToCompilation(compilationDto, events)
                )
        );
    }

    @Transactional
    public void deleteCompilation(Long compilationId) {
        compilationRepository.findById(compilationId)
                .orElseThrow(() -> new RuntimeException("Подборка c id=" + compilationId + " не найдена"));
        compilationRepository.deleteById(compilationId);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new RuntimeException("Подборка с id=" + compilationId + " не найдена"));

        if (updateCompilationRequest.getTitle() != null) {
            if (!compilationRepository.findByTitleIgnoreCase(updateCompilationRequest.getTitle()).isEmpty() &&
                    !compilation.getTitle().equalsIgnoreCase(updateCompilationRequest.getTitle())) {
                throw new RuntimeException("Подборка с названием " + updateCompilationRequest.getTitle() + " уже существует");
            }
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>();
            if (!updateCompilationRequest.getEvents().isEmpty()) {
                List<Event> existingEvents = eventRepository.findAllById(updateCompilationRequest.getEvents());

                Set<Long> existingIds = existingEvents.stream()
                        .map(Event::getId)
                        .collect(Collectors.toSet());

                Set<Event> stubEvents = updateCompilationRequest.getEvents().stream()
                        .filter(eventId -> !existingIds.contains(eventId))
                        .map(eventId -> Event.builder()
                                .id(eventId)
                                .title("Event " + eventId)
                                .annotation("Event description " + eventId)
                                .eventDate(LocalDateTime.now().plusDays(eventId % 30))
                                .paid(eventId % 2 == 0)
                                .state("PUBLISHED")
                                .build())
                        .collect(Collectors.toSet());

                eventRepository.saveAll(stubEvents);

                events.addAll(existingEvents);
                events.addAll(stubEvents);
            }
            compilation.setEvents(events);
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }
}
