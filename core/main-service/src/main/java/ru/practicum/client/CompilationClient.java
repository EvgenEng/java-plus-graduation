package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;

import java.util.List;

@FeignClient(name = "compilation-service")
public interface CompilationClient {

    @PostMapping("/admin/compilations")
    CompilationDto createCompilation(@RequestBody NewCompilationDto compilationDto);

    @DeleteMapping("/admin/compilations/{comId}")
    void deleteCompilation(@PathVariable Long comId);

    @PatchMapping("/admin/compilations/{comId}")
    CompilationDto updateCompilation(
            @PathVariable Long comId,
            @RequestBody UpdateCompilationRequest updateCompilationRequest);

    @GetMapping("/compilations")
    List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    @GetMapping("/compilations/{compId}")
    CompilationDto getCompilationById(@PathVariable Long compId);
}
