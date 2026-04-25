package ru.bsuedu.cad.lab.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ru.bsuedu.cad.lab.config.TestConfigDB;
import ru.bsuedu.cad.lab.entity.*;
import ru.bsuedu.cad.lab.repository.*;
import ru.bsuedu.cad.lab.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfigDB.class)
@Transactional
@DisplayName("Интеграционные тесты OrderService + репозитории")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Customer savedCustomer;
    private Product savedProduct1;
    private Product savedProduct2;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Корма");
        category.setDescription("Тестовая категория");
        category = categoryRepository.save(category);

        savedCustomer = new Customer();
        savedCustomer.setName("Тестовый клиент");
        savedCustomer.setEmail("test@example.com");
        savedCustomer.setPhone("+79001234567");
        savedCustomer.setAddress("Тестовый адрес, д. 1");
        savedCustomer = customerRepository.save(savedCustomer);

        savedProduct1 = new Product();
        savedProduct1.setName("Тестовый корм");
        savedProduct1.setDescription("Описание");
        savedProduct1.setCategory(category);
        savedProduct1.setPrice(new BigDecimal("1500.00"));
        savedProduct1.setStockQuantity(50);
        savedProduct1.setImageUrl("https://example.com/test.jpg");
        savedProduct1.setCreatedAt(LocalDate.now());
        savedProduct1.setUpdatedAt(LocalDate.now());
        savedProduct1 = productRepository.save(savedProduct1);

        savedProduct2 = new Product();
        savedProduct2.setName("Тестовая игрушка");
        savedProduct2.setDescription("Описание");
        savedProduct2.setCategory(category);
        savedProduct2.setPrice(new BigDecimal("300.00"));
        savedProduct2.setStockQuantity(100);
        savedProduct2.setImageUrl("https://example.com/test2.jpg");
        savedProduct2.setCreatedAt(LocalDate.now());
        savedProduct2.setUpdatedAt(LocalDate.now());
        savedProduct2 = productRepository.save(savedProduct2);

        entityManager.flush();
        entityManager.clear();
    }

    // ==================== Успешные сценарии ====================

    @Test
    @DisplayName("Создание заказа сохраняется в БД с правильной суммой")
    void createOrder_persistsToDatabase() {
        Order order = orderService.createOrder(
                savedCustomer.getCustomerId(),
                List.of(savedProduct1.getProductId(), savedProduct2.getProductId()),
                List.of(2, 3)
        );

        assertThat(order.getOrderId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo("NEW");
        // 1500*2 + 300*3 = 3900
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("3900.00"));
        assertThat(order.getShippingAddress()).isEqualTo("Тестовый адрес, д. 1");

        // Сбрасываем кеш первого уровня, чтобы JPQL fetch пошёл в БД
        entityManager.flush();
        entityManager.clear();

        // Проверяем, что заказ реально в БД с деталями
        Order fromDb = orderService.getOrderById(order.getOrderId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getOrderDetails()).hasSize(2);
    }

    @Test
    @DisplayName("Создание заказа с одним товаром")
    void createOrder_singleProduct_persistsCorrectly() {
        Order order = orderService.createOrder(
                savedCustomer.getCustomerId(),
                List.of(savedProduct1.getProductId()),
                List.of(5)
        );

        assertThat(order.getOrderId()).isNotNull();
        // 1500 * 5 = 7500
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("7500.00"));
    }

    @Test
    @DisplayName("getAllOrders возвращает созданные заказы")
    void getAllOrders_afterCreate_returnsOrders() {
        orderService.createOrder(savedCustomer.getCustomerId(), List.of(savedProduct1.getProductId()), List.of(1));
        orderService.createOrder(savedCustomer.getCustomerId(), List.of(savedProduct2.getProductId()), List.of(2));

        entityManager.flush();
        entityManager.clear();

        List<Order> orders = orderService.getAllOrders();
        assertThat(orders).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("deleteOrder удаляет заказ из БД")
    void deleteOrder_removesFromDatabase() {
        Order order = orderService.createOrder(
                savedCustomer.getCustomerId(),
                List.of(savedProduct1.getProductId()),
                List.of(1)
        );
        Long orderId = order.getOrderId();

        entityManager.flush();
        entityManager.clear();

        orderService.deleteOrder(orderId);

        entityManager.flush();
        entityManager.clear();

        assertThat(orderRepository.findById(orderId)).isEmpty();
    }

    @Test
    @DisplayName("updateOrder обновляет статус и адрес в БД")
    void updateOrder_changesPersistedInDatabase() {
        Order order = orderService.createOrder(
                savedCustomer.getCustomerId(),
                List.of(savedProduct1.getProductId()),
                List.of(1)
        );

        entityManager.flush();
        entityManager.clear();

        Order updated = orderService.updateOrder(
                order.getOrderId(),
                savedCustomer.getCustomerId(),
                "SHIPPED",
                "Новый адрес, д. 99",
                List.of(savedProduct2.getProductId()),
                List.of(4)
        );

        assertThat(updated.getStatus()).isEqualTo("SHIPPED");
        assertThat(updated.getShippingAddress()).isEqualTo("Новый адрес, д. 99");
        // 300 * 4 = 1200
        assertThat(updated.getTotalPrice()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    // ==================== Негативные сценарии ====================

    @Test
    @DisplayName("createOrder: ошибка при несуществующем клиенте")
    void createOrder_invalidCustomer_throwsException() {
        assertThatThrownBy(() ->
                orderService.createOrder(99999L, List.of(savedProduct1.getProductId()), List.of(1))
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Клиент не найден");
    }

    @Test
    @DisplayName("createOrder: ошибка при несуществующем товаре")
    void createOrder_invalidProduct_throwsException() {
        assertThatThrownBy(() ->
                orderService.createOrder(savedCustomer.getCustomerId(), List.of(99999L), List.of(1))
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Товар не найден");
    }

    @Test
    @DisplayName("getOrderById: ошибка при несуществующем заказе")
    void getOrderById_notFound_throwsException() {
        assertThatThrownBy(() -> orderService.getOrderById(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Заказ не найден");
    }

    @Test
    @DisplayName("deleteOrder: ошибка при удалении несуществующего заказа")
    void deleteOrder_notFound_throwsException() {
        assertThatThrownBy(() -> orderService.deleteOrder(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Заказ не найден");
    }

    @Test
    @DisplayName("updateOrder: ошибка при обновлении несуществующего заказа")
    void updateOrder_notFound_throwsException() {
        assertThatThrownBy(() ->
                orderService.updateOrder(99999L, savedCustomer.getCustomerId(), "NEW", "addr",
                        List.of(savedProduct1.getProductId()), List.of(1))
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Заказ не найден");
    }
}
