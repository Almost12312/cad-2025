package ru.bsuedu.cad.lab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

@Component
@Primary
public class HTMLTableRenderer implements Renderer {

    private final ProductProvider provider;

    @Value("${html.output.path}")
    private String outputPath;

    public HTMLTableRenderer(ProductProvider provider) {
        this.provider = provider;
    }

    @Override
    public void render() {
        List<Product> products = provider.getProducts();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (PrintWriter pw = new PrintWriter(new FileWriter(outputPath))) {
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"ru\">");
            pw.println("<head>");
            pw.println("    <meta charset=\"UTF-8\">");
            pw.println("    <title>Товары зоомагазина</title>");
            pw.println("    <style>");
            pw.println("        body { font-family: Arial, sans-serif; margin: 20px; }");
            pw.println("        table { border-collapse: collapse; width: 100%; }");
            pw.println("        th, td { border: 1px solid #333; padding: 8px; text-align: left; }");
            pw.println("        th { background-color: #4CAF50; color: white; }");
            pw.println("        tr:nth-child(even) { background-color: #f2f2f2; }");
            pw.println("    </style>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<h1>Товары зоомагазина</h1>");
            pw.println("<table>");
            pw.println("    <tr>");
            pw.println("        <th>ID</th><th>Название</th><th>Описание</th><th>Категория</th>");
            pw.println("        <th>Цена</th><th>Кол-во</th><th>URL изображения</th><th>Создан</th><th>Обновлён</th>");
            pw.println("    </tr>");

            for (Product p : products) {
                pw.println("    <tr>");
                pw.printf("        <td>%d</td><td>%s</td><td>%s</td><td>%d</td>%n",
                        p.getProductId(), escape(p.getName()), escape(p.getDescription()), p.getCategoryId());
                pw.printf("        <td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td>%n",
                        p.getPrice(), p.getStockQuantity(), escape(p.getImageUrl()),
                        p.getCreatedAt() != null ? sdf.format(p.getCreatedAt()) : "",
                        p.getUpdatedAt() != null ? sdf.format(p.getUpdatedAt()) : "");
                pw.println("    </tr>");
            }

            pw.println("</table>");
            pw.println("</body>");
            pw.println("</html>");

            System.out.println("HTML-таблица сохранена в файл: " + outputPath);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи HTML-файла: " + e.getMessage(), e);
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
