package ru.bsuedu.cad.lab.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.bsuedu.cad.lab.entity.Order;
import ru.bsuedu.cad.lab.entity.OrderDetail;
import ru.bsuedu.cad.lab.service.DataLoaderService;
import ru.bsuedu.cad.lab.service.OrderService;

import java.util.List;

@Component
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final DataLoaderService dataLoaderService;
    private final OrderService orderService;

    public Client(DataLoaderService dataLoaderService, OrderService orderService) {
        this.dataLoaderService = dataLoaderService;
        this.orderService = orderService;
    }

    public void run() {
        LOGGER.info("=== Начало работы клиента ===");

        // 1. Загрузка данных из CSV
        LOGGER.info("--- Загрузка начальных данных из CSV ---");
        dataLoaderService.loadAll();

        // 2. Создание заказа
        LOGGER.info("--- Создание нового заказа ---");
        Order order = orderService.createOrder(
                1L, // Алексей Иванов
                List.of(1L, 2L, 10L), // Сухой корм, Игрушка "Мышка", Поводок
                List.of(2, 3, 1) // количества
        );

        // 3. Доказательство сохранения — получаем все заказы из БД
        LOGGER.info("--- Проверка сохранения заказа в БД ---");
        List<Order> allOrders = orderService.getAllOrders();
        LOGGER.info("Количество заказов в БД: {}", allOrders.size());

        for (Order o : allOrders) {
            LOGGER.info("Заказ #{}: клиент={}, дата={}, статус={}, итого={} руб., адрес={}",
                    o.getOrderId(),
                    o.getCustomer().getName(),
                    o.getOrderDate(),
                    o.getStatus(),
                    o.getTotalPrice(),
                    o.getShippingAddress());

            for (OrderDetail detail : o.getOrderDetails()) {
                LOGGER.info("  Позиция: {} x {} шт. по {} руб.",
                        detail.getProduct().getName(),
                        detail.getQuantity(),
                        detail.getPrice());
            }
        }

        LOGGER.info("=== Работа клиента завершена ===");
    }
}
