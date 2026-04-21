# Лабораторная работа №5. Разработка и развертывание Web-приложений

## Цель работы

Добавить веб-интерфейс к приложению магазина зоотоваров с использованием Java-сервлетов. Реализовать страницу списка заказов, форму создания заказа и REST-сервис для получения информации о продуктах. Развернуть приложение на Apache Tomcat 11.

## Выполненные задачи

1. Скопирован и адаптирован проект из лабораторной работы №4.
2. Проект переведён на сборку WAR-файла (плагин `war` в Gradle).
3. Настроен Spring `ContextLoaderListener` через `web.xml` для инициализации Spring-контекста в Tomcat.
4. Реализован `AppContextListener` (`@WebListener`) — загрузка данных из CSV при старте приложения.
5. Реализован `OrderListServlet` (`/orders`) — страница со списком всех заказов и кнопкой создания нового.
6. Реализован `OrderCreateServlet` (`/orders/create`) — форма создания заказа (GET — форма, POST — обработка). После создания — редирект на список заказов.
7. Реализован `ProductRestServlet` (`/api/products`) — REST-сервис, возвращающий JSON с информацией о продуктах (название, категория, остаток на складе).
8. Приложение собирается командой `gradle war`.

## Структура пакетов

```
ru.bsuedu.cad.lab               — конфигурация (ConfigBasic, ConfigJpa)
ru.bsuedu.cad.lab.entity        — JPA сущности
ru.bsuedu.cad.lab.repository    — Spring Data JPA репозитории
ru.bsuedu.cad.lab.service       — бизнес-логика
ru.bsuedu.cad.lab.servlet       — сервлеты (Web UI + REST API)
```

## Технологии

- Java 17
- Spring Context / ORM / Web / Data JPA
- Hibernate 6.2.0.Final + HikariCP
- H2 Database (in-memory)
- Jakarta Servlet 6.0
- Jackson (JSON-сериализация)
- Apache Tomcat 11
- Gradle (WAR)

## Сборка и деплой

```bash
cd les10/lab
gradle war
```

WAR-файл: `app/build/libs/app.war`

Деплой на Tomcat:
1. Скопировать `app.war` в `$CATALINA_HOME/webapps/`
2. Или загрузить через Tomcat Manager (`http://localhost:8080/manager/html`)

## Эндпоинты

| URL | Метод | Описание |
|-----|-------|----------|
| `/app/orders` | GET | Страница со списком заказов |
| `/app/orders/create` | GET | Форма создания заказа |
| `/app/orders/create` | POST | Обработка формы создания заказа |
| `/app/api/products` | GET | REST API — список продуктов (JSON) |

## Пример REST-ответа `/api/products`

```json
[
  {
    "productId": 1,
    "name": "Сухой корм для собак",
    "categoryName": "Корма",
    "stockQuantity": 50
  },
  {
    "productId": 2,
    "name": "Игрушка для кошек Мышка",
    "categoryName": "Игрушки",
    "stockQuantity": 200
  }
]
```

## UML-диаграмма классов

```mermaid
classDiagram
    class ConfigBasic {
        -String driverClassName
        -String url
        -String username
        -String password
        +dataSource() DataSource
    }

    class ConfigJpa {
        -DataSource dataSource
        +entityManagerFactory() LocalContainerEntityManagerFactoryBean
        +transactionManager(EntityManagerFactory) PlatformTransactionManager
    }

    class Category {
        -Long categoryId
        -String name
        -String description
        -List~Product~ products
    }

    class Product {
        -Long productId
        -String name
        -String description
        -Category category
        -BigDecimal price
        -Integer stockQuantity
        -String imageUrl
        -LocalDate createdAt
        -LocalDate updatedAt
    }

    class Customer {
        -Long customerId
        -String name
        -String email
        -String phone
        -String address
    }

    class Order {
        -Long orderId
        -Customer customer
        -LocalDateTime orderDate
        -BigDecimal totalPrice
        -String status
        -String shippingAddress
        -List~OrderDetail~ orderDetails
    }

    class OrderDetail {
        -Long orderDetailId
        -Order order
        -Product product
        -Integer quantity
        -BigDecimal price
    }

    class CategoryRepository {
        <<interface>>
    }
    class ProductRepository {
        <<interface>>
        +findAllWithCategory() List~Product~
    }
    class CustomerRepository {
        <<interface>>
    }
    class OrderRepository {
        <<interface>>
        +findAllWithDetails() List~Order~
    }
    class OrderDetailRepository {
        <<interface>>
    }

    class DataLoaderService {
        +loadAll()
    }

    class OrderService {
        +createOrder(Long, List~Long~, List~Integer~) Order
        +getAllOrders() List~Order~
    }

    class AppContextListener {
        +contextInitialized(ServletContextEvent)
        +contextDestroyed(ServletContextEvent)
    }

    class OrderListServlet {
        +doGet(HttpServletRequest, HttpServletResponse)
    }

    class OrderCreateServlet {
        +doGet(HttpServletRequest, HttpServletResponse)
        +doPost(HttpServletRequest, HttpServletResponse)
    }

    class ProductRestServlet {
        +doGet(HttpServletRequest, HttpServletResponse)
    }

    Category "1" --> "*" Product : содержит
    Customer "1" --> "*" Order : размещает
    Order "1" --> "*" OrderDetail : содержит
    Product "1" --> "*" OrderDetail : включен в

    CategoryRepository ..> Category
    ProductRepository ..> Product
    CustomerRepository ..> Customer
    OrderRepository ..> Order
    OrderDetailRepository ..> OrderDetail

    DataLoaderService --> CategoryRepository
    DataLoaderService --> CustomerRepository
    DataLoaderService --> ProductRepository

    OrderService --> OrderRepository
    OrderService --> OrderDetailRepository
    OrderService --> CustomerRepository
    OrderService --> ProductRepository

    AppContextListener --> DataLoaderService
    OrderListServlet --> OrderService
    OrderCreateServlet --> OrderService
    OrderCreateServlet --> CustomerRepository
    OrderCreateServlet --> ProductRepository
    ProductRestServlet --> ProductRepository

    ConfigJpa --> ConfigBasic

    JpaRepository <|-- CategoryRepository
    JpaRepository <|-- ProductRepository
    JpaRepository <|-- CustomerRepository
    JpaRepository <|-- OrderRepository
    JpaRepository <|-- OrderDetailRepository

    HttpServlet <|-- OrderListServlet
    HttpServlet <|-- OrderCreateServlet
    HttpServlet <|-- ProductRestServlet
    ServletContextListener <|.. AppContextListener
```
