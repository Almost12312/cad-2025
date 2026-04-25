# Лабораторная работа 7. Spring Security. Basic Authentication

## Описание

В рамках лабораторной работы в приложение «Магазин зоотоваров» был добавлен ролевой доступ с использованием Spring Security.

## Что было сделано

1. **Скопирован проект из лабораторной работы №6** (`les12/lab`) в директорию `les14/lab`.
2. **Добавлены зависимости Spring Security** (`spring-security-web`, `spring-security-config`) в `build.gradle.kts` и `libs.versions.toml`.
3. **Конфигурация разделена** на три класса по образцу демо-проекта:
   - `ConfigDB` — DataSource, JPA, транзакции
   - `ConfigWeb` — Thymeleaf, Spring MVC
   - `ConfigSecurity` — настройки безопасности
4. **Добавлены два пользователя** (in-memory):
   - `user` / `1234` — роль `USER` (только просмотр заказов)
   - `manager` / `1234` — роль `MANAGER` (все операции с заказами: создание, редактирование, удаление)
5. **Реализована аутентификация через форму** (Form Login) для веб-интерфейса (`/orders/**`):
   - Создана кастомная страница входа (`login.html`)
   - Добавлен `LoginController`
   - При успешном входе — редирект на `/orders`
6. **Реализована Basic Authentication** для REST API (`/api/**`):
   - Отдельный `SecurityFilterChain` с `@Order(1)` и `securityMatcher("/api/**")`
   - CSRF отключен для API
7. **Разграничение прав в веб-интерфейсе**:
   - Пользователь `user` видит только список заказов (кнопки создания/редактирования/удаления скрыты)
   - Пользователь `manager` имеет полный доступ ко всем операциям
   - Серверная авторизация через `requestMatchers` + `hasRole`
8. **AppInitializer** переведён на `AbstractAnnotationConfigDispatcherServletInitializer` (как в демо)
9. **Добавлен `SecurityInitializer`** — наследник `AbstractSecurityWebApplicationInitializer` для регистрации фильтра Spring Security.

## Сборка и деплой

```bash
# Сборка WAR
./gradlew war

# WAR-файл будет в app/build/libs/pet-store.war
# Скопировать в $CATALINA_HOME/webapps/ и запустить Tomcat 11
```

## Тестирование

### Веб-интерфейс (Form Login)

- Открыть `http://localhost:8080/pet-store/orders` → редирект на страницу входа
- Войти как `user` / `1234` → список заказов без кнопок управления
- Войти как `manager` / `1234` → полный доступ (создание, редактирование, удаление)

### REST API (Basic Authentication)

```bash
# Получить все заказы (user)
curl -u user:1234 http://localhost:8080/pet-store/api/orders

# Создать заказ (manager)
curl -u manager:1234 -X POST http://localhost:8080/pet-store/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"productIds":[1,2],"quantities":[2,3]}'

# Без авторизации → 401 Unauthorized
curl http://localhost:8080/pet-store/api/orders
```

## Учётные записи

| Логин   | Пароль | Роль    | Веб-интерфейс                 | REST API         |
|---------|--------|---------|-------------------------------|------------------|
| user    | 1234   | USER    | Только просмотр заказов       | GET-запросы      |
| manager | 1234   | MANAGER | Все операции с заказами       | Все методы       |

## UML-диаграмма классов

```mermaid
classDiagram
    direction LR

    class AppInitializer {
        +getRootConfigClasses() Class[]
        +getServletConfigClasses() Class[]
        +getServletMappings() String[]
    }

    class SecurityInitializer {
    }

    class ConfigDB {
        -driverClassName: String
        -url: String
        -username: String
        -password: String
        +dataSource() DataSource
        +entityManagerFactory() LocalContainerEntityManagerFactoryBean
        +transactionManager() PlatformTransactionManager
    }

    class ConfigWeb {
        +templateResolver() ClassLoaderTemplateResolver
        +templateEngine() SpringTemplateEngine
        +viewResolver() ViewResolver
    }

    class ConfigSecurity {
        +apiFilterChain(http: HttpSecurity) SecurityFilterChain
        +webFilterChain(http: HttpSecurity) SecurityFilterChain
        +userDetailsService() UserDetailsService
    }

    class LoginController {
        +login() String
    }

    class OrderController {
        -orderService: OrderService
        -customerRepository: CustomerRepository
        -productRepository: ProductRepository
        +listOrders(model: Model, auth: Authentication) String
        +showCreateForm(model: Model) String
        +createOrder(...) String
        +showEditForm(id: Long, model: Model) String
        +updateOrder(...) String
        +deleteOrder(id: Long) String
    }

    class OrderRestController {
        -orderService: OrderService
        +getAllOrders() ResponseEntity
        +getOrderById(id: Long) ResponseEntity
        +createOrder(request: CreateOrderRequest) ResponseEntity
        +updateOrder(id: Long, request: UpdateOrderRequest) ResponseEntity
        +deleteOrder(id: Long) ResponseEntity
    }

    class OrderService {
        -orderRepository: OrderRepository
        -orderDetailRepository: OrderDetailRepository
        -customerRepository: CustomerRepository
        -productRepository: ProductRepository
        +getAllOrders() List~Order~
        +getOrderById(id: Long) Order
        +createOrder(...) Order
        +updateOrder(...) Order
        +deleteOrder(id: Long) void
    }

    class DataLoaderService {
        -categoryRepository: CategoryRepository
        -customerRepository: CustomerRepository
        -productRepository: ProductRepository
        +loadAll() void
    }

    class DataInitListener {
        -dataLoaderService: DataLoaderService
        -initialized: boolean
        +onApplicationEvent(event: ContextRefreshedEvent) void
    }

    class Order {
        -orderId: Long
        -customer: Customer
        -orderDate: LocalDateTime
        -totalPrice: BigDecimal
        -status: String
        -shippingAddress: String
        -orderDetails: List~OrderDetail~
    }

    class OrderDetail {
        -orderDetailId: Long
        -order: Order
        -product: Product
        -quantity: Integer
        -price: BigDecimal
    }

    class Customer {
        -customerId: Long
        -name: String
        -email: String
        -phone: String
        -address: String
        -orders: List~Order~
    }

    class Product {
        -productId: Long
        -name: String
        -description: String
        -category: Category
        -price: BigDecimal
        -stockQuantity: Integer
        -imageUrl: String
        -createdAt: LocalDate
        -updatedAt: LocalDate
    }

    class Category {
        -categoryId: Long
        -name: String
        -description: String
        -products: List~Product~
    }

    class OrderRepository {
        <<interface>>
        +findAllWithDetails() List~Order~
        +findByIdWithDetails(id: Long) Optional~Order~
    }

    class OrderDetailRepository {
        <<interface>>
    }

    class CustomerRepository {
        <<interface>>
    }

    class ProductRepository {
        <<interface>>
        +findAllWithCategory() List~Product~
    }

    class CategoryRepository {
        <<interface>>
    }

    AppInitializer --> ConfigDB
    AppInitializer --> ConfigSecurity
    AppInitializer --> ConfigWeb

    OrderController --> OrderService
    OrderController --> CustomerRepository
    OrderController --> ProductRepository

    OrderRestController --> OrderService

    OrderService --> OrderRepository
    OrderService --> OrderDetailRepository
    OrderService --> CustomerRepository
    OrderService --> ProductRepository

    DataLoaderService --> CategoryRepository
    DataLoaderService --> CustomerRepository
    DataLoaderService --> ProductRepository

    DataInitListener --> DataLoaderService

    Order --> Customer
    Order --> OrderDetail
    OrderDetail --> Product
    Product --> Category
    Customer --> Order

    ConfigSecurity ..> SecurityInitializer : регистрация фильтра
```
