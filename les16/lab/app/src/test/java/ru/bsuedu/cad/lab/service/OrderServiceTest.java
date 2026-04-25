package ru.bsuedu.cad.lab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.bsuedu.cad.lab.entity.*;
import ru.bsuedu.cad.lab.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("Алексей Иванов");
        customer.setEmail("alex@example.com");
        customer.setAddress("Москва, ул. Ленина, д. 10");

        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Корма");

        product1 = new Product();
        product1.setProductId(1L);
        product1.setName("Сухой корм для собак");
        product1.setPrice(new BigDecimal("1500.00"));
        product1.setCategory(category);

        product2 = new Product();
        product2.setProductId(2L);
        product2.setName("Игрушка для кошек");
        product2.setPrice(new BigDecimal("300.00"));
        product2.setCategory(category);
    }

    // ==================== createOrder ====================

    @Test
    @DisplayName("createOrder: успешное создание заказа с одним товаром")
    void createOrder_singleProduct_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setOrderId(1L);
            return o;
        });
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(1L, List.of(1L), List.of(2));

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getCustomer().getName()).isEqualTo("Алексей Иванов");
        assertThat(result.getStatus()).isEqualTo("NEW");
        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(result.getShippingAddress()).isEqualTo("Москва, ул. Ленина, д. 10");

        verify(customerRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderDetailRepository).save(any(OrderDetail.class));
    }

    @Test
    @DisplayName("createOrder: успешное создание заказа с несколькими товарами")
    void createOrder_multipleProducts_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setOrderId(1L);
            return o;
        });
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(1L, List.of(1L, 2L), List.of(1, 3));

        // 1500*1 + 300*3 = 2400
        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("2400.00"));

        verify(orderDetailRepository, times(2)).save(any(OrderDetail.class));
    }

    @Test
    @DisplayName("createOrder: ошибка — клиент не найден")
    void createOrder_customerNotFound_throwsException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(999L, List.of(1L), List.of(1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Клиент не найден");

        verify(customerRepository).findById(999L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder: ошибка — товар не найден")
    void createOrder_productNotFound_throwsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setOrderId(1L);
            return o;
        });
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(1L, List.of(999L), List.of(1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Товар не найден");
    }

    // ==================== getOrderById ====================

    @Test
    @DisplayName("getOrderById: успешное получение заказа")
    void getOrderById_found_success() {
        Order order = new Order();
        order.setOrderId(1L);
        order.setCustomer(customer);
        order.setStatus("NEW");

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        verify(orderRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("getOrderById: ошибка — заказ не найден")
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Заказ не найден");
    }

    // ==================== getAllOrders ====================

    @Test
    @DisplayName("getAllOrders: возвращает список заказов")
    void getAllOrders_returnsList() {
        Order order1 = new Order();
        order1.setOrderId(1L);
        Order order2 = new Order();
        order2.setOrderId(2L);

        when(orderRepository.findAllWithDetails()).thenReturn(List.of(order1, order2));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        verify(orderRepository).findAllWithDetails();
    }

    @Test
    @DisplayName("getAllOrders: пустой список если заказов нет")
    void getAllOrders_emptyList() {
        when(orderRepository.findAllWithDetails()).thenReturn(List.of());

        List<Order> result = orderService.getAllOrders();

        assertThat(result).isEmpty();
    }

    // ==================== deleteOrder ====================

    @Test
    @DisplayName("deleteOrder: успешное удаление заказа")
    void deleteOrder_exists_success() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        orderService.deleteOrder(1L);

        verify(orderRepository).existsById(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteOrder: ошибка — заказ не найден")
    void deleteOrder_notFound_throwsException() {
        when(orderRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Заказ не найден");

        verify(orderRepository, never()).deleteById(any());
    }
}
