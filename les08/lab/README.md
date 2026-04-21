# Лабораторная работа №4. Технологии работы с базами данных. JPA. Spring Data

## Цель работы

Выполнить рефакторинг приложения магазина зоотоваров: перейти с использования Spring JDBC на ORM Hibernate и Spring Data JPA. Расширить приложение новыми сущностями и привести структуру в соответствие со слоистой архитектурой.

## Выполненные задачи

1. Создано приложение с использованием **Spring Context**, **Hibernate ORM** и **Spring Data JPA**.
2. Настроен **DataSource** с использованием **H2** (in-memory) и **HikariCP**.
3. Схема данных создаётся автоматически на основании JPA-сущностей (`hbm2ddl.auto = create`).
4. Реализованы 5 JPA-сущностей: `Category`, `Product`, `Customer`, `Order`, `OrderDetail`.
5. Реализованы Spring Data JPA репозитории для каждой сущности.
6. Реализованы сервисы: `DataLoaderService` (загрузка CSV) и `OrderService` (создание и получение заказов).
7. Реализован клиент `Client`, который загружает данные из CSV, создаёт заказ в транзакции и выводит доказательство сохранения.
8. Приложение запускается через `gradle run`.

## Структура пакетов

```
ru.bsuedu.cad.lab             — основной пакет (App, Config)
ru.bsuedu.cad.lab.entity      — JPA сущности
ru.bsuedu.cad.lab.repository   — Spring Data JPA репозитории
ru.bsuedu.cad.lab.service      — бизнес-логика (сервисы)
ru.bsuedu.cad.lab.app          — клиентское приложение
```

## Технологии

- Java 17
- Spring Context 6.2.2
- Spring ORM 6.2.2
- Spring Data JPA 3.4.4
- Hibernate 6.2.0.Final
- HikariCP 5.0.1
- H2 Database 2.2.224
- Logback 1.5.6
- Gradle (Kotlin DSL)

## Запуск

```bash
cd les08/lab
gradle run
```

## UML-диаграмма классов

```mermaid
classDiagram
    class App {
        +main(String[] args)
    }

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
        +getCategoryId() Long
        +getName() String
        +getDescription() String
        +getProducts() List~Product~
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
        -List~OrderDetail~ orderDetails
        +getProductId() Long
        +getName() String
        +getPrice() BigDecimal
        +getCategory() Category
    }

    class Customer {
        -Long customerId
        -String name
        -String email
        -String phone
        -String address
        -List~Order~ orders
        +getCustomerId() Long
        +getName() String
        +getEmail() String
    }

    class Order {
        -Long orderId
        -Customer customer
        -LocalDateTime orderDate
        -BigDecimal totalPrice
        -String status
        -String shippingAddress
        -List~OrderDetail~ orderDetails
        +getOrderId() Long
        +getCustomer() Customer
        +getTotalPrice() BigDecimal
    }

    class OrderDetail {
        -Long orderDetailId
        -Order order
        -Product product
        -Integer quantity
        -BigDecimal price
        +getOrderDetailId() Long
        +getQuantity() Integer
        +getPrice() BigDecimal
    }

    class CategoryRepository {
        <<interface>>
    }

    class ProductRepository {
        <<interface>>
    }

    class CustomerRepository {
        <<interface>>
    }

    class OrderRepository {
        <<interface>>
    }

    class OrderDetailRepository {
        <<interface>>
    }

    class DataLoaderService {
        -CategoryRepository categoryRepository
        -CustomerRepository customerRepository
        -ProductRepository productRepository
        +loadAll()
    }

    class OrderService {
        -OrderRepository orderRepository
        -OrderDetailRepository orderDetailRepository
        -CustomerRepository customerRepository
        -ProductRepository productRepository
        +createOrder(Long, List~Long~, List~Integer~) Order
        +getAllOrders() List~Order~
    }

    class Client {
        -DataLoaderService dataLoaderService
        -OrderService orderService
        +run()
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

    Client --> DataLoaderService
    Client --> OrderService

    App --> ConfigJpa
    App --> Client
    ConfigJpa --> ConfigBasic

    JpaRepository <|-- CategoryRepository
    JpaRepository <|-- ProductRepository
    JpaRepository <|-- CustomerRepository
    JpaRepository <|-- OrderRepository
    JpaRepository <|-- OrderDetailRepository
```

## Схема базы данных (ER-диаграмма)

```mermaid
erDiagram
    CATEGORIES {
        int category_id PK
        string name
        string description
    }

    PRODUCTS {
        int product_id PK
        string name
        string description
        int category_id FK
        decimal price
        int stock_quantity
        string image_url
        datetime created_at
        datetime updated_at
    }

    CUSTOMERS {
        int customer_id PK
        string name
        string email
        string phone
        string address
    }

    ORDERS {
        int order_id PK
        int customer_id FK
        datetime order_date
        decimal total_price
        string status
        string shipping_address
    }

    ORDER_DETAILS {
        int order_detail_id PK
        int order_id FK
        int product_id FK
        int quantity
        decimal price
    }

    CATEGORIES ||--o{ PRODUCTS : "содержит"
    CUSTOMERS ||--o{ ORDERS : "размещает"
    ORDERS ||--o{ ORDER_DETAILS : "содержит"
    PRODUCTS ||--o{ ORDER_DETAILS : "включен в"
```
