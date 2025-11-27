package ru.practicum.entities.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.centralRepository.CategoryRepository;
import ru.practicum.centralRepository.EventRepository;
import ru.practicum.entities.category.model.Category;
import ru.practicum.entities.category.model.dto.CategoryDto;
import ru.practicum.entities.category.model.mapper.CategoryMapper;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        return categoryRepository.findCategories(from, size).stream().map(CategoryMapper::categoryToDto).toList();
    }

    public CategoryDto getCategoryById(Long catId) {
        return CategoryMapper.categoryToDto(
                categoryRepository.findById(catId).orElseThrow(
                        () -> new NotFoundException("Категория с id=" + catId + " не найдена")
                )
        );
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto newCategoryDto) {
        if (!categoryRepository.findByNameIgnoreCase(newCategoryDto.getName()).isEmpty()) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }
        return CategoryMapper.categoryToDto(
                categoryRepository.saveAndFlush(CategoryMapper.requestToCategory(newCategoryDto))
        );
    }

    @Transactional
    public void deleteCategory(Long catId) {
        categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория с id=" + catId + " не найдена")
        );
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConditionsNotMetException("Удаление категории невозможно, так как она используется в событиях");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена")
        );
        List<Category> categories = categoryRepository.findByNameIgnoreCase(newCategoryDto.getName());
        if (!categories.isEmpty() && !categories.get(0).getId().equals(id)) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }

        category.setName(newCategoryDto.getName());
        return CategoryMapper.categoryToDto(categoryRepository.saveAndFlush(category));
    }
}