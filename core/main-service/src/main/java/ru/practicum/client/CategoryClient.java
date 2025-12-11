package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.entities.category.model.dto.CategoryDto;

import java.util.List;

@FeignClient(name = "category-service")
public interface CategoryClient {

    @GetMapping("/categories")
    List<CategoryDto> getAllCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/categories/{catId}")
    CategoryDto getCategoryById(@PathVariable("catId") Long catId);

    @PostMapping("/admin/categories")
    CategoryDto createCategory(@RequestBody CategoryDto categoryDto);

    @PatchMapping("/admin/categories/{catId}")
    CategoryDto updateCategory(
            @PathVariable("catId") Long catId,
            @RequestBody CategoryDto categoryDto);

    @DeleteMapping("/admin/categories/{catId}")
    void deleteCategory(@PathVariable("catId") Long catId);
}
