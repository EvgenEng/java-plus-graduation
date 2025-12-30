package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        return categoryRepository.findAllCategories(from, size).stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    /*public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория с id=" + catId + " не найдена"));
        return CategoryMapper.toCategoryDto(category);
    }
     */
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория с id=" + catId + " не найдена"));
        return CategoryMapper.toCategoryDto(category); // ★ Используй toCategoryDto
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (!categoryRepository.findByNameIgnoreCase(categoryDto.getName()).isEmpty()) {
            throw new ConflictException(
                    "Категория с именем " + categoryDto.getName() + " уже существует");
        }

        Category category = CategoryMapper.toCategory(categoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    /*@Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория с id=" + catId + " не найдена"));

        categoryRepository.delete(category);
    }
    */
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Удаление категории: id={}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория с id=" + catId + " не найдена"));

        boolean hasEvents = categoryRepository.existsById(catId);

        throw new ConflictException(
                "Нельзя удалить категорию с привязанными событиями");
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория с id=" + id + " не найдена"));

        List<Category> existingCategories = categoryRepository.findByNameIgnoreCase(categoryDto.getName());
        if (!existingCategories.isEmpty() && !existingCategories.get(0).getId().equals(id)) {
            throw new ConflictException(
                    "Категория с именем " + categoryDto.getName() + " уже существует");
        }

        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }
}
