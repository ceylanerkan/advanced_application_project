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

    private void verifyAdminAccess(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only platform administrators can modify categories.");
        }
    }

    public Category createCategory(Category category, String email) {
        verifyAdminAccess(email);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails, String email) {
        verifyAdminAccess(email);
        Category category = getCategoryById(id);
        
        category.setName(categoryDetails.getName());
        category.setParentCategory(categoryDetails.getParentCategory());
        
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id, String email) {
        verifyAdminAccess(email);
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}