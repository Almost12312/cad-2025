package ru.bsuedu.cad.lab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ru.bsuedu.cad.lab.entity.Order;
import ru.bsuedu.cad.lab.repository.CustomerRepository;
import ru.bsuedu.cad.lab.repository.ProductRepository;
import ru.bsuedu.cad.lab.service.OrderService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderService orderService, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listOrders(Model model, Authentication authentication) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isManager", authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
        return "order-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        return "order-create";
    }

    @PostMapping
    public String createOrder(@RequestParam Long customerId,
                              @RequestParam(name = "productId") List<Long> productIds,
                              @RequestParam(name = "quantity") List<Integer> quantities) {
        List<Long> fIds = new ArrayList<>(); List<Integer> fQty = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            if (productIds.get(i) != null && productIds.get(i) > 0) { fIds.add(productIds.get(i)); fQty.add(quantities.get(i)); }
        }
        try { orderService.createOrder(customerId, fIds, fQty); } catch (Exception e) { LOGGER.error("Ошибка создания заказа", e); }
        return "redirect:/orders";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        return "order-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateOrder(@PathVariable Long id, @RequestParam Long customerId, @RequestParam String status,
                              @RequestParam String shippingAddress, @RequestParam(name = "productId") List<Long> productIds,
                              @RequestParam(name = "quantity") List<Integer> quantities) {
        List<Long> fIds = new ArrayList<>(); List<Integer> fQty = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            if (productIds.get(i) != null && productIds.get(i) > 0) { fIds.add(productIds.get(i)); fQty.add(quantities.get(i)); }
        }
        try { orderService.updateOrder(id, customerId, status, shippingAddress, fIds, fQty); } catch (Exception e) { LOGGER.error("Ошибка обновления заказа", e); }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable Long id) {
        try { orderService.deleteOrder(id); } catch (Exception e) { LOGGER.error("Ошибка удаления заказа", e); }
        return "redirect:/orders";
    }
}
