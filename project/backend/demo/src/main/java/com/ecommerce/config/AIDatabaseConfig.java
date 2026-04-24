package com.ecommerce.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class AIDatabaseConfig {

    /**
     * By specifying .type(HikariDataSource.class), we allow Spring to bind 
     * properties directly to the Hikari connection pool. 
     * This avoids using any 'autoconfigure.jdbc' helper classes.
     */
    @Bean(name = "aiDataSource")
    @ConfigurationProperties(prefix = "ai.datasource")
    public DataSource aiDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "aiJdbcTemplate")
    public JdbcTemplate aiJdbcTemplate(@Qualifier("aiDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}