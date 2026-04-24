package com.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest(properties = {
    // 1. ANA VERİTABANI (Primary DataSource)
    "spring.datasource.url=jdbc:mysql://localhost:3306/advanced_project",
    "spring.datasource.username=root",
    "spring.datasource.password=admin",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",

    // 2. AI VERİTABANI (Secondary DataSource) - ÇÖKMEYİ ENGELLEYEN KISIM BURASI
    "ai.datasource.url=jdbc:mysql://localhost:3306/advanced_project",
    "ai.datasource.username=root",
    "ai.datasource.password=admin",
    "ai.datasource.driver-class-name=com.mysql.cj.jdbc.Driver"
})
public class DatabaseCheckTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void checkDatabaseUrl() throws SQLException {
        // This will print the exact URL your app is currently using!
        String databaseUrl = dataSource.getConnection().getMetaData().getURL();
        System.out.println("=================================================");
        System.out.println("ACTIVE DATABASE URL: " + databaseUrl);
        System.out.println("=================================================");

    }
}