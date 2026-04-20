# Лабораторная работа №1. Gradle. Внедрение зависимостей

## Цель работы

Создать каркас консольного Spring-приложения «Магазин товаров для животных», сконфигурированного с помощью Java-конфигурации. Приложение считывает данные о товарах из CSV-файла и выводит их в консоль в виде таблицы.

## Ход работы

### 1. Создание проекта

Создан Gradle-проект со следующими параметрами:

- **Project name:** product-table
- **Пакет:** ru.bsuedu.cad.lab
- **Тип:** Application (Single application project)
- **Язык:** Java 17
- **DSL:** Kotlin
- **Тестовый фреймворк:** JUnit Jupiter
- **Gradle:** 8.12

### 2. Добавление зависимости Spring

В `build.gradle.kts` добавлена зависимость:

```kotlin
implementation("org.springframework:spring-context:6.2.2")
```

### 3. Структура приложения

Реализована следующая архитектура на основе принципов IoC и DI:

```
ru.bsuedu.cad.lab
├── App.java                    — точка входа, создание Spring-контекста
├── AppConfiguration.java       — Java-конфигурация бинов (@Configuration)
├── Product.java                — сущность «Товар»
├── Reader.java                 — интерфейс чтения данных
├── ResourceFileReader.java     — реализация: чтение CSV из classpath
├── Parser.java                 — интерфейс парсинга
├── CSVParser.java              — реализация: разбор CSV-строки в список Product
├── ProductProvider.java        — интерфейс поставщика товаров
├── ConcreteProductProvider.java — реализация: связывает Reader и Parser
├── Renderer.java               — интерфейс отрисовки
└── ConsoleTableRenderer.java   — реализация: вывод таблицы в консоль
```

### 4. Описание классов

- **Product** — POJO-класс с полями: `productId`, `name`, `description`, `categoryId`, `price`, `stockQuantity`, `imageUrl`, `createdAt`, `updatedAt`.
- **Reader / ResourceFileReader** — читает содержимое файла `product.csv` из `src/main/resources` через `ClassLoader.getResourceAsStream()`.
- **Parser / CSVParser** — разбирает CSV-текст (с учётом кавычек) в список объектов `Product`.
- **ProductProvider / ConcreteProductProvider** — объединяет `Reader` и `Parser`, возвращает готовый список товаров.
- **Renderer / ConsoleTableRenderer** — форматирует и выводит список товаров в консоль в виде ASCII-таблицы.
- **AppConfiguration** — класс с аннотацией `@Configuration`, определяет бины через `@Bean`-методы и внедряет зависимости через параметры методов.
- **App** — создаёт `AnnotationConfigApplicationContext`, получает бин `Renderer` и вызывает `render()`.

### 5. Принцип работы

1. `App.main()` создаёт Spring-контекст на основе `AppConfiguration`.
2. Spring создаёт и связывает бины: `Reader → Parser → ProductProvider → Renderer`.
3. Вызывается `renderer.render()`, который через цепочку зависимостей читает CSV, парсит его и выводит таблицу.

### 6. Запуск

```bash
cd les02/lab
gradle run
```

## Результат

Приложение успешно компилируется, запускается командой `gradle run`, выводит таблицу товаров в консоль и завершается.
