package ru.bsuedu.cad.lab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.bsuedu.cad.lab.entity.Order;
import ru.bsuedu.cad.lab.service.OrderService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;

    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<Map<String, Object>> result = orders.stream()
                .map(this::orderToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(orderToMap(order));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(
                    request.getCustomerId(),
                    request.getProductIds(),
                    request.getQuantities()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(orderToMap(order));
        } catch (RuntimeException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOrder(@PathVariable Long id,
                                                           @RequestBody UpdateOrderRequest request) {
        try {
            Order order = orderService.updateOrder(
                    id,
                    request.getCustomerId(),
                    request.getStatus(),
                    request.getShippingAddress(),
                    request.getProductIds(),
                    request.getQuantities()
            );
            return ResponseEntity.ok(orderToMap(order));
        } catch (RuntimeException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("customerName", order.getCustomer() != null ? order.getCustomer().getName() : null);
        map.put("customerId", order.getCustomer() != null ? order.getCustomer().getCustomerId() : null);
        map.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        map.put("totalPrice", order.getTotalPrice());
        map.put("status", order.getStatus());
        map.put("shippingAddress", order.getShippingAddress());
        map.put("details", order.getOrderDetails().stream().map(d -> {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("orderDetailId", d.getOrderDetailId());
            detail.put("productId", d.getProduct() != null ? d.getProduct().getProductId() : null);
            detail.put("productName", d.getProduct() != null ? d.getProduct().getName() : null);
            detail.put("quantity", d.getQuantity());
            detail.put("price", d.getPrice());
            return detail;
        }).collect(Collectors.toList()));
        return map;
    }

    public static class CreateOrderRequest {
        private Long customerId;
        private List<Long> productIds;
        private List<Integer> quantities;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public List<Long> getProductIds() { return productIds; }
        public void setProductIds(List<Long> productIds) { this.productIds = productIds; }
        public List<Integer> getQuantities() { return quantities; }
        public void setQuantities(List<Integer> quantities) { this.quantities = quantities; }
    }

    public static class UpdateOrderRequest {
        private Long customerId;
        private String status;
        private String shippingAddress;
        private List<Long> productIds;
        private List<Integer> quantities;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public List<Long> getProductIds() { return productIds; }
        public void setProductIds(List<Long> productIds) { this.productIds = productIds; }
        public List<Integer> getQuantities() { return quantities; }
        public void setQuantities(List<Integer> quantities) { this.quantities = quantities; }
    }
}
