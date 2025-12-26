package ru.practicum.controller;

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
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompilationController {
    private final CompilationService compilationService;

    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> createCompilation(
            @Valid @RequestBody NewCompilationDto compilationDto) {
        CompilationDto createdCompilation = compilationService.createCompilation(compilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompilation);
    }

    @DeleteMapping("/admin/compilations/{comId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long comId) {
        compilationService.deleteCompilation(comId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/compilations/{comId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable Long comId,
            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        CompilationDto updatedCompilation = compilationService.updateCompilation(comId, updateCompilationRequest);
        return ResponseEntity.ok(updatedCompilation);
    }

    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<CompilationDto> compilations = compilationService.getAllCompilations(pinned, from, size);
        return ResponseEntity.ok(compilations);
    }

    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(@PathVariable Long compId) {
        CompilationDto compilation = compilationService.getCompilationById(compId);
        return ResponseEntity.ok(compilation);
    }
}
