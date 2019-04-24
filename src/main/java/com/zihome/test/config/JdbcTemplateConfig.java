package com.zihome.test.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 描述:
 *  jdbc配置
 *
 * @author Yangjy
 * @date 2018-03-26
 */
@Configuration
public class JdbcTemplateConfig {

    @Bean("masterJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Autowired DataSource datasource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        return jdbcTemplate;
    }

}
