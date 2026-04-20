package ru.bsuedu.cad.lab;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CSVCategoryParser implements CategoryParser {

    @Override
    public List<Category> parse(String data) {
        List<Category> categories = new ArrayList<>();
        String[] lines = data.split("\\r?\\n");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] fields = line.split(",", 3);
            if (fields.length < 3) continue;

            try {
                Category c = new Category();
                c.setCategoryId(Integer.parseInt(fields[0].trim()));
                c.setName(fields[1].trim());
                c.setDescription(fields[2].trim());
                categories.add(c);
            } catch (Exception e) {
                System.err.println("Ошибка разбора строки категории: " + line + " — " + e.getMessage());
            }
        }
        return categories;
    }
}
