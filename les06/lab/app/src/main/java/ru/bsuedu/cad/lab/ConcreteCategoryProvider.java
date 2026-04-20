package ru.bsuedu.cad.lab;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConcreteCategoryProvider implements CategoryProvider {

    private final CategoryFileReader reader;
    private final CategoryParser parser;

    public ConcreteCategoryProvider(CategoryFileReader reader, CategoryParser parser) {
        this.reader = reader;
        this.parser = parser;
    }

    @Override
    public List<Category> getCategories() {
        String data = reader.read();
        return parser.parse(data);
    }
}
