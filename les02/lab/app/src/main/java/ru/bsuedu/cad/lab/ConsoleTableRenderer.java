package ru.bsuedu.cad.lab;

import java.text.SimpleDateFormat;
import java.util.List;

public class ConsoleTableRenderer implements Renderer {

    private final ProductProvider provider;

    public ConsoleTableRenderer(ProductProvider provider) {
        this.provider = provider;
    }

    @Override
    public void render() {
        List<Product> products = provider.getProducts();

        String format = "| %-3s | %-35s | %-40s | %-11s | %-8s | %-5s | %-40s | %-12s | %-12s |%n";
        String separator = "+" + "-".repeat(5)
                + "+" + "-".repeat(37)
                + "+" + "-".repeat(42)
                + "+" + "-".repeat(13)
                + "+" + "-".repeat(10)
                + "+" + "-".repeat(7)
                + "+" + "-".repeat(42)
                + "+" + "-".repeat(14)
                + "+" + "-".repeat(14) + "+";

        System.out.println(separator);
        System.out.printf(format,
                "ID", "Название", "Описание", "Категория", "Цена", "Кол.", "URL изображения", "Создан", "Обновлён");
        System.out.println(separator);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Product p : products) {
            System.out.printf(format,
                    p.getProductId(),
                    truncate(p.getName(), 35),
                    truncate(p.getDescription(), 40),
                    p.getCategoryId(),
                    p.getPrice(),
                    p.getStockQuantity(),
                    truncate(p.getImageUrl(), 40),
                    p.getCreatedAt() != null ? sdf.format(p.getCreatedAt()) : "",
                    p.getUpdatedAt() != null ? sdf.format(p.getUpdatedAt()) : "");
        }
        System.out.println(separator);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 2) + ".." : s;
    }
}
