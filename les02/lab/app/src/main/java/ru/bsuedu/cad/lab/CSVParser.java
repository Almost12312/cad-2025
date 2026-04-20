package ru.bsuedu.cad.lab;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSVParser implements Parser {

    @Override
    public List<Product> parse(String data) {
        List<Product> products = new ArrayList<>();
        String[] lines = data.split("\\r?\\n");

        // Пропускаем заголовок
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] fields = parseLine(line);
            if (fields.length < 9) continue;

            try {
                Product p = new Product();
                p.setProductId(Long.parseLong(fields[0].trim()));
                p.setName(fields[1].trim());
                p.setDescription(fields[2].trim());
                p.setCategoryId(Integer.parseInt(fields[3].trim()));
                p.setPrice(new BigDecimal(fields[4].trim()));
                p.setStockQuantity(Integer.parseInt(fields[5].trim()));
                p.setImageUrl(fields[6].trim());
                p.setCreatedAt(parseDate(fields[7].trim()));
                p.setUpdatedAt(parseDate(fields[8].trim()));
                products.add(p);
            } catch (Exception e) {
                System.err.println("Ошибка разбора строки: " + line + " — " + e.getMessage());
            }
        }
        return products;
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Простой парсер CSV-строки с учётом кавычек.
     */
    private String[] parseLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    result.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
