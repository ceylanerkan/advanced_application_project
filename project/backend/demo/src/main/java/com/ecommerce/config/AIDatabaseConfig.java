package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

@Configuration
public class AIDatabaseConfig {

    // Step 1: Establish a "Least Privilege" Database Connection
    // This builds a secondary DataSource explicitly for the AI's dynamic execution
    @Bean(name = "aiDataSource")
    @ConfigurationProperties(prefix = "ai.datasource")
    public DataSource aiDataSource() {
        return DataSourceBuilder.create().build();
    }

    // This JdbcTemplate will exclusively use the read-only DataSource
    @Bean(name = "aiJdbcTemplate")
    public JdbcTemplate aiJdbcTemplate(@Qualifier("aiDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
