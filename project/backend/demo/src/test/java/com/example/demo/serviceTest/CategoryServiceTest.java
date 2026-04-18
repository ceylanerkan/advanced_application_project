package com.example.demo.serviceTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory));
        List<Category> categories = categoryService.getAllCategories();
        assertEquals(1, categories.size());
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        Category found = categoryService.getCategoryById(1L);
        assertEquals("Electronics", found.getName());
    }

    @Test
    void createCategory_ShouldSaveAndReturn() {
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        Category saved = categoryService.createCategory(testCategory);
        assertNotNull(saved);
    }

    @Test
    void updateCategory_ShouldUpdateAndReturn() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        Category updated = categoryService.updateCategory(1L, testCategory);
        assertNotNull(updated);
    }

    @Test
    void deleteCategory_ShouldCallRepository() {
        doNothing().when(categoryRepository).deleteById(1L);
        categoryService.deleteCategory(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }
}