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

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductId("B001");
        testProduct.setName("Test Product");
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));
        List<Product> products = productService.getAllProducts();
        assertEquals(1, products.size());
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        Product foundProduct = productService.getProductById(1L);
        assertNotNull(foundProduct);
        assertEquals("Test Product", foundProduct.getName());
    }

    @Test
    void createProduct_ShouldSaveAndReturn() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        Product savedProduct = productService.createProduct(testProduct);
        assertNotNull(savedProduct);
    }

    @Test
    void deleteProduct_ShouldCallRepositoryDelete() {
        doNothing().when(productRepository).deleteById(1L);
        productService.deleteProduct(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }
}