package ru.bsuedu.cad.lab;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void appHasMainClass() {
        assertDoesNotThrow(() -> {
            Class.forName("ru.bsuedu.cad.lab.App");
        });
    }
}
