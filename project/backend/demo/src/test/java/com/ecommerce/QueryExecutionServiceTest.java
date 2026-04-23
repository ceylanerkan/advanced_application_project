package com.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ecommerce.service.QueryExecutionService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryExecutionServiceTest {

    private QueryExecutionService executionService;
    private JdbcTemplate readOnlyJdbcTemplate;

    @BeforeEach
    void setUp() {
        // Using standard Mockito.mock() to avoid requiring a real DB context
        readOnlyJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        executionService = new QueryExecutionService(readOnlyJdbcTemplate);
    }

    @Test
    void validateAndExecute_ShouldAllowSafeSelectQueries() {
        String safeSql = "SELECT order_id, status, grand_total FROM orders WHERE user_id = 42";
        
        assertDoesNotThrow(() -> executionService.executeSafeQuery(safeSql),
            "Service should allow explicitly named SELECT statements.");
    }

    @Test
    void validateAndExecute_ShouldBlockSqlInjectionAndMultiStatements_AV03() {
        String injectedSql = "SELECT order_id FROM orders WHERE user_id = 42; DROP TABLE users;";
        
        SecurityException exception = assertThrows(SecurityException.class, 
            () -> executionService.executeSafeQuery(injectedSql));
        
        // Replaced bare 'assert' with JUnit 5 'assertTrue'
        assertTrue(exception.getMessage().contains("Multiple statements detected"));
    }

    @Test
    void validateAndExecute_ShouldBlockMassAssignmentWrites_AV11() {
        String updateSql = "UPDATE products SET unit_price = 0 WHERE category_id = 1";
        
        SecurityException exception = assertThrows(SecurityException.class, 
            () -> executionService.executeSafeQuery(updateSql));
            
        assertTrue(exception.getMessage().contains("Only SELECT operations are permitted"));
    }

    @Test
    void validateAndExecute_ShouldBlockSelectStarExfiltration_AV12() {
        String wildcardSql = "SELECT * FROM users WHERE id = 1";
        
        SecurityException exception = assertThrows(SecurityException.class, 
            () -> executionService.executeSafeQuery(wildcardSql));
            
        assertTrue(exception.getMessage().contains("Wildcard SELECT * is strictly forbidden"));
    }
    
    @Test
    void validateAndExecute_ShouldBlockSensitiveColumnAccess_AV12() {
        String sensitiveSql = "SELECT email, password_hash FROM users WHERE id = 1";
        
        SecurityException exception = assertThrows(SecurityException.class, 
            () -> executionService.executeSafeQuery(sensitiveSql));
            
        assertTrue(exception.getMessage().contains("Access to restricted column blocked"));
    }
}