package ru.practicum.entities.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.centralRepository.CompilationRepository;
import ru.practicum.centralRepository.EventRepository;
import ru.practicum.entities.compilation.model.Compilation;
import ru.practicum.entities.compilation.model.dto.CompilationDto;
import ru.practicum.entities.compilation.model.dto.NewCompilationDto;
import ru.practicum.entities.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.entities.compilation.model.mapper.CompilationMapper;
import ru.practicum.entities.event.model.Event;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        return compilationRepository.findCompilations(pinned, from, size).stream().map(CompilationMapper::toCompilationDto).toList();
    }

    public CompilationDto getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .map(CompilationMapper::toCompilationDto)
                .orElse(null);
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        if (!compilationRepository.findByTitleIgnoreCase(compilationDto.getTitle()).isEmpty()) {
            throw new ConditionsNotMetException("Подборка с названием " + compilationDto.getTitle() + " уже существует");
        }

        Set<Event> events = new HashSet<>();
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllByIdIn(compilationDto.getEvents().stream().toList()));
        }

        return CompilationMapper.toCompilationDto(
                compilationRepository.save(CompilationMapper.newCompilationDtoToCompilation(compilationDto, events))
        );
    }

    @Transactional
    public void deleteCompilation(Long compilationId) {
        compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка c id=" + compilationId + " не найдена"));
        compilationRepository.deleteById(compilationId);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка с id=" + compilationId + " не найдена"));

        if (updateCompilationRequest.getTitle() != null) {
            if (!compilationRepository.findByTitleIgnoreCase(updateCompilationRequest.getTitle()).isEmpty() &&
                    !compilation.getTitle().equalsIgnoreCase(updateCompilationRequest.getTitle())) {
                throw new ConditionsNotMetException("Подборка с названием " + updateCompilationRequest.getTitle() + " уже существует");
            }
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>();
            if (!updateCompilationRequest.getEvents().isEmpty()) {
                events = new HashSet<>(eventRepository.findAllByIdIn(updateCompilationRequest.getEvents().stream().toList()));
            }
            compilation.setEvents(events);
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

}