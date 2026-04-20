package ru.bsuedu.cad.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CategoryRequest {

    private static final Logger logger = LoggerFactory.getLogger(CategoryRequest.class);

    private final JdbcTemplate jdbcTemplate;

    public CategoryRequest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void execute() {
        String sql = "SELECT c.category_id, c.name, c.description, COUNT(p.product_id) AS product_count " +
                     "FROM CATEGORIES c " +
                     "JOIN PRODUCTS p ON c.category_id = p.category_id " +
                     "GROUP BY c.category_id, c.name, c.description " +
                     "HAVING COUNT(p.product_id) > 1";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        logger.info("=== Категории с количеством товаров > 1 ===");
        for (Map<String, Object> row : rows) {
            logger.info("ID: {}, Название: {}, Описание: {}, Кол-во товаров: {}",
                    row.get("CATEGORY_ID"),
                    row.get("NAME"),
                    row.get("DESCRIPTION"),
                    row.get("PRODUCT_COUNT"));
        }

        if (rows.isEmpty()) {
            logger.info("Категорий с количеством товаров > 1 не найдено.");
        }
    }
}
