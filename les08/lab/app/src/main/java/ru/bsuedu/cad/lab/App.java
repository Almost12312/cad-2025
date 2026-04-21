package ru.bsuedu.cad.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ru.bsuedu.cad.lab.app.Client;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        LOGGER.info("Запуск приложения магазина зоотоваров...");
        var ctx = new AnnotationConfigApplicationContext(ConfigJpa.class);
        var client = ctx.getBean(Client.class);
        client.run();
        ctx.close();
    }
}
