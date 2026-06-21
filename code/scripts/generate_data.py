#!/usr/bin/env python3

import sys
import random
import hashlib
from datetime import datetime, timedelta

try:
    import psycopg2
except ImportError:
    print("ОШИБКА: не найден psycopg2")
    sys.exit(1)

def hash_pw(p: str) -> str:
    return hashlib.sha256(p.encode("utf-8")).hexdigest()

DB_CONFIG = {
    "host": "localhost",
    "port": 5433,
    "database": "bookstore_db",
    "user": "postgres",
    "password": "postgres",
}
PUBLISHERS = [
    ("Эксмо",          "Россия", "https://eksmo.ru"),
    ("АСТ",            "Россия", "https://ast.ru"),
    ("Азбука-Аттикус", "Россия", "https://atticus-group.ru"),
    ("МИФ",            "Россия", "https://mann-ivanov-ferber.ru"),
    ("Питер",          "Россия", "https://piter.com"),
]

PUBLISHER_PREFIXES = [
    "Книжный дом", "Редакция", "Библиотека", "Литера", "Академкнига",
    "Северная книга", "Новый текст", "Городские книги", "Универсум", "Печатный двор",
]

CATEGORIES = [
    (13, "Классическая литература", "Произведения мировой классики"),
    (14, "Фантастика",              "Фэнтези, мистика и волшебство"),
    (15, "Детективы",               "Детективные романы и триллеры"),
    (16, "Романы",                  "Любовные и социальные романы"),
    (17, "Исторические книги",      "Исторические романы и документальная проза"),
    (18, "Научная фантастика",      "Научно-фантастические произведения"),
    (19, "Приключения",             "Приключенческая литература"),
    (20, "Биографии",               "Биографии и мемуары"),
    (21, "Поэзия",                  "Поэтические сборники"),
    (22, "Философия",               "Философские труды"),
    (23, "Психология",              "Психология и саморазвитие"),
    (24, "Бизнес и экономика",      "Деловая литература"),
]

AUTHORS = [
    ("Лев Толстой",        "Великий русский писатель-романист",          "Россия"),
    ("Фёдор Достоевский",  "Русский писатель-психолог",                 "Россия"),
    ("Антон Чехов",        "Мастер короткого рассказа",                 "Россия"),
    ("Михаил Булгаков",    "Автор Мастера и Маргариты",                 "Россия"),
    ("Александр Пушкин",   "Основоположник русской литературы",         "Россия"),
    ("Иван Тургенев",      "Русский писатель-реалист",                  "Россия"),
    ("Николай Гоголь",     "Автор Мёртвых душ",                        "Россия"),
    ("Артур Конан Дойл",   "Создатель Шерлока Холмса",                  "Великобритания"),
    ("Агата Кристи",       "Королева детектива",                        "Великобритания"),
    ("Жюль Верн",          "Пионер научной фантастики",                 "Франция"),
    ("Герберт Уэллс",      "Классик научной фантастики",                "Великобритания"),
    ("Рэй Брэдбери",       "Американский фантаст",                      "США"),
    ("Джек Лондон",        "Автор приключенческих романов",             "США"),
    ("Эрнест Хемингуэй",   "Нобелевский лауреат по литературе",         "США"),
    ("Оноре де Бальзак",   "Мастер реалистического романа",             "Франция"),
]

FIRST_NAMES = [
    "Алексей", "Мария", "Иван", "Елена", "Дмитрий", "Ольга", "Сергей", "Анна",
    "Николай", "Виктория", "Павел", "Татьяна", "Андрей", "Ирина", "Михаил",
    "Наталья", "Владимир", "Софья", "Роман", "Дарья",
]
LAST_NAMES = [
    "Соколов", "Морозов", "Волкова", "Лебедев", "Новикова", "Кузнецов",
    "Смирнова", "Орлов", "Зайцева", "Фёдоров", "Павлова", "Медведев",
    "Егорова", "Никитин", "Крылова", "Макаров", "Андреева", "Белова",
    "Романов", "Тихонова",
]
COUNTRIES = ["Россия", "Великобритания", "США", "Франция", "Германия", "Италия", "Испания"]

TITLE_NOUNS = [
    "город", "сад", "дом", "мост", "архив", "остров", "путь", "портрет",
    "дневник", "лабиринт", "созвездие", "рукопись", "экспедиция", "тайна",
    "письмо", "берег", "комната", "замок", "сезон", "маршрут",
]
TITLE_ADJECTIVES = [
    "забытый", "последний", "северный", "тихий", "стеклянный", "лунный",
    "старый", "невидимый", "далёкий", "книжный", "ночной", "солнечный",
    "осенний", "секретный", "потерянный", "морской",
]
TITLE_SUFFIXES = [
    "в тумане", "на окраине", "в письмах", "у моря", "после полуночи",
    "для двоих", "без свидетелей", "в созвездии", "на старой карте",
    "в конце зимы",
]

BOOKS_DATA = [
    ("Война и мир",              "978-5-04-089536-1", 0,  890.0,
     "Эпопея о жизни русского общества в эпоху наполеоновских войн — грандиозный роман о судьбах людей",
     1274, "1869-01-01", [13],      [0]),
    ("Преступление и наказание", "978-5-04-089537-2", 0,  650.0,
     "Психологический роман о студенте Раскольникове, совершившем преступление и мучительно ищущем искупление",
     592,  "1866-01-01", [13],      [1]),
    ("Анна Каренина",            "978-5-04-089538-3", 1,  750.0,
     "Роман о трагической судьбе светской женщины, разрывающейся между долгом и страстью",
     864,  "1878-01-01", [13, 16],  [0]),
    ("Мастер и Маргарита",       "978-5-04-089539-4", 2,  580.0,
     "Загадочный роман о визите Воланда в советскую Москву, переплетающий мистику и сатиру",
     480,  "1967-01-01", [13, 14],  [3]),
    ("Евгений Онегин",           "978-5-04-089540-5", 1,  420.0,
     "Роман в стихах о пресыщённом жизнью молодом человеке, упустившем своё счастье",
     320,  "1833-01-01", [13, 21],  [4]),
    ("Отцы и дети",              "978-5-04-089541-6", 2,  380.0,
     "Роман о конфликте поколений в России — нигилист Базаров против дворянского уклада",
     320,  "1862-01-01", [13],      [5]),
    ("Мёртвые души",             "978-5-04-089542-7", 1,  490.0,
     "Поэма о похождениях предприимчивого Чичикова, скупающего умерших крепостных",
     400,  "1842-01-01", [13],      [6]),
    ("Братья Карамазовы",        "978-5-04-089543-8", 0,  780.0,
     "Философский роман о природе добра и зла через историю трёх братьев и убийства отца",
     896,  "1880-01-01", [13, 22],  [1]),

    ("Ночной Дозор",             "978-5-04-100001-1", 0,  520.0,
     "Мистический роман о противостоянии светлых и тёмных Иных в современной Москве",
     480,  "1998-01-01", [14],      [3]),
    ("Пикник на обочине",        "978-5-04-100002-2", 1,  440.0,
     "Роман о зоне — загадочном месте после визита инопланетян, где сталкеры ищут артефакты",
     224,  "1972-01-01", [14, 18],  [3]),
    ("Хроники Амбера",           "978-5-04-100004-4", 3,  680.0,
     "Фэнтезийный цикл о многомировой монархии, где принцы сражаются за трон истинного мира",
     640,  "1970-01-01", [14, 19],  [3]),
    ("Властелин колец",          "978-5-04-100005-5", 0,  890.0,
     "Эпическое фэнтези о походе Братства Кольца — одно из величайших произведений XX века",
     1216, "1954-01-01", [14, 19],  [3]),
    ("Гарри Поттер и философский камень", "978-5-389-07985-0", 2,  590.0,
     "История о мальчике-волшебнике, узнающем о своём предназначении в школе Хогвартс",
     432,  "1997-01-01", [14, 19],  [3]),
    ("Дюна",                     "978-5-04-100006-6", 1,  720.0,
     "Эпическая сага о пустынной планете Арракис и мальчике, ставшем пророком",
     688,  "1965-01-01", [14, 18],  [3]),

    ("Этюд в багровых тонах",    "978-5-04-200001-1", 1,  480.0,
     "Первое знакомство с великим детективом Шерлоком Холмсом и его методами дедукции",
     256,  "1887-01-01", [15],      [7]),
    ("Убийство в Восточном экспрессе", "978-5-04-200002-2", 0, 520.0,
     "Пуаро расследует убийство в заснеженном поезде, где каждый пассажир — под подозрением",
     288,  "1934-01-01", [15],      [8]),
    ("Десять негритят",          "978-5-04-200003-3", 2,  450.0,
     "Десять незнакомцев оказываются на острове, откуда нет выхода, и начинают погибать один за другим",
     224,  "1939-01-01", [15],      [8]),
    ("Собака Баскервилей",       "978-5-04-200004-4", 1,  420.0,
     "Холмс и Ватсон распутывают тайну проклятия рода Баскервилей на болотах Дартмура",
     240,  "1902-01-01", [15, 19],  [7]),
    ("Восточный детектив",       "978-5-04-200005-5", 0,  490.0,
     "Классический детектив в экзотических восточных декорациях от мастера жанра",
     272,  "1932-01-01", [15],      [8]),
    ("Свидетель обвинения",      "978-5-04-200006-6", 3,  380.0,
     "Судебный детектив с неожиданным поворотом — один из лучших рассказов Агаты Кристи",
     320,  "1953-01-01", [15],      [8]),

    ("Три товарища",             "978-5-04-300003-3", 2,  490.0,
     "Роман о дружбе и любви трёх ветеранов Первой мировой войны в послевоенной Германии",
     448,  "1936-01-01", [16],      [13]),
    ("Прощай, оружие!",          "978-5-04-300004-4", 1,  460.0,
     "Антивоенный роман Хемингуэя о любви американского офицера и британской медсестры",
     352,  "1929-01-01", [16, 17],  [13]),
    ("Старик и море",            "978-5-04-300005-5", 3,  320.0,
     "Притча о старом кубинском рыбаке и его борьбе с гигантским марлином в океане",
     128,  "1952-01-01", [16],      [13]),
    ("Дама с камелиями",         "978-5-04-300007-7", 1,  390.0,
     "Роман о трагической любви куртизанки Маргариты Готье и молодого Армана Дюваля",
     256,  "1848-01-01", [16],      [14]),

    ("451 градус по Фаренгейту", "978-5-04-400001-1", 0,  450.0,
     "Антиутопия о мире будущего, где книги запрещены, а пожарники их сжигают",
     256,  "1953-01-01", [18],      [11]),
    ("Машина времени",           "978-5-04-400002-2", 1,  380.0,
     "Путешествие в далёкое будущее, где человечество разделилось на два биологических вида",
     192,  "1895-01-01", [18, 19],  [10]),
    ("Война миров",              "978-5-04-400003-3", 2,  390.0,
     "Вторжение марсиан на Землю — один из первых романов о контакте с внеземными цивилизациями",
     224,  "1898-01-01", [18],      [10]),
    ("Путешествие к центру Земли","978-5-04-400004-4", 1, 420.0,
     "Захватывающая подземная экспедиция профессора Лиденброка через жерло вулкана",
     256,  "1864-01-01", [18, 19],  [9]),
    ("20000 лье под водой",      "978-5-04-400005-5", 0,  490.0,
     "Путешествие на борту подводной лодки Наутилус таинственного капитана Немо",
     448,  "1870-01-01", [18, 19],  [9]),
    ("Солярис",                  "978-5-04-400006-6", 2,  510.0,
     "Философский роман о контакте с разумным океаном планеты Солярис и природе познания",
     304,  "1961-01-01", [18, 22],  [3]),
]

DEMO_USERS = [
    ("classics_fan",    "classics@bookstore.test",   "demo123", "CUSTOMER",
     "Любитель классики — высокие оценки книгам 0-7 (классика)",        "classics"),
    ("fantasy_lover",   "fantasy@bookstore.test",    "demo123", "CUSTOMER",
     "Фанат фэнтези — высокие оценки книгам 8-13 (фантастика)",         "fantasy"),
    ("detective_fan",   "detective@bookstore.test",  "demo123", "CUSTOMER",
     "Любитель детективов — высокие оценки книгам 14-19 (детективы)",   "detective"),
    ("scifi_reader",    "scifi@bookstore.test",      "demo123", "CUSTOMER",
     "Читатель НФ — высокие оценки книгам 24-29 (научная фантастика)",  "scifi"),
    ("universal_reader","universal@bookstore.test",  "demo123", "CUSTOMER",
     "Универсал — оценивает всё подряд, средние-высокие оценки",        "universal"),
    ("romance_reader",  "romance@bookstore.test",    "demo123", "CUSTOMER",
     "Любитель романов — высокие оценки книгам 20-23 (романы)",         "romance"),
    ("moderator",       "moderator@bookstore.test",  "moderator123", "MODERATOR",
     "Модератор платформы — управление заказами и контентом",            None),
    ("admin",           "admin@bookstore.test",      "admin123", "ADMIN",
     "Администратор — полный доступ к системе",                          None),
]

COMMENTS_POS = [
    "Одна из лучших книг, что я читал(а)!",
    "Шедевр мировой литературы. Рекомендую всем.",
    "Не мог(ла) оторваться — прочитал(а) за два дня.",
    "Очень глубокое и интересное произведение.",
    "Потрясающе написано, полностью захватывает.",
    "Перечитывал(а) несколько раз — каждый раз открываю что-то новое.",
    "Классика, которую должен прочитать каждый.",
    "Невероятная книга! Давно так не переживал(а) за героев.",
    "Блестящий стиль и захватывающий сюжет.",
    "Одно из лучших произведений в своём жанре.",
]

COMMENTS_MID = [
    "Неплохая книга, но местами затянуто.",
    "Интересно, но ожидал(а) большего.",
    "Читается нормально, но не шедевр.",
    "Хорошее произведение, но не для всех.",
    "Есть интересные моменты, но в целом — на любителя.",
    "Неоднозначное впечатление: начало хорошее, конец слабее.",
    "Средненько. Для знакомства с автором подойдёт.",
]

COMMENTS_NEG = [
    "Не моё. Не смог(ла) дочитать до конца.",
    "Скучновато, не захватывает совсем.",
    "Переоценённая книга, на мой взгляд.",
    "Слишком медленное повествование.",
]


def clamp(v: int, lo: int = 1, hi: int = 5) -> int:
    return max(lo, min(hi, v))


def pick_comment(rating: int, rng: random.Random) -> str | None:
    if rng.random() > 0.6:
        return None
    if rating >= 5:
        return rng.choice(COMMENTS_POS)
    elif rating >= 3:
        return rng.choice(COMMENTS_MID)
    else:
        return rng.choice(COMMENTS_NEG)


def make_demo_reviews(demo_uid: int, group: str | None, book_ids: list, rng: random.Random) -> list:
    if group is None:
        return []
    rows = []
    n = len(book_ids)

    def rate(rng, book_idxs, base, var=1):
        for idx in book_idxs:
            if idx < n:
                r = clamp(base + rng.randint(-var, var))
                rows.append((demo_uid, book_ids[idx], r))

    if group == "classics":
        rate(rng, range(0, 8),  5, 0)
        rate(rng, range(8, 14), 3, 1)
        rate(rng, range(14, 20), 2, 1)
        rate(rng, range(24, 30), 2, 1)
    elif group == "fantasy":
        rate(rng, range(0, 8),  4, 1)
        rate(rng, range(8, 14), 5, 0)
        rate(rng, range(11, 14), 5, 0)
        rate(rng, range(14, 20), 3, 1)
    elif group == "detective":
        rate(rng, range(0, 8),  3, 1)
        rate(rng, range(8, 14), 3, 1)
        rate(rng, range(14, 20), 5, 0)
        rate(rng, range(20, 24), 3, 1)
    elif group == "scifi":
        rate(rng, range(0, 8),  3, 1)
        rate(rng, range(8, 14), 4, 1)
        rate(rng, range(14, 20), 3, 1)
        rate(rng, range(24, 30), 5, 0)
    elif group == "romance":
        rate(rng, range(0, 8),  4, 1)
        rate(rng, range(14, 20), 3, 1)
        rate(rng, range(20, 24), 5, 0)
        rate(rng, range(24, 30), 3, 1)
    elif group == "universal":
        for idx in range(n):
            if rng.random() > 0.15:
                rows.append((demo_uid, book_ids[idx], clamp(rng.randint(3, 5))))
    return rows


def make_bulk_reviews(user_ids: list, book_ids: list) -> list:
    rng = random.Random(42)
    n = len(book_ids)

    def rate(u_idx, book_range, base, var=1):
        rows = []
        for b_idx in book_range:
            if b_idx < n:
                r = clamp(base + rng.randint(-var, var))
                rows.append((user_ids[u_idx], book_ids[b_idx], r))
        return rows

    rows = []

    for u in range(0, 10):
        rows += rate(u, range(0, 8),  5, 0)
        rows += rate(u, range(8, 14), 3, 1)
        rows += rate(u, range(14, 20), 2, 1)
        rows += rate(u, range(24, 30), 2, 1)

    for u in range(10, 20):
        rows += rate(u, range(0, 8),  5, 0)
        rows += rate(u, range(8, 14), 5, 0)
        rows += rate(u, range(11, 14), 5, 0)
        rows += rate(u, range(14, 20), 3, 1)
        rows += rate(u, range(20, 24), 3, 1)

    for u in range(20, 30):
        rows += rate(u, range(0, 8),  3, 1)
        rows += rate(u, range(8, 14), 4, 1)
        rows += rate(u, range(14, 20), 5, 0)
        rows += rate(u, range(20, 24), 3, 1)

    for u in range(30, 40):
        rows += rate(u, range(0, 8),  3, 1)
        rows += rate(u, range(14, 20), 4, 1)
        rows += rate(u, range(20, 24), 5, 0)
        rows += rate(u, range(24, 30), 5, 0)

    for u in range(40, 50):
        for b_idx in range(n):
            if rng.random() > 0.20:
                rows.append((user_ids[u], book_ids[b_idx], clamp(rng.randint(3, 5))))

    for u in range(50, 60):
        fav = rng.choice([range(0,8), range(8,14), range(14,20), range(20,24), range(24,30)])
        rows += rate(u, fav, 5, 0)
        for b_idx in range(n):
            if rng.random() > 0.65:
                rows.append((user_ids[u], book_ids[b_idx], clamp(rng.randint(2, 5))))

    seen: set = set()
    unique = []
    for uid, bid, r in rows:
        key = (uid, bid)
        if key not in seen:
            seen.add(key)
            unique.append((uid, bid, r))
    return unique


def make_extra_publishers(target_count: int = 50) -> list:
    rows = list(PUBLISHERS)
    for i in range(len(rows) + 1, target_count + 1):
        prefix = PUBLISHER_PREFIXES[i % len(PUBLISHER_PREFIXES)]
        rows.append((f"{prefix} {i}", "Россия", f"https://publisher{i}.bookstore.test"))
    return rows


def make_extra_authors(target_count: int = 250) -> list:
    rows = list(AUTHORS)
    for i in range(len(rows) + 1, target_count + 1):
        first = FIRST_NAMES[i % len(FIRST_NAMES)]
        last = LAST_NAMES[(i * 7) % len(LAST_NAMES)]
        rows.append((f"{first} {last}", f"Автор современной прозы и жанровой литературы, запись {i}", COUNTRIES[i % len(COUNTRIES)]))
    return rows


def make_extra_books(target_count: int = 1000) -> list:
    rows = list(BOOKS_DATA)
    used_isbn = {row[1] for row in rows}
    rng = random.Random(2026)
    category_ids = [row[0] for row in CATEGORIES]
    while len(rows) < target_count:
        i = len(rows) + 1
        adjective = TITLE_ADJECTIVES[i % len(TITLE_ADJECTIVES)]
        noun = TITLE_NOUNS[(i * 5) % len(TITLE_NOUNS)]
        suffix = TITLE_SUFFIXES[(i * 3) % len(TITLE_SUFFIXES)]
        title = f"{adjective.capitalize()} {noun} {suffix}"
        isbn = f"978-5-99-{i:06d}-{i % 10}"
        if isbn in used_isbn:
            continue
        used_isbn.add(isbn)
        cat_main = category_ids[i % len(category_ids)]
        cat_extra = category_ids[(i * 3) % len(category_ids)]
        cat_ids = [cat_main] if cat_main == cat_extra else [cat_main, cat_extra]
        author_idx = i % 250
        rows.append((
            title,
            isbn,
            i % 50,
            float(250 + (i * 37) % 950),
            f"Реалистичное описание книги «{title}»: сюжет, герои и основные темы произведения.",
            120 + (i * 17) % 780,
            f"{1950 + (i % 74)}-01-01",
            cat_ids,
            [author_idx],
        ))
    rng.shuffle(rows[30:])
    return rows


def main() -> None:
    print("=" * 60)
    print("  BookStore — загрузка данных")
    print("=" * 60)
    print()
    print("Подключение к БД...")
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        conn.autocommit = False
        cur = conn.cursor()
    except Exception as exc:
        print(f"Ошибка подключения: {exc}")
        sys.exit(1)

    try:
        print("Очистка таблиц...")
        cur.execute("""
            TRUNCATE TABLE
                recommendations, collection_books, collections,
                wishlist, reviews, order_items, orders,
                book_authors, book_categories, books,
                authors, categories, publishers, users
            RESTART IDENTITY CASCADE
        """)

        print("Создание издателей...")
        publishers = make_extra_publishers(50)
        cur.executemany(
            "INSERT INTO publishers (name, country, website) VALUES (%s, %s, %s)",
            publishers,
        )
        cur.execute("SELECT publisher_id FROM publishers ORDER BY publisher_id")
        pub_ids = [r[0] for r in cur.fetchall()]

        print("Создание категорий (ID 13–24)...")
        for cat_id, name, desc in CATEGORIES:
            cur.execute(
                "INSERT INTO categories (category_id, name, description)"
                " OVERRIDING SYSTEM VALUE VALUES (%s, %s, %s)",
                (cat_id, name, desc),
            )
        cur.execute("SELECT setval('categories_category_id_seq', 24)")

        print("Создание авторов...")
        authors = make_extra_authors(250)
        cur.executemany(
            "INSERT INTO authors (name, biography, country) VALUES (%s, %s, %s)",
            authors,
        )
        cur.execute("SELECT author_id FROM authors ORDER BY author_id")
        author_ids = [r[0] for r in cur.fetchall()]

        print("Создание книг...")
        books_data = make_extra_books(1000)
        book_isbns = []
        for title, isbn, pub_idx, price, desc, pages, pub_date, cat_ids, auth_idxs in books_data:
            cur.execute(
                """INSERT INTO books
                       (title, isbn, publisher_id, price, description, pages,
                        publication_date, avg_rating, rating_count, created_at)
                   VALUES (%s, %s, %s, %s, %s, %s, %s, 0.0, 0, NOW())
                   RETURNING isbn""",
                (title, isbn, pub_ids[pub_idx], price, desc, pages, pub_date),
            )
            book_isbn = cur.fetchone()[0]
            book_isbns.append(book_isbn)
            for cat_id in cat_ids:
                cur.execute("INSERT INTO book_categories (book_isbn, category_id) VALUES (%s, %s)",
                            (book_isbn, cat_id))
            for auth_idx in auth_idxs:
                cur.execute("INSERT INTO book_authors (book_isbn, author_id) VALUES (%s, %s)",
                            (book_isbn, author_ids[auth_idx]))
        print(f"  Создано книг: {len(book_isbns)}")

        print("Создание специальных аккаунтов...")
        rng_demo = random.Random(99)
        demo_uids = {}
        demo_group = {}

        for username, email, password, role, desc, group in DEMO_USERS:
            pw = hash_pw(password)
            cur.execute(
                "INSERT INTO users (username, email, password_hash, role, created_at)"
                " VALUES (%s, %s, %s, %s, NOW()) RETURNING user_id",
                (username, email, pw, role),
            )
            uid = cur.fetchone()[0]
            demo_uids[username] = uid
            if group:
                demo_group[uid] = group

        print(f"  Создано специальных аккаунтов: {len(DEMO_USERS)}")

        print("Создание обычных пользователей...")
        pw_hash = hash_pw("password123")
        bulk_user_ids = []
        for i in range(1, 1001):
            cur.execute(
                "INSERT INTO users (username, email, password_hash, role, created_at)"
                " VALUES (%s, %s, %s, 'CUSTOMER', NOW()) RETURNING user_id",
                (f"reader{i}", f"reader{i}@bookstore.test", pw_hash),
            )
            bulk_user_ids.append(cur.fetchone()[0])
        print(f"  Создано обычных пользователей: {len(bulk_user_ids)}")

        print("Создание отзывов для демо-аккаунтов...")
        demo_reviews = []
        for uid, group in demo_group.items():
            demo_reviews += make_demo_reviews(uid, group, book_isbns, rng_demo)

        rng_ts = random.Random(7)
        inserted_reviews = 0

        for uid, bid, rating in demo_reviews:
            comment = pick_comment(rating, rng_ts)
            created_at = datetime.now() - timedelta(days=rng_ts.randint(0, 180))
            cur.execute(
                "INSERT INTO reviews (user_id, book_isbn, rating, comment, created_at)"
                " VALUES (%s, %s, %s, %s, %s)"
                " ON CONFLICT (user_id, book_isbn) DO NOTHING",
                (uid, bid, rating, comment, created_at),
            )
            inserted_reviews += 1

        print("Создание отзывов для обычных пользователей...")
        bulk_reviews = make_bulk_reviews(bulk_user_ids, book_isbns)
        rng_bulk = random.Random(12345)
        for uid in bulk_user_ids[60:]:
            preferred_start = rng_bulk.choice([0, 8, 14, 20, 24, 100, 250, 500, 750])
            for bid in rng_bulk.sample(book_isbns[max(0, preferred_start):min(len(book_isbns), preferred_start + 80)], 8):
                bulk_reviews.append((uid, bid, clamp(rng_bulk.randint(3, 5))))
            for bid in rng_bulk.sample(book_isbns, 4):
                bulk_reviews.append((uid, bid, clamp(rng_bulk.randint(1, 5))))

        for uid, bid, rating in bulk_reviews:
            comment = pick_comment(rating, rng_ts)
            created_at = datetime.now() - timedelta(days=rng_ts.randint(0, 365))
            cur.execute(
                "INSERT INTO reviews (user_id, book_isbn, rating, comment, created_at)"
                " VALUES (%s, %s, %s, %s, %s)"
                " ON CONFLICT (user_id, book_isbn) DO NOTHING",
                (uid, bid, rating, comment, created_at),
            )
            inserted_reviews += 1

        print(f"  Создано отзывов: {inserted_reviews}")

        print("Пересчёт avg_rating и rating_count для книг...")
        cur.execute("""
            UPDATE books b
            SET avg_rating   = COALESCE(sub.avg_r, 0.0),
                rating_count = COALESCE(sub.cnt,   0)
            FROM (
                SELECT book_isbn,
                       ROUND(AVG(rating)::NUMERIC, 2)::FLOAT AS avg_r,
                       COUNT(*) AS cnt
                FROM reviews
                GROUP BY book_isbn
            ) sub
            WHERE b.isbn = sub.book_isbn
        """)

        print("Создание тестовых заказов, списков желаемого и подборок...")
        order_statuses = ['PENDING', 'CONFIRMED', 'DELIVERED', 'DELIVERED', 'CANCELLED']
        order_count = 0
        rng_orders = random.Random(31415)
        order_user_pool = list(demo_uids.values())[:6] + bulk_user_ids
        for _ in range(1200):
            uid = rng_orders.choice(order_user_pool)
            status = rng_orders.choice(order_statuses)
            book_idx = rng_orders.randint(0, len(book_isbns) - 1)
            bid = book_isbns[book_idx]
            cur.execute(
                "SELECT price FROM books WHERE isbn = %s", (bid,)
            )
            price = cur.fetchone()[0]
            quantity = rng_orders.randint(1, 3)
            total = float(price) * quantity
            cur.execute(
                "INSERT INTO orders (user_id, total_amount, status, created_at) VALUES (%s, %s, %s, %s) RETURNING order_id",
                (uid, total, status, datetime.now() - timedelta(days=rng_orders.randint(0, 180)))
            )
            order_id = cur.fetchone()[0]
            cur.execute(
                "INSERT INTO order_items (order_id, book_isbn, quantity, price) VALUES (%s, %s, %s, %s)",
                (order_id, bid, quantity, price)
            )
            order_count += 1

        print(f"  Создано заказов: {order_count}")

        wishlist_seen = set()
        wishlist_count = 0
        for uid in order_user_pool:
            for bid in rng_orders.sample(book_isbns, 2):
                key = (uid, bid)
                if key in wishlist_seen:
                    continue
                wishlist_seen.add(key)
                cur.execute(
                    "INSERT INTO wishlist (user_id, book_isbn, added_at) VALUES (%s, %s, %s) ON CONFLICT DO NOTHING",
                    (uid, bid, datetime.now() - timedelta(days=rng_orders.randint(0, 120)))
                )
                wishlist_count += 1
                if wishlist_count >= 1100:
                    break
            if wishlist_count >= 1100:
                break

        collection_count = 0
        collection_books_count = 0
        collection_names = ["Любимое", "На лето", "Классика", "Фантастика", "Для подарка", "Хочу прочитать"]
        for uid in order_user_pool[:1000]:
            cur.execute(
                "INSERT INTO collections (owner_user_id, name, description, created_at) VALUES (%s, %s, %s, %s) RETURNING collection_id",
                (
                    uid,
                    rng_orders.choice(collection_names),
                    "Пользовательская подборка книг",
                    datetime.now() - timedelta(days=rng_orders.randint(0, 200)),
                )
            )
            collection_id = cur.fetchone()[0]
            collection_count += 1
            for bid in rng_orders.sample(book_isbns, 2):
                cur.execute(
                    "INSERT INTO collection_books (collection_id, book_isbn, added_at) VALUES (%s, %s, %s) ON CONFLICT DO NOTHING",
                    (collection_id, bid, datetime.now() - timedelta(days=rng_orders.randint(0, 200)))
                )
                collection_books_count += 1

        recommendation_count = 0
        for uid in list(demo_uids.values())[:6] + bulk_user_ids[:200]:
            cur.execute("SELECT calculate_recommendations(%s)", (uid,))
            recommendation_count += cur.fetchone()[0]

        print(f"  Записей wishlist: {wishlist_count}")
        print(f"  Подборок: {collection_count}")
        print(f"  Книг в подборках: {collection_books_count}")
        print(f"  Рекомендаций: {recommendation_count}")

        conn.commit()

        cur.execute("SELECT COUNT(*) FROM reviews")
        total_reviews = cur.fetchone()[0]

        print()
        print("=" * 60)
        print("  Данные успешно загружены!")
        print("=" * 60)
        print(f"  Издателей:    {len(pub_ids)}")
        print(f"  Категорий:    {len(CATEGORIES)}  (ID 13–24)")
        print(f"  Авторов:      {len(author_ids)}")
        print(f"  Книг:         {len(book_isbns)}")
        print(f"  Пользователей: {len(DEMO_USERS) + len(bulk_user_ids)}")
        print(f"  Отзывов:      {total_reviews}")
        print(f"  Заказов:      {order_count}")
        print()
        print("─" * 60)
        print("  ДЕМО-АККАУНТЫ")
        print("─" * 60)
        rows_table = [
            ("Аккаунт",       "Пароль",       "Роль",     "Описание"),
            ("─" * 16,        "─" * 13,       "─" * 9,    "─" * 30),
            ("classics_fan",  "demo123",      "CUSTOMER", "Любитель классики"),
            ("fantasy_lover", "demo123",      "CUSTOMER", "Фанат фэнтези"),
            ("detective_fan", "demo123",      "CUSTOMER", "Любитель детективов"),
            ("scifi_reader",  "demo123",      "CUSTOMER", "Читатель НФ"),
            ("romance_reader","demo123",      "CUSTOMER", "Любитель романов"),
            ("universal_reader","demo123",    "CUSTOMER", "Универсал-читатель"),
            ("─" * 16,        "─" * 13,       "─" * 9,    "─" * 30),
            ("moderator",     "moderator123", "MODERATOR","Управление заказами и модерация"),
            ("admin",         "admin123",     "ADMIN",    "Полный доступ к системе"),
            ("─" * 16,        "─" * 13,       "─" * 9,    "─" * 30),
            ("reader1..60",   "password123",  "CUSTOMER", "Обычные читатели (60 штук)"),
        ]
        for row in rows_table:
            print(f"  {row[0]:<18} {row[1]:<14} {row[2]:<10} {row[3]}")
        print()
        print("  Рекомендации активированы для всех демо-аккаунтов!")
        print("=" * 60)

    except Exception as exc:
        conn.rollback()
        print(f"\nОшибка: {exc}")
        import traceback; traceback.print_exc()
        sys.exit(1)
    finally:
        cur.close()
        conn.close()


if __name__ == "__main__":
    main()
