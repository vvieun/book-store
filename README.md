[English](README.md) · [Русский](README.ru.md)

# bookstore with a recommendation system

a school project: a bookstore with a spring boot backend, a react frontend, a postgresql database and a redis cache.

## contents

- [about the project](#about-the-project)
- [domain description and core entities](#domain-description-and-core-entities)
- [actors](#actors-roles)
- [use-case diagram](#use-case-diagram)
- [er diagram](#er-diagram)
- [user scenarios](#user-scenarios)
- [business processes (bpmn)](#business-processes-bpmn)
- [c4](#c4)
- [sequence diagrams](#sequence-diagrams)
- [database schema](#database-schema)
- [function algorithm flowcharts](#function-algorithm-flowcharts)
- [project structure](#project-structure)
- [stack](#stack)
- [running](#running)
- [research](#research)

## about the project

a bookstore database with personal book recommendations based on what each user does. the project has two parts:

- **bookstore** — the list of books, authors, publishers, orders, wishlists and book collections made by users;
- **recommendation system** — user reviews and ratings, and the saved results of the filtering algorithms.

the database is in third normal form. it uses a role-based access model. thanks to indexing and an outside cache (redis), normal queries run in 100 ms or less.

### recommendation system

the project uses two methods.

**collaborative filtering** — it finds users who are alike, using the ratings matrix. to measure how alike users $u_i$ and $u_j$ are, it uses the pearson correlation coefficient:

$$\text{sim}(u_i, u_j) = \frac{\sum_{p \in P} (r_{i,p} - \bar{r}_i)(r_{j,p} - \bar{r}_j)}{\sqrt{\sum_{p \in P} (r_{i,p} - \bar{r}_i)^2} \cdot \sqrt{\sum_{p \in P} (r_{j,p} - \bar{r}_j)^2}}$$

here $r_{i,p}$ is the rating that user $i$ gave to book $p$, $\bar{r}_i$ is user $i$'s average rating, and $P$ is the set of rated books. in the code (the db function `calculate_recommendations`), it picks up to 10 most alike users (correlation $> 0.3$ and at least 3 shared ratings). a book's score is the weighted average of their ratings:

$$s_{\text{collab}}(u, p) = \frac{\sum_{v \in N} \text{sim}(u, v) \cdot r_{v,p}}{\sum_{v \in N} \lvert \text{sim}(u, v) \rvert}$$

here $N$ is the set of alike users who rated book $p$.

**content-based filtering** — it matches a book's features against what the user likes. how well book $p$ fits user $u$:

$$\text{rel}(u, p) = \vec{u} \cdot \vec{p} = \sum_{i=1}^{n} u_i \cdot p_i$$

here $\vec{p} = (f_1, \ldots, f_n)$ is the book's feature vector and $\vec{u}$ is the user's preference vector. the profile is built from the books the user has rated (category and author weights). each candidate book gets a score $s_{\text{content}}$: the sum of matched weights, changed by the book's average rating.

### combining the approaches into a single score

the final recommendation mixes both methods. for a book found by both, the scores are added with weights:

$$\text{score}(u, p) = w \cdot s_{\text{collab}}(u, p) + (1 - w) \cdot s_{\text{content}}(u, p)$$

here $w$ is the weight of the collaborative part (`bookstore.recommendation.collaborative-weight`, $0.5$ by default). a book found by only one method keeps its score. the list is sorted from high score to low, cut to the asked size and saved in redis. if there are not enough personal picks, the system adds popular books (the top books by average rating).

## domain description and core entities

the domain is an online bookstore with personal book ideas.

core entities:
- **user** (user) — a person with an account in the system;
- **book** (book) — a book you can buy;
- **author** (author) — the author of a book;
- **category** (category) — a genre or section;
- **publisher** (publisher) — the publisher of a book;
- **review** (review) — a rating and comment on a book;
- **order** (order) — a purchase of one or more books;
- **order item** (orderitem) — one book in an order, with its quantity and price;
- **wishlist** (wishlist) — books the user wants to buy later;
- **collection** (collection) — a book collection made by a user, with its books (collection_books);
- **recommendation** (recommendation) — the saved result of the recommendation algorithm for a user.

## actors (roles)

| actor | description |
|---|---|
| **guest** (guest) | a user without an account. can look at the catalog, book pages, read reviews and use search. cannot get personal book ideas or place orders. |
| **customer** (customer) | a user with an account. can order books, leave reviews, get personal book ideas and keep a wishlist. |
| **moderator** (moderator) | a staff member. has all customer rights, and can also edit book entries, delete reviews that break the rules, and see overall statistics. |
| **admin** (admin) | full access to everything: managing users, roles, the catalog, categories, publishers and authors, clearing the cache, and viewing the action log. |

## use-case diagram

_Diagram is in the [Russian version](README.ru.md)._

## er diagram

_Diagram is in the [Russian version](README.ru.md)._

## user scenarios

### scenario 1: a customer finds and orders a book from a recommendation

**actor:** customer (authenticated)  
**precondition:** the user has left some reviews and made some purchases before.  
**main flow:**

1. the user opens the "recommendations" section.
2. the system looks at the user's ratings and past purchases and builds a personal list of books.
3. the user sees a list of suggested books with short notes.
4. the user picks a book and opens its page.
5. reads the description and reviews from other customers.
6. clicks "order".
7. the system makes a new order with the status "processing".

**postcondition:** an order is made. on the next request, the recommendations are updated to include the new purchase.

### scenario 2: a customer leaves a review on a book

**actor:** customer (authenticated)  
**precondition:** the user has already got and read the ordered book.  
**main flow:**

1. the user opens the book page and goes to the "reviews" section.
2. fills in the form: picks a rating (1–5 stars) and writes a comment.
3. sends the review.
4. the system checks if the user has reviewed this book before:
   - no — it makes a new review and works out the book's average rating again;
   - yes — it updates the old review and works out the rating again.
5. the review shows up in the list on the book page.

**postcondition:** the review is saved, the book's average rating is updated, and the recommendations are worked out again on the next request.

### scenario 3: a moderator deletes a violating review

**actor:** moderator  
**precondition:** someone has reported a review with offensive content.  
**main flow:**

1. the moderator opens the review management section.
2. finds the review that breaks the service rules.
3. clicks "delete review" and confirms.
4. the system deletes the review and works out the average rating of that book again.
5. the moderator gets a message that the delete worked.

**postcondition:** the review is deleted, the book's rating is worked out again, and the recommendations are updated on the next request.

### scenario 4: an admin clears stale recommendations

**actor:** admin  
**precondition:** many new books have been added to the catalog, so the old recommendations are out of date.  
**main flow:**

1. the admin opens the system management section.
2. clicks "clear the recommendation cache".
3. the system drops the old saved recommendations.
4. the admin gets a message that the action worked.
5. on the next request, the system builds the recommendations again, using the current catalog.

**postcondition:** the old recommendations are gone, and users get fresh personal lists.

## business processes (bpmn)

### process 1: building recommendations
_Diagram is in the [Russian version](README.ru.md)._

### process 2: placing and processing an order
_Diagram is in the [Russian version](README.ru.md)._

## c4

### l1
_Diagram is in the [Russian version](README.ru.md)._

### l2
_Diagram is in the [Russian version](README.ru.md)._

### l3
_Diagram is in the [Russian version](README.ru.md)._

### l4 repository
_Diagram is in the [Russian version](README.ru.md)._

### l4 services
_Diagram is in the [Russian version](README.ru.md)._

## sequence diagrams

### building personal recommendations
_Diagram is in the [Russian version](README.ru.md)._

### placing and processing an order
_Diagram is in the [Russian version](README.ru.md)._

## database schema

dbms: **postgresql**.
_Diagram is in the [Russian version](README.ru.md)._

## function algorithm flowcharts

### create interaction on rating
_Diagram is in the [Russian version](README.ru.md)._

### update product by rating
_Diagram is in the [Russian version](README.ru.md)._

### calculate recommendations
_Diagram is in the [Russian version](README.ru.md)._

### get popular products by category
_Diagram is in the [Russian version](README.ru.md)._

### get user statistics
_Diagram is in the [Russian version](README.ru.md)._

## project structure

```
code/
  backend/      spring boot (java 17): domain, dto, repository (postgresql + mongodb), service, techui-консоль
  frontend/     react + typescript (vite): слои domain / application / infrastructure / presentation
  sql/          схема, индексы, триггеры, функции, роли, helper-функции генерации данных
  scripts/      generate_data.py (наполнение бд), benchmark_indexes.py, plot_index_graphs.py
  benchmarks/   результаты замеров индексов (csv/json)
  docker-compose.yml, run.sh
diagrams/       исходники и png всех диаграмм (er, c4, bpmn, sequence, схема, алгоритмы, графики исследования)
run.sh          локальный запуск без контейнеров приложения
```

## stack

- backend: java 17, spring boot 3, spring security, spring data jpa;
- database: postgresql 15 (main; there is a mongodb profile);
- cache: redis 7;
- frontend: react 18 + typescript (vite), served through nginx in docker;
- infrastructure: docker compose (postgres, redis, api, nginx).

## running

### docker (recommended)

you need docker (and `python3` for the `seed` step). one command builds the backend and frontend images and starts postgres, redis, api and nginx with the ui:

```bash
./code/run.sh start                         # = docker compose up -d --build
./code/run.sh seed                          # тестовые данные (generate_data.py)
./code/run.sh logs [api|frontend|postgres|redis]
./code/run.sh stop
```

this is the same as: `docker compose -f code/docker-compose.yml up -d --build`.

after it starts:
- ui — http://localhost:3000 (nginx, sends `/api` to the backend)
- rest api — http://localhost:8080
- postgresql — localhost:5433 (database `bookstore_db`)
- redis — localhost:6379

test login after `seed`: `admin` / `admin123`.

### local development

without the application containers (you need java 17, maven, node/npm; postgres and redis run in docker). the frontend is built with vite straight into spring boot's static resources. the backend runs locally and serves the api and ui at http://localhost:8080:

```bash
./run.sh start
./run.sh seed
./run.sh stop
```

## research

### research setup

for the tests, the script makes a dataset, switches between index setups, runs sets of measurements, and saves the results together with the `explain analyze` plans.

after setup, the data size was:
- `users` — 1000 rows;
- `reviews` — 29480;
- `orders` — 30000;
- `order_items` — 30000.

these setups were compared: no indexes, simple, composite, simple plus composite, redundant.

simple indexes were built for the columns `category_id`, `user_id`, `created_at`, `avg_rating`, `order_id`, `publisher_id`.
composite ones — for the sets `(category_id, book_id)`, `(user_id, rating)`, `(user_id, status, created_at)`, `(book_id, rating)`.
the redundant set added indexes for `price`, `created_at`, `total_amount`.

the queries that were measured:
1. top books by category (q1);
2. a user's reviews sorted by rating (q2);
3. a user's orders by date (q3);
4. rating statistics grouped by category (q4);
5. a user's order history with a `join` on items (q5);
6. write: inserting an order and an item (with a transaction rollback) (q6).

for each "query-setup" pair, 20 warm-up runs and 250 measured runs were done.
before each set, the indexes were rebuilt to match the setup and `analyze` was run.

### research results

summary of run times, ms:

| index configuration | average read time | average write time |
|---|---:|---:|
| no indexes | 0.9068 | 5.7547 |
| simple indexes | 0.8125 | 6.0368 |
| composite indexes | 0.8525 | 6.0350 |
| simple and composite indexes | 0.8144 | 6.0965 |
| redundant indexing | 0.8622 | 6.3980 |

chart of the average run time of queries `q1-q6`:

_Diagram is in the [Russian version](README.ru.md)._

chart of the average run time by number of indexes:

_Diagram is in the [Russian version](README.ru.md)._

for the order-history query with a `join`, the plan with no indexes uses sequential scans, while the setups with simple indexes use index reads. this matches the lower read time in the measurements. with the redundant setup, the average read time goes up compared to the simple and combined indexes: some extra indexes are not used in the queries but still make planning and upkeep cost more.

### conclusion

the lowest average read time was with simple indexes — 0.8125 ms; for the "simple and composite indexes" set — 0.8144 ms; and with no indexes — 0.9068 ms.
for writes, the lowest average time was 5.7547 ms with no indexes; with the "simple and composite indexes" set it goes up to 6.0965 ms, and with redundant indexing — to 6.3980 ms.

indexes make reads a lot faster (the best result is with simple indexes), but writes get slower. the "simple and composite indexes" set gives a read time close to the best, with only a small drop in write speed.
