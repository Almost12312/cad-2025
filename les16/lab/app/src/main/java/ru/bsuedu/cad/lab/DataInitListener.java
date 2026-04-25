package ru.bsuedu.cad.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ru.bsuedu.cad.lab.service.DataLoaderService;

@Component
public class DataInitListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitListener.class);

    private final DataLoaderService dataLoaderService;
    private boolean initialized = false;

    public DataInitListener(DataLoaderService dataLoaderService) {
        this.dataLoaderService = dataLoaderService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!initialized) {
            LOGGER.info("Инициализация данных из CSV...");
            dataLoaderService.loadAll();
            LOGGER.info("Данные из CSV успешно загружены.");
            initialized = true;
        }
    }
}
