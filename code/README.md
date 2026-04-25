# Книжный магазин с системой рекомендаций

Курсовой проект по дисциплине «Базы данных».

## Стек

- **Backend:** Spring Boot 3.2 (Java 17), JPA, Redis
- **База данных:** PostgreSQL 15
- **Кэш:** Redis 7
- **Frontend:** React 18
- **Сборка и запуск:** Docker Compose

## Быстрый старт

### Требования

- Docker и Docker Compose

### Запуск

```bash
# из корня проекта
./run.sh start
```

Будут запущены:

- **PostgreSQL** — порт 5432, БД `bookstore_db` (схему создаёт JPA при старте API)
- **Redis** — порт 6379
- **API (Java)** — порт 8080
- **Frontend** — порт 3000

### Ссылки

- **API:** http://localhost:8080  
- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **Frontend:** http://localhost:3000  

### Остановка

```bash
./run.sh stop
```

## API (Java)

- **Книги:** `GET/POST` `/api/books`, `/api/books/{id}`, `/api/books/search`, `/api/books/category/{id}`, `/api/books/top`
- **Отзывы:** `GET/POST/DELETE` `/api/reviews`, `/api/reviews/user/{id}`, `/api/reviews/book/{id}`
- **Заказы:** `GET/POST` `/api/orders`, `/api/orders/user/{id}`
- **Рекомендации:** `GET` `/api/recommendations/hybrid`, `/api/recommendations/collaborative`, `/api/recommendations/content-based`

## Структура

```
code/
├── docker-compose.yml    # PostgreSQL, Redis, API (Java), Frontend
├── api-java/             # Spring Boot приложение (книги, отзывы, заказы, рекомендации)
├── frontend/             # React SPA
└── sql/                  # Опциональные SQL-скрипты (для отчёта)
```

## Локальный запуск без Docker

1. PostgreSQL и Redis должны быть запущены.
2. В `api-java` задать переменные: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`.
3. Запуск API: из `api-java` выполнить `mvn spring-boot:run` (или указать main class `ru.bmstu.iu7.bookstore.BookStoreApplication`).
4. Frontend: из `frontend` выполнить `npm start`, в `.env` указать `REACT_APP_API_URL=http://localhost:8080`.
