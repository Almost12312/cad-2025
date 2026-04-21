package ru.bsuedu.cad.lab.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.bsuedu.cad.lab.entity.Order;
import ru.bsuedu.cad.lab.entity.OrderDetail;
import ru.bsuedu.cad.lab.service.OrderService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/orders")
public class OrderListServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        orderService = ctx.getBean(OrderService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        List<Order> orders = orderService.getAllOrders();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Заказы — Магазин зоотоваров</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        out.println("h1 { color: #333; }");
        out.println("table { border-collapse: collapse; width: 100%; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.2); }");
        out.println("th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }");
        out.println("th { background-color: #4CAF50; color: white; }");
        out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
        out.println(".btn { display: inline-block; padding: 10px 20px; margin: 15px 0; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }");
        out.println(".btn:hover { background: #45a049; }");
        out.println(".details { font-size: 0.9em; color: #666; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<h1>🐾 Магазин зоотоваров — Заказы</h1>");
        out.println("<a class='btn' href='" + req.getContextPath() + "/orders/create'>➕ Создать новый заказ</a>");

        if (orders.isEmpty()) {
            out.println("<p>Заказов пока нет.</p>");
        } else {
            out.println("<table>");
            out.println("<tr><th>№</th><th>Клиент</th><th>Дата</th><th>Статус</th><th>Итого (руб.)</th><th>Адрес доставки</th><th>Позиции</th></tr>");

            for (Order order : orders) {
                StringBuilder details = new StringBuilder();
                for (OrderDetail d : order.getOrderDetails()) {
                    details.append(d.getProduct().getName())
                            .append(" x ").append(d.getQuantity())
                            .append(" (").append(d.getPrice()).append(" руб.)<br>");
                }

                out.println("<tr>");
                out.println("<td>" + order.getOrderId() + "</td>");
                out.println("<td>" + order.getCustomer().getName() + "</td>");
                out.println("<td>" + order.getOrderDate() + "</td>");
                out.println("<td>" + order.getStatus() + "</td>");
                out.println("<td>" + order.getTotalPrice() + "</td>");
                out.println("<td>" + order.getShippingAddress() + "</td>");
                out.println("<td class='details'>" + details + "</td>");
                out.println("</tr>");
            }
            out.println("</table>");
        }

        out.println("</body></html>");
    }
}
