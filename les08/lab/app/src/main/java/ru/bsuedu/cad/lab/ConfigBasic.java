package ru.bsuedu.cad.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "ru.bsuedu.cad.lab")
@PropertySource("classpath:db/jdbc.properties")
public class ConfigBasic {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBasic.class);

    @Value("${jdbc.driverClassName}")
    private String driverClassName;

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        try {
            var hc = new HikariConfig();
            hc.setJdbcUrl(url);
            hc.setDriverClassName(driverClassName);
            hc.setUsername(username);
            hc.setPassword(password);
            var dataSource = new HikariDataSource(hc);
            dataSource.setMaximumPoolSize(25);
            return dataSource;
        } catch (Exception e) {
            LOGGER.error("Hikari DataSource bean cannot be created!", e);
            return null;
        }
    }
}
