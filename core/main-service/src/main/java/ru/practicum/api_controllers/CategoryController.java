package ru.practicum.api_controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.client.CategoryClient;
import ru.practicum.dto.CategoryDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryClient categoryClient;

    // Создание категории
    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("Создание категории через Feign: {}", categoryDto.getName());
        CategoryDto createdCategory = categoryClient.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    // Удаление категории
    @DeleteMapping("/admin/categories/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        log.info("Удаление категории через Feign: ID={}", catId);
        categoryClient.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }

    // Обновление категории
    @PatchMapping("/admin/categories/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Обновление категории через Feign: ID={}", catId);
        CategoryDto updatedCategory = categoryClient.updateCategory(catId, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }

    // Получение всех категорий (публичный доступ)
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Получение всех категорий через Feign: from={}, size={}", from, size);
        List<CategoryDto> categories = categoryClient.getAllCategories(from, size);
        return ResponseEntity.ok(categories);
    }

    // Получение категории по ID (публичный доступ)
    @GetMapping("/categories/{catId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long catId) {
        log.info("Получение категории по ID через Feign: ID={}", catId);
        CategoryDto category = categoryClient.getCategoryById(catId);
        return ResponseEntity.ok(category);
    }
}
