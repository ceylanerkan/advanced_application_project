package com.ecommerce.controller;

import com.ecommerce.security.service.JwtService;
import com.ecommerce.model.Category;
import com.ecommerce.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @MockitoBean
    private UserDetailsService userDetailsService;
    
    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        // Assuming your Category model has a description or other fields, 
        // you can set them here if needed.
    }

    @Test
    void getAllCategories_ShouldReturn200() throws Exception {
        Mockito.when(categoryService.getAllCategories()).thenReturn(Arrays.asList(testCategory));
        
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    void getCategoryById_ShouldReturn200() throws Exception {
        Mockito.when(categoryService.getCategoryById(1L)).thenReturn(testCategory);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_ShouldReturn201() throws Exception {
        Mockito.when(categoryService.createCategory(any(Category.class), Mockito.anyString())).thenReturn(testCategory);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void updateCategory_ShouldReturn200() throws Exception {
        Mockito.when(categoryService.updateCategory(eq(1L), any(Category.class), Mockito.anyString())).thenReturn(testCategory);

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void deleteCategory_ShouldReturn204() throws Exception {
        Mockito.doNothing().when(categoryService).deleteCategory(eq(1L), Mockito.anyString());

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }
}