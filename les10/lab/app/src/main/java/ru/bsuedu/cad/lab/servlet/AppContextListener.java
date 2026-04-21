package ru.bsuedu.cad.lab.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.bsuedu.cad.lab.service.DataLoaderService;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Инициализация приложения — загрузка данных из CSV...");
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(sce.getServletContext());
        DataLoaderService dataLoaderService = ctx.getBean(DataLoaderService.class);
        dataLoaderService.loadAll();
        LOGGER.info("Данные из CSV успешно загружены.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Приложение остановлено.");
    }
}
