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

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: " + id));
    }

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
        }

        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        LOGGER.info("Заказ #{} успешно создан. Итого: {} руб.", order.getOrderId(), totalPrice);
        return order;
    }

    @Transactional
    public Order updateOrder(Long orderId, Long customerId, String status, String shippingAddress,
                             List<Long> productIds, List<Integer> quantities) {
        LOGGER.info("Обновление заказа #{}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: " + orderId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Клиент не найден: " + customerId));

        order.setCustomer(customer);
        order.setStatus(status);
        order.setShippingAddress(shippingAddress);

        // Удаляем старые позиции
        order.getOrderDetails().clear();
        orderRepository.flush();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (int i = 0; i < productIds.size(); i++) {
            Product product = productRepository.findById(productIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));
            int qty = quantities.get(i);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(qty);
            detail.setPrice(product.getPrice());
            order.getOrderDetails().add(detail);

            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        LOGGER.info("Заказ #{} обновлён. Итого: {} руб.", order.getOrderId(), totalPrice);
        return order;
    }

    @Transactional
    public void deleteOrder(Long id) {
        LOGGER.info("Удаление заказа #{}", id);
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Заказ не найден: " + id);
        }
        orderRepository.deleteById(id);
        LOGGER.info("Заказ #{} удалён", id);
    }
}
