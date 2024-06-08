package com.sap.region.manager.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>DataSourceConfig class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */

@Configuration
public class DataSourceConfig {

    // Fetching the DB ConfigurationProperties from application.yaml
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSourceProperties secondaryDataSourceProperties() {
        return new DataSourceProperties();
    }
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.batch")
    public DataSourceProperties batchDataSourceProperties() {
         return new DataSourceProperties();
    }

    @Bean
    @Primary
    protected DataSource dataSource(@Qualifier("batchDataSourceProperties") DataSourceProperties batchDataSourceProperties) {
        return batchDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    public Map<String, DataSource> dataSourceMap(@Qualifier("primaryDataSourceProperties") DataSourceProperties primaryDataSourceProperties, @Qualifier("secondaryDataSourceProperties") DataSourceProperties secondaryDataSourceProperties) {
        Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
        dataSourceMap.put("primary",
                primaryDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build());
        dataSourceMap.put("secondary",
                secondaryDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build());
        return dataSourceMap;
    }
    
    @Bean
    public  Map<String, JdbcTemplate> jdbcTemplateMap(@Qualifier("dataSourceMap") Map<String, DataSource> dataSourceMap) {
        Map<String, JdbcTemplate> jdbcTemplateMap = new ConcurrentHashMap<>();
        jdbcTemplateMap.put("primary", new JdbcTemplate(dataSourceMap.get("primary")));
        jdbcTemplateMap.put("secondary", new JdbcTemplate(dataSourceMap.get("secondary")));
        return jdbcTemplateMap;
    }

    @Bean
    public  JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
