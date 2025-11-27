package ru.practicum.api_controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.entities.compilation.model.dto.CompilationDto;
import ru.practicum.entities.compilation.model.dto.NewCompilationDto;
import ru.practicum.entities.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.entities.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compilationService;

    // Создание подборки событий
    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> createCompilation(
            @Valid @RequestBody NewCompilationDto compilationDto) {
        CompilationDto createdCompilation = compilationService.createCompilation(compilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompilation);
    }

    // Удаление подборки событий
    @DeleteMapping("/admin/compilations/{comId}")
    public ResponseEntity<Void> deleteCompilation(
            @PathVariable Long comId) {
        compilationService.deleteCompilation(comId);
        return ResponseEntity.noContent().build();
    }

    // Обновление подборки событий
    @PatchMapping("/admin/compilations/{comId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable Long comId,
            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        CompilationDto updatedCompilation = compilationService.updateCompilation(comId, updateCompilationRequest);
        return ResponseEntity.ok(updatedCompilation);
    }

    // Получение подборок событий
    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<CompilationDto> compilations = compilationService.getAllCompilations(pinned, from, size);
        return ResponseEntity.ok(compilations);
    }

    // Получение подборки событий по ID
    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(@PathVariable Long compId) {
        CompilationDto compilation = compilationService.getCompilationById(compId);
        return ResponseEntity.ok(compilation);
    }
}
