package ru.practicum.api_controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.CompilationClient;
import ru.practicum.entities.compilation.model.dto.CompilationDto;
import ru.practicum.entities.compilation.model.dto.NewCompilationDto;
import ru.practicum.entities.compilation.model.dto.UpdateCompilationRequest;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationClient compilationClient;

    // Создание подборки событий
    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> createCompilation(
            @Valid @RequestBody NewCompilationDto compilationDto) {
        CompilationDto createdCompilation = compilationClient.createCompilation(compilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompilation);
    }

    // Удаление подборки событий
    @DeleteMapping("/admin/compilations/{comId}")
    public ResponseEntity<Void> deleteCompilation(
            @PathVariable Long comId) {
        compilationClient.deleteCompilation(comId);
        return ResponseEntity.noContent().build();
    }

    // Обновление подборки событий
    @PatchMapping("/admin/compilations/{comId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable Long comId,
            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        CompilationDto updatedCompilation = compilationClient.updateCompilation(comId, updateCompilationRequest);
        return ResponseEntity.ok(updatedCompilation);
    }

    // Получение подборок событий
    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<CompilationDto> compilations = compilationClient.getCompilations(pinned, from, size);
        return ResponseEntity.ok(compilations);
    }

    // Получение подборки событий по ID
    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(@PathVariable Long compId) {
        CompilationDto compilation = compilationClient.getCompilationById(compId);
        return ResponseEntity.ok(compilation);
    }
}
