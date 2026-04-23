package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.model.User;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private void verifyAdminAccess(String customerId) {
        User currentUser = userRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only platform administrators can modify categories.");
        }
    }

    public Category createCategory(Category category, String customerId) {
        verifyAdminAccess(customerId);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails, String customerId) {
        verifyAdminAccess(customerId);
        Category category = getCategoryById(id);
        
        category.setName(categoryDetails.getName());
        
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id, String customerId) {
        verifyAdminAccess(customerId);
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}