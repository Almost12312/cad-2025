package ru.bsuedu.cad.lab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.bsuedu.cad.lab.entity.*;
import ru.bsuedu.cad.lab.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    /**
     * Создание заказа в рамках транзакции.
     * @param customerId ID клиента
     * @param productIds список ID товаров
     * @param quantities список количеств для каждого товара
     * @return созданный заказ
     */
    @Transactional
    public Order createOrder(Long customerId, List<Long> productIds, List<Integer> quantities) {
        LOGGER.info("Создание заказа для клиента с ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Клиент не найден: " + customerId));

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("NEW");
        order.setShippingAddress(customer.getAddress());

        BigDecimal totalPrice = BigDecimal.ZERO;

        order = orderRepository.save(order);

        for (int i = 0; i < productIds.size(); i++) {
            Product product = productRepository.findById(productIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            int qty = quantities.get(i);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(qty);
            detail.setPrice(product.getPrice());
            orderDetailRepository.save(detail);

            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));

            LOGGER.info("  Добавлен товар: {} x {} = {}",
                    product.getName(), qty, product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        LOGGER.info("Заказ #{} успешно создан. Итого: {} руб.", order.getOrderId(), totalPrice);
        return order;
    }

    /**
     * Получение списка всех заказов с деталями.
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithDetails();
    }
}
