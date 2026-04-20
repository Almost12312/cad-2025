package ru.bsuedu.cad.lab;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ResourceFileReader implements Reader {

    private static final String FILE_NAME = "product.csv";

    @Override
    public String read() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
        if (is == null) {
            throw new RuntimeException("Файл не найден: " + FILE_NAME);
        }
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
