package ru.bsuedu.cad.lab;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void testCSVParserReturnsProducts() {
        CSVParser parser = new CSVParser();
        String csv = "product_id,name,description,category_id,price,stock_quantity,image_url,created_at,updated_at\n"
                + "1,Корм,Описание,1,1500,50,https://example.com/img.jpg,2025-01-15,2025-02-01";
        var products = parser.parse(csv);
        assertEquals(1, products.size());
        assertEquals("Корм", products.get(0).getName());
    }

    @Test
    void testResourceFileReaderReadsFile() {
        ResourceFileReader reader = new ResourceFileReader();
        String content = reader.read();
        assertNotNull(content);
        assertFalse(content.isEmpty());
    }
}
