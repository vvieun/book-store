# Развёртывание (Bookstore — Java + React)

## Вариант 1: Docker Compose (рекомендуется)

Из корня проекта:

```bash
./run.sh start
```

Сервисы: PostgreSQL (`bookstore_db`), Redis, Spring Boot API (порт 8080), React (порт 3000).  
Схема БД создаётся при первом старте API (JPA `ddl-auto=update`).

## Вариант 2: Локально

1. Установить и запустить PostgreSQL 15 и Redis 7.
2. Создать БД: `CREATE DATABASE bookstore_db;`
3. Запустить API из `code/api-java`:
   - задать переменные окружения (хост/порт БД и Redis);
   - выполнить: `mvn spring-boot:run` (main: `ru.bmstu.iu7.bookstore.BookStoreApplication`).
4. Frontend из `code/frontend`: `npm install && npm start`, в `.env`: `REACT_APP_API_URL=http://localhost:8080`.

## Переменные окружения API (Java)

| Переменная    | По умолчанию  | Описание        |
|---------------|---------------|-----------------|
| DB_HOST       | localhost     | Хост PostgreSQL |
| DB_PORT       | 5432          | Порт PostgreSQL |
| DB_NAME       | bookstore_db  | Имя БД          |
| DB_USER       | postgres      | Пользователь    |
| DB_PASSWORD   | postgres      | Пароль          |
| REDIS_HOST    | localhost     | Хост Redis      |
| REDIS_PORT    | 6379          | Порт Redis      |
