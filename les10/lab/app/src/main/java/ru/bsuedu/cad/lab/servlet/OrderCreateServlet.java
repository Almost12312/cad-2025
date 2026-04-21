package ru.bsuedu.cad.lab.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.bsuedu.cad.lab.entity.Customer;
import ru.bsuedu.cad.lab.entity.Product;
import ru.bsuedu.cad.lab.repository.CustomerRepository;
import ru.bsuedu.cad.lab.repository.ProductRepository;
import ru.bsuedu.cad.lab.service.OrderService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/orders/create")
public class OrderCreateServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCreateServlet.class);

    private OrderService orderService;
    private CustomerRepository customerRepository;
    private ProductRepository productRepository;

    @Override
    public void init() throws ServletException {
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        orderService = ctx.getBean(OrderService.class);
        customerRepository = ctx.getBean(CustomerRepository.class);
        productRepository = ctx.getBean(ProductRepository.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        List<Customer> customers = customerRepository.findAll();
        List<Product> products = productRepository.findAll();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Создание заказа — Магазин зоотоваров</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        out.println("h1 { color: #333; }");
        out.println("form { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.2); max-width: 600px; }");
        out.println("label { display: block; margin: 10px 0 5px; font-weight: bold; }");
        out.println("select, input { width: 100%; padding: 8px; margin-bottom: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }");
        out.println(".product-row { display: flex; gap: 10px; align-items: center; margin-bottom: 5px; }");
        out.println(".product-row select { flex: 3; }");
        out.println(".product-row input { flex: 1; width: auto; }");
        out.println(".btn { padding: 10px 20px; background: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; font-size: 16px; }");
        out.println(".btn:hover { background: #45a049; }");
        out.println(".btn-secondary { background: #666; margin-left: 10px; text-decoration: none; color: white; display: inline-block; padding: 10px 20px; border-radius: 5px; }");
        out.println(".btn-add { background: #2196F3; padding: 5px 15px; font-size: 14px; margin-bottom: 10px; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<h1>🐾 Создание нового заказа</h1>");

        out.println("<form method='POST' action='" + req.getContextPath() + "/orders/create'>");

        // Выбор клиента
        out.println("<label>Клиент:</label>");
        out.println("<select name='customerId' required>");
        out.println("<option value=''>-- Выберите клиента --</option>");
        for (Customer c : customers) {
            out.println("<option value='" + c.getCustomerId() + "'>" + c.getName() + " (" + c.getEmail() + ")</option>");
        }
        out.println("</select>");

        // Выбор товаров
        out.println("<label>Товары:</label>");
        out.println("<div id='products'>");
        out.println("<div class='product-row'>");
        out.println("<select name='productId' required>");
        out.println("<option value=''>-- Товар --</option>");
        for (Product p : products) {
            out.println("<option value='" + p.getProductId() + "'>" + p.getName() + " — " + p.getPrice() + " руб. (остаток: " + p.getStockQuantity() + ")</option>");
        }
        out.println("</select>");
        out.println("<input type='number' name='quantity' min='1' value='1' required placeholder='Кол-во'>");
        out.println("</div>");
        out.println("</div>");

        out.println("<br>");
        out.println("<button type='submit' class='btn'>Создать заказ</button>");
        out.println("<a class='btn-secondary' href='" + req.getContextPath() + "/orders'>Назад к списку</a>");
        out.println("</form>");

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String customerIdStr = req.getParameter("customerId");
        String[] productIdStrs = req.getParameterValues("productId");
        String[] quantityStrs = req.getParameterValues("quantity");

        if (customerIdStr == null || productIdStrs == null || quantityStrs == null) {
            resp.sendRedirect(req.getContextPath() + "/orders/create");
            return;
        }

        try {
            Long customerId = Long.parseLong(customerIdStr);
            List<Long> productIds = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();

            for (int i = 0; i < productIdStrs.length; i++) {
                if (!productIdStrs[i].isEmpty()) {
                    productIds.add(Long.parseLong(productIdStrs[i]));
                    quantities.add(Integer.parseInt(quantityStrs[i]));
                }
            }

            orderService.createOrder(customerId, productIds, quantities);
            LOGGER.info("Заказ успешно создан через веб-форму");

        } catch (Exception e) {
            LOGGER.error("Ошибка создания заказа", e);
        }

        resp.sendRedirect(req.getContextPath() + "/orders");
    }
}
