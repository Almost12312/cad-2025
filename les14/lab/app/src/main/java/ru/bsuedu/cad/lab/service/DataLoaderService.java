package ru.bsuedu.cad.lab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.bsuedu.cad.lab.entity.Category;
import ru.bsuedu.cad.lab.entity.Customer;
import ru.bsuedu.cad.lab.entity.Product;
import ru.bsuedu.cad.lab.repository.CategoryRepository;
import ru.bsuedu.cad.lab.repository.CustomerRepository;
import ru.bsuedu.cad.lab.repository.ProductRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataLoaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoaderService.class);

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public DataLoaderService(CategoryRepository categoryRepository,
                             CustomerRepository customerRepository,
                             ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void loadAll() {
        loadCategories();
        loadCustomers();
        loadProducts();
    }

    private void loadCategories() {
        LOGGER.info("Загрузка категорий из CSV...");
        try (var reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/csv/category.csv"), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 3);
                Category category = new Category();
                category.setName(parts[1].trim());
                category.setDescription(parts[2].trim());
                categoryRepository.save(category);
            }
            LOGGER.info("Категории загружены. Всего: {}", categoryRepository.count());
        } catch (Exception e) {
            LOGGER.error("Ошибка загрузки категорий", e);
        }
    }

    private void loadCustomers() {
        LOGGER.info("Загрузка клиентов из CSV...");
        try (var reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/csv/customer.csv"), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = parseCsvLine(line);
                Customer customer = new Customer();
                customer.setName(parts[1].trim());
                customer.setEmail(parts[2].trim());
                customer.setPhone(parts[3].trim());
                customer.setAddress(parts[4].trim());
                customerRepository.save(customer);
            }
            LOGGER.info("Клиенты загружены. Всего: {}", customerRepository.count());
        } catch (Exception e) {
            LOGGER.error("Ошибка загрузки клиентов", e);
        }
    }

    private void loadProducts() {
        LOGGER.info("Загрузка товаров из CSV...");
        Map<Long, Category> categoryMap = new HashMap<>();
        for (Category c : categoryRepository.findAll()) {
            categoryMap.put(c.getCategoryId(), c);
        }
        try (var reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/csv/product.csv"), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = parseCsvLine(line);
                Product product = new Product();
                product.setName(parts[1].trim());
                product.setDescription(parts[2].trim());
                long catId = Long.parseLong(parts[3].trim());
                product.setCategory(categoryMap.get(catId));
                product.setPrice(new BigDecimal(parts[4].trim()));
                product.setStockQuantity(Integer.parseInt(parts[5].trim()));
                product.setImageUrl(parts[6].trim());
                product.setCreatedAt(LocalDate.parse(parts[7].trim()));
                product.setUpdatedAt(LocalDate.parse(parts[8].trim()));
                productRepository.save(product);
            }
            LOGGER.info("Товары загружены. Всего: {}", productRepository.count());
        } catch (Exception e) {
            LOGGER.error("Ошибка загрузки товаров", e);
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
