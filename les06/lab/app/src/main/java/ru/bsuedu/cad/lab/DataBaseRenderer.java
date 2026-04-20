package ru.bsuedu.cad.lab;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
@Primary
public class DataBaseRenderer implements Renderer {

    private final ProductProvider productProvider;
    private final CategoryProvider categoryProvider;
    private final JdbcTemplate jdbcTemplate;
    private final CategoryRequest categoryRequest;

    public DataBaseRenderer(ProductProvider productProvider, CategoryProvider categoryProvider,
                            JdbcTemplate jdbcTemplate, CategoryRequest categoryRequest) {
        this.productProvider = productProvider;
        this.categoryProvider = categoryProvider;
        this.jdbcTemplate = jdbcTemplate;
        this.categoryRequest = categoryRequest;
    }

    @Override
    public void render() {
        List<Category> categories = categoryProvider.getCategories();
        for (Category c : categories) {
            jdbcTemplate.update(
                "INSERT INTO CATEGORIES (category_id, name, description) VALUES (?, ?, ?)",
                c.getCategoryId(), c.getName(), c.getDescription()
            );
        }

        List<Product> products = productProvider.getProducts();
        for (Product p : products) {
            jdbcTemplate.update(
                "INSERT INTO PRODUCTS (product_id, name, description, category_id, price, stock_quantity, image_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getProductId(), p.getName(), p.getDescription(), p.getCategoryId(),
                p.getPrice(), p.getStockQuantity(), p.getImageUrl(),
                p.getCreatedAt() != null ? new Timestamp(p.getCreatedAt().getTime()) : null,
                p.getUpdatedAt() != null ? new Timestamp(p.getUpdatedAt().getTime()) : null
            );
        }

        System.out.println("Данные успешно сохранены в базу данных.");
        System.out.println("Категорий: " + categories.size() + ", Товаров: " + products.size());

        categoryRequest.execute();
    }
}
