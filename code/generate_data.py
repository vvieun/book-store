#!/usr/bin/env python3
import sys 
import random 
from datetime import datetime ,timedelta 

try :
    import psycopg2 
except ImportError :
    print ("ERROR: psycopg2 not found. Run: pip install psycopg2-binary")
    sys .exit (1 )

try :
    import bcrypt 
    def hash_pw (p :str )->str :
        return bcrypt .hashpw (p .encode (),bcrypt .gensalt (rounds =10 )).decode ()
except ImportError :
    print ("WARNING: bcrypt not found. Using static fallback hash.")

    def hash_pw (_ :str )->str :
        return "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

DB_CONFIG ={
"host":"localhost",
"port":5433 ,
"database":"bookstore_db",
"user":"postgres",
"password":"postgres",
}


PUBLISHERS =[
("Эксмо","Россия","https://eksmo.ru"),
("АСТ","Россия","https://ast.ru"),
("Азбука-Аттикус","Россия","https://atticus-group.ru"),
("МИФ","Россия","https://mann-ivanov-ferber.ru"),
("Питер","Россия","https://piter.com"),
]

CATEGORIES =[
(13 ,"Классическая литература","Произведения мировой классики"),
(14 ,"Фантастика","Фэнтези, мистика и волшебство"),
(15 ,"Детективы","Детективные романы и триллеры"),
(16 ,"Романы","Любовные и социальные романы"),
(17 ,"Исторические книги","Исторические романы и документальная проза"),
(18 ,"Научная фантастика","Научно-фантастические произведения"),
(19 ,"Приключения","Приключенческая литература"),
(20 ,"Биографии","Биографии и мемуары"),
(21 ,"Поэзия","Поэтические сборники"),
(22 ,"Философия","Философские труды"),
(23 ,"Психология","Психология и саморазвитие"),
(24 ,"Бизнес и экономика","Деловая литература"),
]

AUTHORS =[
("Лев Толстой","Великий русский писатель-романист","Россия"),
("Фёдор Достоевский","Русский писатель-психолог","Россия"),
("Антон Чехов","Мастер короткого рассказа","Россия"),
("Михаил Булгаков","Автор Мастера и Маргариты","Россия"),
("Александр Пушкин","Основоположник русской литературы","Россия"),
("Иван Тургенев","Русский писатель-реалист","Россия"),
("Николай Гоголь","Автор Мёртвых душ","Россия"),
("Артур Конан Дойл","Создатель Шерлока Холмса","Великобритания"),
("Агата Кристи","Королева детектива","Великобритания"),
("Жюль Верн","Пионер научной фантастики","Франция"),
("Герберт Уэллс","Классик научной фантастики","Великобритания"),
("Рэй Брэдбери","Американский фантаст","США"),
("Джек Лондон","Автор приключенческих романов","США"),
("Эрнест Хемингуэй","Нобелевский лауреат по литературе","США"),
("Оноре де Бальзак","Мастер реалистического романа","Франция"),
]


BOOKS_DATA =[

("Война и мир","978-5-04-089536-1",0 ,890.0 ,
"Эпопея о жизни русского общества в эпоху наполеоновских войн — грандиозный роман о судьбах людей",
1274 ,"1869-01-01",[13 ],[0 ]),
("Преступление и наказание","978-5-04-089537-2",0 ,650.0 ,
"Психологический роман о студенте Раскольникове, совершившем преступление и мучительно ищущем искупление",
592 ,"1866-01-01",[13 ],[1 ]),
("Анна Каренина","978-5-04-089538-3",1 ,750.0 ,
"Роман о трагической судьбе светской женщины, разрывающейся между долгом и страстью",
864 ,"1878-01-01",[13 ,16 ],[0 ]),
("Мастер и Маргарита","978-5-04-089539-4",2 ,580.0 ,
"Загадочный роман о визите Воланда в советскую Москву, переплетающий мистику и сатиру",
480 ,"1967-01-01",[13 ,14 ],[3 ]),
("Евгений Онегин","978-5-04-089540-5",1 ,420.0 ,
"Роман в стихах о пресыщённом жизнью молодом человеке, упустившем своё счастье",
320 ,"1833-01-01",[13 ,21 ],[4 ]),
("Отцы и дети","978-5-04-089541-6",2 ,380.0 ,
"Роман о конфликте поколений в России — нигилист Базаров против дворянского уклада",
320 ,"1862-01-01",[13 ],[5 ]),
("Мёртвые души","978-5-04-089542-7",1 ,490.0 ,
"Поэма о похождениях предприимчивого Чичикова, скупающего умерших крепостных",
400 ,"1842-01-01",[13 ],[6 ]),
("Братья Карамазовы","978-5-04-089543-8",0 ,780.0 ,
"Философский роман о природе добра и зла через историю трёх братьев и убийства отца",
896 ,"1880-01-01",[13 ,22 ],[1 ]),


("Ночной Дозор","978-5-04-100001-1",0 ,520.0 ,
"Мистический роман о противостоянии светлых и тёмных Иных в современной Москве",
480 ,"1998-01-01",[14 ],[3 ]),
("Пикник на обочине","978-5-04-100002-2",1 ,440.0 ,
"Роман о зоне — загадочном месте после визита инопланетян, где сталкеры ищут артефакты",
224 ,"1972-01-01",[14 ,18 ],[3 ]),
("Хроники Амбера","978-5-04-100004-4",3 ,680.0 ,
"Фэнтезийный цикл о многомировой монархии, где принцы сражаются за трон истинного мира",
640 ,"1970-01-01",[14 ,19 ],[3 ]),
("Властелин колец","978-5-04-100005-5",0 ,890.0 ,
"Эпическое фэнтези о походе Братства Кольца — одно из величайших произведений XX века",
1216 ,"1954-01-01",[14 ,19 ],[3 ]),
("Гарри Поттер и философский камень","978-5-389-07985-0",2 ,590.0 ,
"История о мальчике-волшебнике, узнающем о своём предназначении в школе Хогвартс",
432 ,"1997-01-01",[14 ,19 ],[3 ]),
("Дюна","978-5-04-100006-6",1 ,720.0 ,
"Эпическая сага о пустынной планете Арракис и мальчике, ставшем пророком",
688 ,"1965-01-01",[14 ,18 ],[3 ]),


("Этюд в багровых тонах","978-5-04-200001-1",1 ,480.0 ,
"Первое знакомство с великим детективом Шерлоком Холмсом и его методами дедукции",
256 ,"1887-01-01",[15 ],[7 ]),
("Убийство в Восточном экспрессе","978-5-04-200002-2",0 ,520.0 ,
"Пуаро расследует убийство в заснеженном поезде, где каждый пассажир — под подозрением",
288 ,"1934-01-01",[15 ],[8 ]),
("Десять негритят","978-5-04-200003-3",2 ,450.0 ,
"Десять незнакомцев оказываются на острове, откуда нет выхода, и начинают погибать один за другим",
224 ,"1939-01-01",[15 ],[8 ]),
("Собака Баскервилей","978-5-04-200004-4",1 ,420.0 ,
"Холмс и Ватсон распутывают тайну проклятия рода Баскервилей на болотах Дартмура",
240 ,"1902-01-01",[15 ,19 ],[7 ]),
("Восточный детектив","978-5-04-200005-5",0 ,490.0 ,
"Классический детектив в экзотических восточных декорациях от мастера жанра",
272 ,"1932-01-01",[15 ],[8 ]),
("Свидетель обвинения","978-5-04-200006-6",3 ,380.0 ,
"Судебный детектив с неожиданным поворотом — один из лучших рассказов Агаты Кристи",
320 ,"1953-01-01",[15 ],[8 ]),


("Три товарища","978-5-04-300003-3",2 ,490.0 ,
"Роман о дружбе и любви трёх ветеранов Первой мировой войны в послевоенной Германии",
448 ,"1936-01-01",[16 ],[13 ]),
("Прощай, оружие!","978-5-04-300004-4",1 ,460.0 ,
"Антивоенный роман Хемингуэя о любви американского офицера и британской медсестры",
352 ,"1929-01-01",[16 ,17 ],[13 ]),
("Старик и море","978-5-04-300005-5",3 ,320.0 ,
"Притча о старом кубинском рыбаке и его борьбе с гигантским марлином в океане",
128 ,"1952-01-01",[16 ],[13 ]),
("Дама с камелиями","978-5-04-300007-7",1 ,390.0 ,
"Роман о трагической любви куртизанки Маргариты Готье и молодого Армана Дюваля",
256 ,"1848-01-01",[16 ],[14 ]),


("451 градус по Фаренгейту","978-5-04-400001-1",0 ,450.0 ,
"Антиутопия о мире будущего, где книги запрещены, а пожарники их сжигают",
256 ,"1953-01-01",[18 ],[11 ]),
("Машина времени","978-5-04-400002-2",1 ,380.0 ,
"Путешествие в далёкое будущее, где человечество разделилось на два биологических вида",
192 ,"1895-01-01",[18 ,19 ],[10 ]),
("Война миров","978-5-04-400003-3",2 ,390.0 ,
"Вторжение марсиан на Землю — один из первых романов о контакте с внеземными цивилизациями",
224 ,"1898-01-01",[18 ],[10 ]),
("Путешествие к центру Земли","978-5-04-400004-4",1 ,420.0 ,
"Захватывающая подземная экспедиция профессора Лиденброка через жерло вулкана",
256 ,"1864-01-01",[18 ,19 ],[9 ]),
("20000 лье под водой","978-5-04-400005-5",0 ,490.0 ,
"Путешествие на борту подводной лодки Наутилус таинственного капитана Немо",
448 ,"1870-01-01",[18 ,19 ],[9 ]),
("Солярис","978-5-04-400006-6",2 ,510.0 ,
"Философский роман о контакте с разумным океаном планеты Солярис и природе познания",
304 ,"1961-01-01",[18 ,22 ],[3 ]),
]


DEMO_USERS =[

("classics_fan","classics@bookstore.test","demo123","CUSTOMER",
"Любитель классики — высокие оценки книгам 0-7 (классика)","classics"),
("fantasy_lover","fantasy@bookstore.test","demo123","CUSTOMER",
"Фанат фэнтези — высокие оценки книгам 8-13 (фантастика)","fantasy"),
("detective_fan","detective@bookstore.test","demo123","CUSTOMER",
"Любитель детективов — высокие оценки книгам 14-19 (детективы)","detective"),
("scifi_reader","scifi@bookstore.test","demo123","CUSTOMER",
"Читатель НФ — высокие оценки книгам 24-29 (научная фантастика)","scifi"),
("universal_reader","universal@bookstore.test","demo123","CUSTOMER",
"Универсал — оценивает всё подряд, средние-высокие оценки","universal"),
("romance_reader","romance@bookstore.test","demo123","CUSTOMER",
"Любитель романов — высокие оценки книгам 20-23 (романы)","romance"),
("moderator","moderator@bookstore.test","moderator123","MODERATOR",
"Модератор платформы — управление заказами и контентом",None ),
("admin","admin@bookstore.test","admin123","ADMIN",
"Администратор — полный доступ к системе",None ),
]


COMMENTS_POS =[
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

COMMENTS_MID =[
"Неплохая книга, но местами затянуто.",
"Интересно, но ожидал(а) большего.",
"Читается нормально, но не шедевр.",
"Хорошее произведение, но не для всех.",
"Есть интересные моменты, но в целом — на любителя.",
"Неоднозначное впечатление: начало хорошее, конец слабее.",
"Средненько. Для знакомства с автором подойдёт.",
]

COMMENTS_NEG =[
"Не моё. Не смог(ла) дочитать до конца.",
"Скучновато, не захватывает совсем.",
"Переоценённая книга, на мой взгляд.",
"Слишком медленное повествование.",
]


def clamp (v :int ,lo :int =1 ,hi :int =5 )->int :
    return max (lo ,min (hi ,v ))


def pick_comment (rating :int ,rng :random .Random )->str |None :
    """Случайно добавляем комментарий (~60% вероятность)."""
    if rng .random ()>0.6 :
        return None 
    if rating >=5 :
        return rng .choice (COMMENTS_POS )
    elif rating >=3 :
        return rng .choice (COMMENTS_MID )
    else :
        return rng .choice (COMMENTS_NEG )


def make_demo_reviews (demo_uid :int ,group :str |None ,book_ids :list ,rng :random .Random )->list :
    """Генерирует отзывы для демо-аккаунта в зависимости от группы."""
    if group is None :
        return []
    rows =[]
    n =len (book_ids )

    def rate (rng ,book_idxs ,base ,var =1 ):
        for idx in book_idxs :
            if idx <n :
                r =clamp (base +rng .randint (-var ,var ))
                rows .append ((demo_uid ,book_ids [idx ],r ))

    if group =="classics":
        rate (rng ,range (0 ,8 ),5 ,0 )
        rate (rng ,range (8 ,14 ),3 ,1 )
        rate (rng ,range (14 ,20 ),2 ,1 )
        rate (rng ,range (24 ,30 ),2 ,1 )
    elif group =="fantasy":
        rate (rng ,range (0 ,8 ),4 ,1 )
        rate (rng ,range (8 ,14 ),5 ,0 )
        rate (rng ,range (11 ,14 ),5 ,0 )
        rate (rng ,range (14 ,20 ),3 ,1 )
    elif group =="detective":
        rate (rng ,range (0 ,8 ),3 ,1 )
        rate (rng ,range (8 ,14 ),3 ,1 )
        rate (rng ,range (14 ,20 ),5 ,0 )
        rate (rng ,range (20 ,24 ),3 ,1 )
    elif group =="scifi":
        rate (rng ,range (0 ,8 ),3 ,1 )
        rate (rng ,range (8 ,14 ),4 ,1 )
        rate (rng ,range (14 ,20 ),3 ,1 )
        rate (rng ,range (24 ,30 ),5 ,0 )
    elif group =="romance":
        rate (rng ,range (0 ,8 ),4 ,1 )
        rate (rng ,range (14 ,20 ),3 ,1 )
        rate (rng ,range (20 ,24 ),5 ,0 )
        rate (rng ,range (24 ,30 ),3 ,1 )
    elif group =="universal":
        for idx in range (n ):
            if rng .random ()>0.15 :
                rows .append ((demo_uid ,book_ids [idx ],clamp (rng .randint (3 ,5 ))))
    return rows 


def make_bulk_reviews (user_ids :list ,book_ids :list )->list :
    """
    Создаёт большой массив отзывов для 60 обычных пользователей.
    Группы (CF-friendly, корреляция Пирсона > 0.3):
      A (u 0-9):   Любители классики → книги 0-7 высоко, 8-13 средне
      B (u 10-19): Фанаты фэнтези+классика → 0-7 высоко, 8-13 высоко
      C (u 20-29): Детективщики → 8-13 средне, 14-19 высоко
      D (u 30-39): Романтики+НФ → 14-19 средне, 20-23 высоко, 24-29 высоко
      E (u 40-49): Универсалы → все книги средне-высоко
      F (u 50-59): Смешанный вкус → случайные паттерны
    """
    rng =random .Random (42 )
    n =len (book_ids )

    def rate (u_idx ,book_range ,base ,var =1 ):
        rows =[]
        for b_idx in book_range :
            if b_idx <n :
                r =clamp (base +rng .randint (-var ,var ))
                rows .append ((user_ids [u_idx ],book_ids [b_idx ],r ))
        return rows 

    rows =[]

    for u in range (0 ,10 ):
        rows +=rate (u ,range (0 ,8 ),5 ,0 )
        rows +=rate (u ,range (8 ,14 ),3 ,1 )
        rows +=rate (u ,range (14 ,20 ),2 ,1 )
        rows +=rate (u ,range (24 ,30 ),2 ,1 )

    for u in range (10 ,20 ):
        rows +=rate (u ,range (0 ,8 ),5 ,0 )
        rows +=rate (u ,range (8 ,14 ),5 ,0 )
        rows +=rate (u ,range (11 ,14 ),5 ,0 )
        rows +=rate (u ,range (14 ,20 ),3 ,1 )
        rows +=rate (u ,range (20 ,24 ),3 ,1 )

    for u in range (20 ,30 ):
        rows +=rate (u ,range (0 ,8 ),3 ,1 )
        rows +=rate (u ,range (8 ,14 ),4 ,1 )
        rows +=rate (u ,range (14 ,20 ),5 ,0 )
        rows +=rate (u ,range (20 ,24 ),3 ,1 )

    for u in range (30 ,40 ):
        rows +=rate (u ,range (0 ,8 ),3 ,1 )
        rows +=rate (u ,range (14 ,20 ),4 ,1 )
        rows +=rate (u ,range (20 ,24 ),5 ,0 )
        rows +=rate (u ,range (24 ,30 ),5 ,0 )

    for u in range (40 ,50 ):
        for b_idx in range (n ):
            if rng .random ()>0.20 :
                rows .append ((user_ids [u ],book_ids [b_idx ],clamp (rng .randint (3 ,5 ))))

    for u in range (50 ,60 ):

        fav =rng .choice ([range (0 ,8 ),range (8 ,14 ),range (14 ,20 ),range (20 ,24 ),range (24 ,30 )])
        rows +=rate (u ,fav ,5 ,0 )

        for b_idx in range (n ):
            if rng .random ()>0.65 :
                rows .append ((user_ids [u ],book_ids [b_idx ],clamp (rng .randint (2 ,5 ))))

    seen :set =set ()
    unique =[]
    for uid ,bid ,r in rows :
        key =(uid ,bid )
        if key not in seen :
            seen .add (key )
            unique .append ((uid ,bid ,r ))
    return unique 


def main ()->None :
    print ("="*60 )
    print ("  BookStore — загрузка данных")
    print ("="*60 )
    print ()
    print ("Подключение к БД...")
    try :
        conn =psycopg2 .connect (**DB_CONFIG )
        conn .autocommit =False 
        cur =conn .cursor ()
    except Exception as exc :
        print (f"Ошибка подключения: {exc }")
        sys .exit (1 )

    try :
        print ("Очистка таблиц...")
        cur .execute ("""
            TRUNCATE TABLE
                reviews, order_items, orders, reading_history,
                wishlist, book_authors, book_categories, books,
                authors, categories, publishers, users
            RESTART IDENTITY CASCADE
        """)


        print ("Создание издателей...")
        cur .executemany (
        "INSERT INTO publishers (name, country, website) VALUES (%s, %s, %s)",
        PUBLISHERS ,
        )
        cur .execute ("SELECT publisher_id FROM publishers ORDER BY publisher_id")
        pub_ids =[r [0 ]for r in cur .fetchall ()]


        print ("Создание категорий (ID 13–24)...")
        for cat_id ,name ,desc in CATEGORIES :
            cur .execute (
            "INSERT INTO categories (category_id, name, description)"
            " OVERRIDING SYSTEM VALUE VALUES (%s, %s, %s)",
            (cat_id ,name ,desc ),
            )
        cur .execute ("SELECT setval('categories_category_id_seq', 24)")


        print ("Создание авторов...")
        cur .executemany (
        "INSERT INTO authors (name, biography, country) VALUES (%s, %s, %s)",
        AUTHORS ,
        )
        cur .execute ("SELECT author_id FROM authors ORDER BY author_id")
        author_ids =[r [0 ]for r in cur .fetchall ()]


        print ("Создание книг...")
        book_ids =[]
        for title ,isbn ,pub_idx ,price ,desc ,pages ,pub_date ,cat_ids ,auth_idxs in BOOKS_DATA :
            cur .execute (
            """INSERT INTO books
                       (title, isbn, publisher_id, price, description, pages,
                        publication_date, avg_rating, rating_count, created_at)
                   VALUES (%s, %s, %s, %s, %s, %s, %s, 0.0, 0, NOW())
                   RETURNING book_id""",
            (title ,isbn ,pub_ids [pub_idx ],price ,desc ,pages ,pub_date ),
            )
            book_id =cur .fetchone ()[0 ]
            book_ids .append (book_id )
            for cat_id in cat_ids :
                cur .execute ("INSERT INTO book_categories (book_id, category_id) VALUES (%s, %s)",
                (book_id ,cat_id ))
            for auth_idx in auth_idxs :
                cur .execute ("INSERT INTO book_authors (book_id, author_id) VALUES (%s, %s)",
                (book_id ,author_ids [auth_idx ]))
        print (f"  Создано книг: {len (book_ids )}")


        print ("Создание специальных аккаунтов...")
        rng_demo =random .Random (99 )
        demo_uids ={}
        demo_group ={}

        for username ,email ,password ,role ,desc ,group in DEMO_USERS :
            pw =hash_pw (password )
            cur .execute (
            "INSERT INTO users (username, email, password_hash, role, created_at)"
            " VALUES (%s, %s, %s, %s, NOW()) RETURNING user_id",
            (username ,email ,pw ,role ),
            )
            uid =cur .fetchone ()[0 ]
            demo_uids [username ]=uid 
            if group :
                demo_group [uid ]=group 

        print (f"  Создано специальных аккаунтов: {len (DEMO_USERS )}")


        print ("Создание обычных пользователей...")
        pw_hash =hash_pw ("password123")
        bulk_user_ids =[]
        for i in range (1 ,61 ):
            cur .execute (
            "INSERT INTO users (username, email, password_hash, role, created_at)"
            " VALUES (%s, %s, %s, 'CUSTOMER', NOW()) RETURNING user_id",
            (f"reader{i }",f"reader{i }@bookstore.test",pw_hash ),
            )
            bulk_user_ids .append (cur .fetchone ()[0 ])
        print (f"  Создано обычных пользователей: {len (bulk_user_ids )}")


        print ("Создание отзывов для демо-аккаунтов...")
        demo_reviews =[]
        for uid ,group in demo_group .items ():
            demo_reviews +=make_demo_reviews (uid ,group ,book_ids ,rng_demo )

        rng_ts =random .Random (7 )
        inserted_reviews =0 

        for uid ,bid ,rating in demo_reviews :
            comment =pick_comment (rating ,rng_ts )
            created_at =datetime .now ()-timedelta (days =rng_ts .randint (0 ,180 ))
            cur .execute (
            "INSERT INTO reviews (user_id, book_id, rating, comment, created_at)"
            " VALUES (%s, %s, %s, %s, %s)"
            " ON CONFLICT (user_id, book_id) DO NOTHING",
            (uid ,bid ,rating ,comment ,created_at ),
            )
            inserted_reviews +=1 


        print ("Создание отзывов для обычных пользователей...")
        bulk_reviews =make_bulk_reviews (bulk_user_ids ,book_ids )

        for uid ,bid ,rating in bulk_reviews :
            comment =pick_comment (rating ,rng_ts )
            created_at =datetime .now ()-timedelta (days =rng_ts .randint (0 ,365 ))
            cur .execute (
            "INSERT INTO reviews (user_id, book_id, rating, comment, created_at)"
            " VALUES (%s, %s, %s, %s, %s)"
            " ON CONFLICT (user_id, book_id) DO NOTHING",
            (uid ,bid ,rating ,comment ,created_at ),
            )
            inserted_reviews +=1 

        print (f"  Создано отзывов: {inserted_reviews }")


        print ("Пересчёт avg_rating и rating_count для книг...")
        cur .execute ("""
            UPDATE books b
            SET avg_rating   = COALESCE(sub.avg_r, 0.0),
                rating_count = COALESCE(sub.cnt,   0)
            FROM (
                SELECT book_id,
                       ROUND(AVG(rating)::NUMERIC, 2)::FLOAT AS avg_r,
                       COUNT(*) AS cnt
                FROM reviews
                GROUP BY book_id
            ) sub
            WHERE b.book_id = sub.book_id
        """)


        print ("Создание тестовых заказов...")
        order_statuses =['PENDING','PROCESSING','DELIVERED','DELIVERED','CANCELLED']
        order_count =0 
        for uid in list (demo_uids .values ())[:5 ]+bulk_user_ids [:10 ]:
            status =random .choice (order_statuses )
            book_idx =random .randint (0 ,len (book_ids )-1 )
            bid =book_ids [book_idx ]
            cur .execute (
            "SELECT price FROM books WHERE book_id = %s",(bid ,)
            )
            price =cur .fetchone ()[0 ]
            cur .execute (
            "INSERT INTO orders (user_id, total_amount, status, created_at) VALUES (%s, %s, %s, %s) RETURNING order_id",
            (uid ,price ,status ,datetime .now ()-timedelta (days =random .randint (0 ,60 )))
            )
            order_id =cur .fetchone ()[0 ]
            cur .execute (
            "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (%s, %s, 1, %s)",
            (order_id ,bid ,price )
            )
            order_count +=1 

        print (f"  Создано заказов: {order_count }")

        conn .commit ()


        cur .execute ("SELECT COUNT(*) FROM reviews")
        total_reviews =cur .fetchone ()[0 ]

        print ()
        print ("="*60 )
        print ("  Данные успешно загружены!")
        print ("="*60 )
        print (f"  Издателей:    {len (pub_ids )}")
        print (f"  Категорий:    {len (CATEGORIES )}  (ID 13–24)")
        print (f"  Авторов:      {len (author_ids )}")
        print (f"  Книг:         {len (book_ids )}")
        print (f"  Пользователей: {len (DEMO_USERS )+len (bulk_user_ids )}")
        print (f"  Отзывов:      {total_reviews }")
        print (f"  Заказов:      {order_count }")
        print ()
        print ("─"*60 )
        print ("  ДЕМО-АККАУНТЫ")
        print ("─"*60 )
        rows_table =[
        ("Аккаунт","Пароль","Роль","Описание"),
        ("─"*16 ,"─"*13 ,"─"*9 ,"─"*30 ),
        ("classics_fan","demo123","CUSTOMER","Любитель классики"),
        ("fantasy_lover","demo123","CUSTOMER","Фанат фэнтези"),
        ("detective_fan","demo123","CUSTOMER","Любитель детективов"),
        ("scifi_reader","demo123","CUSTOMER","Читатель НФ"),
        ("romance_reader","demo123","CUSTOMER","Любитель романов"),
        ("universal_reader","demo123","CUSTOMER","Универсал-читатель"),
        ("─"*16 ,"─"*13 ,"─"*9 ,"─"*30 ),
        ("moderator","moderator123","MODERATOR","Управление заказами и модерация"),
        ("admin","admin123","ADMIN","Полный доступ к системе"),
        ("─"*16 ,"─"*13 ,"─"*9 ,"─"*30 ),
        ("reader1..60","password123","CUSTOMER","Обычные читатели (60 штук)"),
        ]
        for row in rows_table :
            print (f"  {row [0 ]:<18} {row [1 ]:<14} {row [2 ]:<10} {row [3 ]}")
        print ()
        print ("  Рекомендации активированы для всех демо-аккаунтов!")
        print ("="*60 )

    except Exception as exc :
        conn .rollback ()
        print (f"\nОшибка: {exc }")
        import traceback ;traceback .print_exc ()
        sys .exit (1 )
    finally :
        cur .close ()
        conn .close ()


if __name__ =="__main__":
    main ()
