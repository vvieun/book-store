import React, { useEffect, useMemo, useRef, useState } from "react";
import type { Book } from "../domain/model";
import { canModerateRole, isAdminRole, normalizeRole } from "../domain/roles";
import type { ApiGateway, LocalStorageGateway } from "../application/ports";
import { createInitialState, createActions, type AppState } from "../application/usecases/store";
import { LIST_PAGE_SIZE, filterByNumericId, paginateSlice } from "./moderationPaging";

type Props = {
  api: ApiGateway;
  storage: LocalStorageGateway;
  onAuthReset?: () => void;
};

type Tab = "catalog" | "book" | "cart" | "wishlist" | "orders" | "collections" | "moderation" | "admin";

function parseError(e: unknown): string {
  if (e instanceof Error && e.message) return e.message;
  return "Произошла ошибка. Попробуйте позже.";
}

function formatRating(v: number | null | undefined): string {
  return Number(v ?? 0).toFixed(2);
}

export function App({ api, storage, onAuthReset }: Props) {
  const [state, setState] = useState<AppState>(() => createInitialState(storage));
  const [tab, setTab] = useState<Tab>("catalog");

  const stateRef = useRef(state);
  stateRef.current = state;
  const currentCollectionRef = useRef<HTMLDivElement | null>(null);

  const actions = useMemo(() => createActions(api, storage, () => stateRef.current, setState), [api, storage]);

  const [authOpen, setAuthOpen] = useState(false);
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [authUsername, setAuthUsername] = useState("");
  const [authEmail, setAuthEmail] = useState("");
  const [authPassword, setAuthPassword] = useState("");
  const [authError, setAuthError] = useState("");

  const [searchQuery, setSearchQuery] = useState("");
  const [bookQty, setBookQty] = useState(1);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState("");

  const [collectionName, setCollectionName] = useState("");
  const [collectionDescription, setCollectionDescription] = useState("");
  const [collectionPickId, setCollectionPickId] = useState<number | "">("");
  const [collectionBookIsbn, setCollectionBookIsbn] = useState("");
  const [collectionDescriptionDraft, setCollectionDescriptionDraft] = useState("");

  const [adminTargetUserId, setAdminTargetUserId] = useState<number | "">("");
  const [adminNewRole, setAdminNewRole] = useState("CUSTOMER");
  const [statusMessage, setStatusMessage] = useState<{ text: string; isError: boolean } | null>(null);
  const [staffOrderPage, setStaffOrderPage] = useState(0);
  const [staffReviewPage, setStaffReviewPage] = useState(0);
  const [orderIdSearch, setOrderIdSearch] = useState("");
  const [reviewIdSearch, setReviewIdSearch] = useState("");
  const [collectionsListPage, setCollectionsListPage] = useState(0);
  const [collectionBooksPage, setCollectionBooksPage] = useState(0);
  const [collectionIdSearch, setCollectionIdSearch] = useState("");
  const pageSize = 20;
  const orderStatuses = ["PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"];

  const ownCollections = state.collections.filter((c) => c.ownerUserId === state.user?.userId);
  const canEditCurrentCollection = !!state.currentCollection && state.currentCollection.ownerUserId === state.user?.userId;
  const canModerate = canModerateRole(state.user?.role);
  const isAdmin = isAdminRole(state.user?.role);

  const filteredStaffOrders = useMemo(
    () => filterByNumericId(state.staffOrders, orderIdSearch, (order) => order.orderId),
    [state.staffOrders, orderIdSearch]
  );
  const pagedStaffOrders = useMemo(
    () => paginateSlice(filteredStaffOrders, staffOrderPage, LIST_PAGE_SIZE),
    [filteredStaffOrders, staffOrderPage]
  );

  const filteredModerationReviews = useMemo(
    () => filterByNumericId(state.moderationReviews, reviewIdSearch, (review) => review.reviewId),
    [state.moderationReviews, reviewIdSearch]
  );
  const pagedModerationReviews = useMemo(
    () => paginateSlice(filteredModerationReviews, staffReviewPage, LIST_PAGE_SIZE),
    [filteredModerationReviews, staffReviewPage]
  );

  const filteredCollections = useMemo(
    () => filterByNumericId(state.collections, collectionIdSearch, (c) => c.collectionId),
    [state.collections, collectionIdSearch]
  );
  const pagedCollections = useMemo(
    () => paginateSlice(filteredCollections, collectionsListPage, LIST_PAGE_SIZE),
    [filteredCollections, collectionsListPage]
  );

  const currentCollectionBooks = state.currentCollection?.books ?? [];
  const pagedCollectionBooks = useMemo(
    () => paginateSlice(currentCollectionBooks, collectionBooksPage, LIST_PAGE_SIZE),
    [currentCollectionBooks, collectionBooksPage]
  );

  useEffect(() => {
    setStaffOrderPage(0);
  }, [orderIdSearch]);

  useEffect(() => {
    setStaffReviewPage(0);
  }, [reviewIdSearch]);

  useEffect(() => {
    setCollectionsListPage(0);
  }, [collectionIdSearch]);

  useEffect(() => {
    setCollectionBooksPage(0);
  }, [state.currentCollection?.collectionId]);

  useEffect(() => {
    void actions.loadCatalog("", 0, pageSize);
  }, [actions, state.user?.userId]);

  useEffect(() => {
    if (state.user) void actions.refreshAccountData();
  }, [actions, state.user?.userId]);

  useEffect(() => {
    if (tab === "collections" && state.user) {
      void actions.loadCollections();
    }
  }, [tab, actions, state.user?.userId]);

  useEffect(() => {
    function refreshCatalogOnFocus() {
      void actions.loadCatalog(searchQuery.trim(), stateRef.current.catalogPage, pageSize);
    }
    window.addEventListener("focus", refreshCatalogOnFocus);
    return () => window.removeEventListener("focus", refreshCatalogOnFocus);
  }, [actions, searchQuery]);

  function currentUserLabel(): string {
    if (!state.user) return "Гость";
    const role = normalizeRole(state.user.role);
    return role === "CUSTOMER" || !role ? state.user.username : `${state.user.username} (${role})`;
  }

  function orderBuyerLabel(order: AppState["staffOrders"][number]): string {
    return order.buyerUsername || (order.buyerUserId ? `пользователь ${order.buyerUserId}` : "покупатель не указан");
  }

  function collectionOwnerLabel(ownerUsername?: string | null, ownerUserId?: number | null): string {
    return ownerUsername || (ownerUserId ? `пользователь ${ownerUserId}` : "неизвестный владелец");
  }

  function isInWishlist(isbn: string): boolean {
    return state.wishlist.some((w) => w.isbn === isbn);
  }

  function wishlistButtonText(isbn: string): string {
    return isInWishlist(isbn) ? "Убрать из вишлиста" : "Добавить в вишлист";
  }

  function notify(text: string, isError: boolean) {
    setStatusMessage({ text, isError });
    if (isError) console.error(text);
    else console.log(text);
  }

  async function loadCatalog(reset = false) {
    try {
      const q = reset ? "" : searchQuery.trim();
      if (reset) setSearchQuery("");
      const count = await actions.loadCatalog(q, 0, pageSize);
      notify(`В каталоге: ${count} книг.`, false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function nextCatalogPage() {
    try {
      const count = await actions.nextCatalogPage(searchQuery.trim(), pageSize);
      notify(`В каталоге: ${count} книг.`, false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function previousCatalogPage() {
    try {
      const count = await actions.previousCatalogPage(searchQuery.trim(), pageSize);
      notify(`В каталоге: ${count} книг.`, false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function openBook(isbn: string) {
    try {
      await actions.openBook(isbn);
      setTab("book");
      notify("Карточка книги открыта.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  function resolveBook(isbn: string): Book | null {
    return state.catalog.find((b) => b.isbn === isbn) || state.currentBook;
  }

  async function submitAuth() {
    try {
      setAuthError("");
      const username = authUsername.trim();
      const email = authEmail.trim();
      const password = authPassword;
      if (!username || !password || (authMode === "register" && !email)) {
        setAuthError(authMode === "register" ? "Заполните логин, email и пароль." : "Введите логин и пароль.");
        return;
      }
      if (authMode === "register") await actions.register(username, email, password);
      else await actions.login(username, password);
      setAuthOpen(false);
      notify(authMode === "register" ? "Регистрация успешна. Вы вошли в аккаунт." : "Вы успешно вошли в систему.", false);
      await actions.loadCatalog("", 0, pageSize);
      await actions.loadOrders();
    } catch (e) {
      const msg = parseError(e);
      setAuthError(msg);
      notify(msg, true);
    }
  }

  function logout() {
    onAuthReset?.();
    actions.logout();
    setAuthPassword("");
    notify("Вы вышли из аккаунта.", false);
    void actions.loadCatalog("", 0, pageSize);
    void actions.loadOrders();
  }

  function addToCart(isbn: string, qty: number) {
    const book = resolveBook(isbn);
    if (!book) {
      notify("Книга не найдена.", true);
      return;
    }
    actions.addToCart(book, qty);
    notify(`Книга "${book.title}" добавлена в корзину.`, false);
  }

  async function toggleWishlist(isbn: string) {
    const book = resolveBook(isbn);
    if (!book) {
      notify("Книга не найдена.", true);
      return;
    }
    try {
      const added = await actions.toggleWishlist(book);
      notify(added ? `Книга "${book.title}" добавлена в вишлист.` : `Книга "${book.title}" удалена из вишлиста.`, false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function saveReview() {
    try {
      if (!state.user) {
        notify("Для отзыва нужно войти в систему.", true);
        return;
      }
      if (!state.currentBook) {
        notify("Сначала откройте карточку книги.", true);
        return;
      }
      await actions.saveReview(Number(reviewRating), reviewComment.trim());
      setReviewComment("");
      await actions.loadCatalog(searchQuery.trim(), state.catalogPage, pageSize);
      notify("Отзыв сохранен.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function checkoutCart() {
    try {
      if (!state.user) {
        notify("Для оформления заказа нужно войти в систему.", true);
        return;
      }
      if (state.cart.length === 0) {
        notify("Корзина пуста.", true);
        return;
      }
      await actions.checkoutCart();
      await actions.loadOrders();
      notify("Заказ из корзины успешно оформлен.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function orderCurrentBook() {
    try {
      if (!state.user) {
        notify("Для оформления заказа нужно войти в систему.", true);
        return;
      }
      if (!state.currentBook) {
        notify("Сначала откройте карточку книги.", true);
        return;
      }
      await actions.orderCurrentBook(Number(bookQty) || 1);
      await actions.loadOrders();
      notify("Заказ успешно оформлен со страницы книги.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function loadCollections() {
    try {
      await actions.loadCollections();
      notify("Подборки обновлены.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function createCollection() {
    try {
      const name = collectionName.trim();
      const desc = collectionDescription.trim();
      if (!name) {
        notify("Введите название подборки.", true);
        return;
      }
      const created = await actions.createCollection(name, desc);
      setCollectionName("");
      setCollectionDescription("");
      notify(`Подборка "${created.name}" создана.`, false);
      const details = await actions.openCollection(created.collectionId);
      setCollectionDescriptionDraft(details.description || "");
      setTimeout(() => currentCollectionRef.current?.scrollIntoView({ behavior: "smooth", block: "start" }), 0);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function openCollection(collectionId: number) {
    try {
      setTab("collections");
      const details = await actions.openCollection(collectionId);
      setCollectionDescriptionDraft(details.description || "");
      const listIndex = stateRef.current.collections.findIndex((c) => c.collectionId === collectionId);
      if (listIndex >= 0) {
        const filtered = filterByNumericId(
          stateRef.current.collections,
          collectionIdSearch,
          (c) => c.collectionId
        );
        const indexInFiltered = filtered.findIndex((c) => c.collectionId === collectionId);
        if (indexInFiltered >= 0) {
          setCollectionsListPage(Math.floor(indexInFiltered / LIST_PAGE_SIZE));
        }
      }
      notify(`Подборка «${details.name}» открыта.`, false);
      requestAnimationFrame(() => {
        currentCollectionRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
      });
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function deleteCollection(collectionId: number) {
    try {
      await actions.deleteCollection(collectionId);
      notify("Подборка удалена.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function addCurrentBookToPickedCollection() {
    try {
      if (!state.currentBook) {
        notify("Сначала откройте карточку книги.", true);
        return;
      }
      if (collectionPickId === "") {
        notify("Выберите подборку.", true);
        return;
      }
      await actions.addCurrentBookToCollection(Number(collectionPickId));
      notify("Книга добавлена в подборку.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function addPickedBookToCurrentCollection() {
    try {
      if (!state.currentCollection) {
        notify("Сначала откройте подборку.", true);
        return;
      }
      if (!canEditCurrentCollection) {
        notify("Можно изменять только свои подборки.", true);
        return;
      }
      if (!collectionBookIsbn) {
        notify("Выберите книгу.", true);
        return;
      }
      const updated = await actions.addBookToCollection(state.currentCollection.collectionId, collectionBookIsbn);
      const book = updated.books.find((b) => b.isbn === collectionBookIsbn);
      setCollectionBookIsbn("");
      notify(book ? `Книга "${book.title}" добавлена в подборку.` : "Книга добавлена в подборку.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function saveCollectionDescription() {
    try {
      if (!state.currentCollection) {
        notify("Сначала откройте подборку.", true);
        return;
      }
      if (!canEditCurrentCollection) {
        notify("Можно изменять только свои подборки.", true);
        return;
      }
      const updated = await actions.updateCollectionDescription(state.currentCollection.collectionId, collectionDescriptionDraft.trim());
      setCollectionDescriptionDraft(updated.description || "");
      notify(updated.description ? "Описание подборки обновлено." : "Описание подборки очищено.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function submitAdminRoleChange() {
    try {
      if (adminTargetUserId === "") {
        notify("Укажите числовой userId пользователя.", true);
        return;
      }
      await actions.updateUserRole(Number(adminTargetUserId), adminNewRole);
      notify("Роль пользователя обновлена.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function loadModerationData() {
    try {
      await actions.loadModerationData();
      notify("Данные модерации обновлены.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function updateStaffOrderStatus(orderId: number, status: string) {
    try {
      await actions.updateStaffOrderStatus(orderId, status);
      notify(`Статус заказа №${orderId} обновлен.`, false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function deleteModerationReview(reviewId: number) {
    try {
      await actions.deleteModerationReview(reviewId);
      notify("Отзыв удален.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  async function removeBookFromCurrentCollection(isbn: string) {
    try {
      if (!state.currentCollection) return;
      if (!canEditCurrentCollection) {
        notify("Можно изменять только свои подборки.", true);
        return;
      }
      await actions.removeBookFromCollection(state.currentCollection.collectionId, isbn);
      notify("Книга удалена из подборки.", false);
    } catch (e) {
      notify(parseError(e), true);
    }
  }

  return (
    <div className="container">
      <div className="panel">
        <h1 className="title">BookStore</h1>
        <div className="row">
          {!state.user && <button onClick={() => setAuthOpen(true)}>Войти</button>}
          {!!state.user && <button onClick={logout}>Выйти</button>}
          <span className="muted">{currentUserLabel()}</span>
        </div>
      </div>

      {statusMessage && (
        <div className={`panel status-banner ${statusMessage.isError ? "status-error" : "status-ok"}`}>
          <span>{statusMessage.text}</span>
          <button type="button" className="status-dismiss" onClick={() => setStatusMessage(null)}>
            Закрыть
          </button>
        </div>
      )}

      <div className="panel">
        <div className="tabs">
          <button className={`tab-btn ${tab === "catalog" ? "active" : ""}`} onClick={() => setTab("catalog")}>
            Каталог
          </button>
          <button className={`tab-btn ${tab === "book" ? "active" : ""}`} onClick={() => setTab("book")}>
            Книга
          </button>
          <button className={`tab-btn ${tab === "cart" ? "active" : ""}`} onClick={() => setTab("cart")}>
            Корзина
          </button>
          <button className={`tab-btn ${tab === "wishlist" ? "active" : ""}`} onClick={() => setTab("wishlist")}>
            Вишлист
          </button>
          <button className={`tab-btn ${tab === "orders" ? "active" : ""}`} onClick={() => setTab("orders")}>
            Заказы
          </button>
          <button className={`tab-btn ${tab === "collections" ? "active" : ""}`} onClick={() => setTab("collections")}>
            Подборки
          </button>
          {canModerate && (
            <button
              className={`tab-btn ${tab === "moderation" ? "active" : ""}`}
              onClick={() => {
                setTab("moderation");
                void actions.loadModerationData();
              }}
            >
              Модерация
            </button>
          )}
          {isAdmin && (
            <button className={`tab-btn ${tab === "admin" ? "active" : ""}`} onClick={() => setTab("admin")}>
              Админ
            </button>
          )}
        </div>
      </div>

      {/* Catalog */}
      {tab === "catalog" && (
        <div className="panel">
          <h3>Каталог</h3>
          <div className="row">
            <input value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} placeholder="Поиск по названию" />
            <button onClick={() => void loadCatalog(false)}>Найти</button>
            <button onClick={() => void loadCatalog(true)}>Сбросить</button>
          </div>

          <div className="book-grid top-space">
            {state.catalog.map((book) => (
              <div key={book.isbn} className="book-card">
                <h4>{book.title}</h4>
                <p>{book.description || "Описание пока не добавлено."}</p>
                <div className="price">{book.price} руб</div>
                <div className="muted">Рейтинг: {formatRating(book.avgRating)}</div>
                <div className="row top-space">
                  <button onClick={() => void openBook(book.isbn)}>Открыть</button>
                  <button onClick={() => addToCart(book.isbn, 1)}>В корзину</button>
                  <button onClick={() => void toggleWishlist(book.isbn)}>{wishlistButtonText(book.isbn)}</button>
                </div>
              </div>
            ))}
          </div>

          <div className="row top-space">
            <button disabled={state.catalogPage === 0} onClick={() => void previousCatalogPage()}>
              Назад
            </button>
            <span className="muted">Страница {state.catalogPage + 1}</span>
            <button disabled={!state.catalogHasNext} onClick={() => void nextCatalogPage()}>
              Вперед
            </button>
          </div>
        </div>
      )}

      {/* Book */}
      {tab === "book" && (
        <div className="panel">
          <h3>Карточка книги</h3>
          {!state.currentBook ? (
            <div className="muted">Выберите книгу в каталоге.</div>
          ) : (
            <>
              <div className="muted">
                <b>{state.currentBook.title}</b>
                <br />
                <span className="muted">{state.currentBook.description || "Описание пока не добавлено."}</span>
                <br />
                Цена: {state.currentBook.price} руб, рейтинг: {formatRating(state.currentBook.avgRating)}
              </div>

              <div className="row top-space">
                <input type="number" min={1} value={bookQty} onChange={(e) => setBookQty(Number(e.target.value))} />
                <button onClick={() => addToCart(state.currentBook!.isbn, Number(bookQty) || 1)}>В корзину</button>
                <button onClick={() => void orderCurrentBook()}>Купить сейчас</button>
                <button onClick={() => void toggleWishlist(state.currentBook!.isbn)}>{wishlistButtonText(state.currentBook.isbn)}</button>
              </div>

              <div className="top-space">
                <h4>Добавить книгу в подборку</h4>
                <div className="row">
                  <select value={collectionPickId} onChange={(e) => setCollectionPickId(e.target.value ? Number(e.target.value) : "")}>
                    <option value="">Выберите подборку</option>
                    {ownCollections.map((c) => (
                      <option key={c.collectionId} value={c.collectionId}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                  <button onClick={() => void addCurrentBookToPickedCollection()}>Добавить</button>
                  <button onClick={() => void loadCollections()}>Обновить список</button>
                </div>
                {!state.user && <div className="muted top-space">Войдите, чтобы создавать подборки и добавлять книги.</div>}
              </div>

              <div className="top-space">
                <h4>Оставить отзыв</h4>
                <div className="row">
                  <select value={reviewRating} onChange={(e) => setReviewRating(Number(e.target.value))}>
                    {[5, 4, 3, 2, 1].map((n) => (
                      <option key={n} value={n}>
                        {n}
                      </option>
                    ))}
                  </select>
                  <textarea value={reviewComment} onChange={(e) => setReviewComment(e.target.value)} rows={2} cols={46} placeholder="Комментарий" />
                  <button onClick={() => void saveReview()}>Сохранить</button>
                </div>
                <ul className="list">
                  {state.currentReviews.map((r) => (
                    <li key={r.reviewId}>
                      {(r.username || "Покупатель") + `: ${r.rating}/5 - ` + (r.comment || "")}
                    </li>
                  ))}
                </ul>
              </div>
            </>
          )}
        </div>
      )}

      {/* Cart */}
      {tab === "cart" && (
        <div className="panel">
          <h3>Корзина</h3>
          <ul className="list">
            {state.cart.length === 0 ? (
              <li>Корзина пуста.</li>
            ) : (
              state.cart.map((item, idx) => {
                const sum = Number(item.price) * Number(item.quantity);
                return (
                  <li key={`${item.isbn}-${idx}`}>
                    {item.title} — {item.quantity} шт. ({sum.toFixed(2)} руб)
                    <button style={{ marginLeft: 8 }} onClick={() => actions.removeCartItem(idx)}>
                      Удалить
                    </button>
                  </li>
                );
              })
            )}
          </ul>
          <div className="row top-space">
            <button onClick={() => void checkoutCart()}>Оформить всё из корзины</button>
            <button onClick={() => actions.clearCart()}>Очистить корзину</button>
          </div>
        </div>
      )}

      {/* Wishlist */}
      {tab === "wishlist" && (
        <div className="panel">
          <h3>Вишлист</h3>
          <ul className="list">
            {state.wishlist.length === 0 ? (
              <li>Вишлист пуст.</li>
            ) : (
              state.wishlist.map((w) => (
                <li key={w.isbn}>
                  {w.title}
                  <button style={{ marginLeft: 8 }} onClick={() => void openBook(w.isbn)}>
                    Открыть
                  </button>
                </li>
              ))
            )}
          </ul>
        </div>
      )}

      {/* Orders */}
      {tab === "orders" && (
        <div className="panel">
          <h3>Мои заказы</h3>
          <button onClick={() => void actions.loadOrders()}>Обновить</button>
          <ul className="list top-space">
            {!state.user ? (
              <li>Войдите, чтобы видеть свои заказы.</li>
            ) : state.orders.length === 0 ? (
              <li>У вас пока нет заказов.</li>
            ) : (
              state.orders.map((o) => (
                <li key={o.orderId}>
                  Заказ №{o.orderId} — {o.status}, сумма: {o.totalAmount} руб
                </li>
              ))
            )}
          </ul>
        </div>
      )}

      {/* Moderation */}
      {tab === "moderation" && canModerate && (
        <div className="panel">
          <h3>Модерация</h3>
          <div className="row">
            <button onClick={() => void loadModerationData()}>Обновить</button>
            <span className="muted">Доступно для MODERATOR и ADMIN.</span>
          </div>

          <div className="top-space">
            <h4>Заказы</h4>
            <div className="row">
              <input
                type="number"
                min={1}
                value={orderIdSearch}
                onChange={(e) => setOrderIdSearch(e.target.value)}
                placeholder="№ заказа"
              />
              <button type="button" onClick={() => setOrderIdSearch("")}>
                Сбросить
              </button>
            </div>
            <ul className="list top-space">
              {state.staffOrders.length === 0 ? (
                <li>Заказы не загружены. Нажмите «Обновить».</li>
              ) : filteredStaffOrders.length === 0 ? (
                <li>Заказ с номером «{orderIdSearch.trim()}» не найден.</li>
              ) : (
                pagedStaffOrders.items.map((order) => (
                  <li key={order.orderId}>
                    Заказ №{order.orderId} — {orderBuyerLabel(order)}, сумма: {order.totalAmount} руб
                    <div className="row top-space">
                      <label className="muted">Статус</label>
                      <select
                        value={order.status}
                        onChange={(e) => void updateStaffOrderStatus(order.orderId, e.target.value)}
                      >
                        {orderStatuses.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    </div>
                  </li>
                ))
              )}
            </ul>
            {filteredStaffOrders.length > 0 && (
              <div className="row top-space">
                <button
                  type="button"
                  disabled={pagedStaffOrders.page === 0}
                  onClick={() => setStaffOrderPage((p) => Math.max(0, p - 1))}
                >
                  Назад
                </button>
                <span className="muted">
                  Страница {pagedStaffOrders.page + 1} из {pagedStaffOrders.totalPages} ({pagedStaffOrders.total}{" "}
                  {pagedStaffOrders.total === 1 ? "заказ" : "заказов"})
                </span>
                <button
                  type="button"
                  disabled={pagedStaffOrders.page >= pagedStaffOrders.totalPages - 1}
                  onClick={() => setStaffOrderPage((p) => p + 1)}
                >
                  Вперёд
                </button>
              </div>
            )}
          </div>

          <div className="top-space">
            <h4>Отзывы</h4>
            <div className="row">
              <input
                type="number"
                min={1}
                value={reviewIdSearch}
                onChange={(e) => setReviewIdSearch(e.target.value)}
                placeholder="№ отзыва"
              />
              <button type="button" onClick={() => setReviewIdSearch("")}>
                Сбросить
              </button>
            </div>
            <ul className="list top-space">
              {state.moderationReviews.length === 0 ? (
                <li>Отзывы не загружены. Нажмите «Обновить».</li>
              ) : filteredModerationReviews.length === 0 ? (
                <li>Отзыв с номером «{reviewIdSearch.trim()}» не найден.</li>
              ) : (
                pagedModerationReviews.items.map((review) => (
                  <li key={review.reviewId}>
                    <span className="muted">№{review.reviewId}</span> · {(review.username || "Покупатель") + `: ${review.rating}/5`}
                    <span className="muted"> · {review.isbn}</span>
                    {review.comment ? ` — ${review.comment}` : ""}
                    <button type="button" style={{ marginLeft: 8 }} onClick={() => void deleteModerationReview(review.reviewId)}>
                      Удалить
                    </button>
                  </li>
                ))
              )}
            </ul>
            {filteredModerationReviews.length > 0 && (
              <div className="row top-space">
                <button
                  type="button"
                  disabled={pagedModerationReviews.page === 0}
                  onClick={() => setStaffReviewPage((p) => Math.max(0, p - 1))}
                >
                  Назад
                </button>
                <span className="muted">
                  Страница {pagedModerationReviews.page + 1} из {pagedModerationReviews.totalPages} (
                  {pagedModerationReviews.total} {pagedModerationReviews.total === 1 ? "отзыв" : "отзывов"})
                </span>
                <button
                  type="button"
                  disabled={pagedModerationReviews.page >= pagedModerationReviews.totalPages - 1}
                  onClick={() => setStaffReviewPage((p) => p + 1)}
                >
                  Вперёд
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Admin: только смена роли */}
      {tab === "admin" && isAdmin && (
        <div className="panel">
          <h3>Администрирование</h3>
          <p className="muted">Смена роли: PATCH /api/users/{"{userId}"}/role — укажите числовой идентификатор из базы.</p>
          <div className="row top-space">
            <label className="muted">userId</label>
            <input
              type="number"
              min={1}
              value={adminTargetUserId}
              onChange={(e) => setAdminTargetUserId(e.target.value ? Number(e.target.value) : "")}
              placeholder="ID пользователя"
            />
          </div>
          <div className="row top-space">
            <label className="muted">Новая роль</label>
            <select value={adminNewRole} onChange={(e) => setAdminNewRole(e.target.value)}>
              <option value="CUSTOMER">CUSTOMER</option>
              <option value="MODERATOR">MODERATOR</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <button onClick={() => void submitAdminRoleChange()}>Применить</button>
          </div>
          <p className="muted top-space">
            После успешной смены вашей же роли интерфейс может потребовать повторный вход — это ожидаемо.
          </p>
        </div>
      )}

      {/* Collections */}
      {tab === "collections" && (
        <div className="panel">
          <h3>Подборки книг</h3>
          {!state.user ? (
            <div className="muted">Войдите, чтобы создавать и просматривать свои подборки.</div>
          ) : (
            <>
              <div className="row">
                <button onClick={() => void loadCollections()}>Обновить</button>
              </div>

              <div className="top-space">
                <h4>Создать подборку</h4>
                <div className="row">
                  <input value={collectionName} onChange={(e) => setCollectionName(e.target.value)} placeholder="Название" />
                  <input value={collectionDescription} onChange={(e) => setCollectionDescription(e.target.value)} placeholder="Описание (опционально)" />
                  <button onClick={() => void createCollection()}>Создать</button>
                </div>
              </div>

              <div className="top-space">
                <h4>Все подборки</h4>
                <div className="row">
                  <input
                    type="number"
                    min={1}
                    value={collectionIdSearch}
                    onChange={(e) => setCollectionIdSearch(e.target.value)}
                    placeholder="№ подборки"
                  />
                  <button type="button" onClick={() => setCollectionIdSearch("")}>
                    Сбросить
                  </button>
                </div>
                <ul className="list top-space">
                  {state.collections.length === 0 ? (
                    <li>Подборок пока нет.</li>
                  ) : filteredCollections.length === 0 ? (
                    <li>Подборка с номером «{collectionIdSearch.trim()}» не найдена.</li>
                  ) : (
                    pagedCollections.items.map((c) => {
                      const isOwn = c.ownerUserId === state.user?.userId;
                      const isOpen = state.currentCollection?.collectionId === c.collectionId;
                      return (
                        <li key={c.collectionId} className={isOpen ? "collection-item-active" : undefined}>
                          <span className="muted">№{c.collectionId}</span> · <b>{c.name}</b> {c.description ? `— ${c.description}` : ""}{" "}
                          <span className="muted">{isOwn ? "моя" : `владелец: ${collectionOwnerLabel(c.ownerUsername, c.ownerUserId)}`}</span>
                          <button type="button" style={{ marginLeft: 8 }} onClick={() => void openCollection(c.collectionId)}>
                            Открыть
                          </button>
                          {isOwn && (
                            <button type="button" style={{ marginLeft: 8 }} onClick={() => void deleteCollection(c.collectionId)}>
                              Удалить
                            </button>
                          )}
                        </li>
                      );
                    })
                  )}
                </ul>
                {filteredCollections.length > 0 && (
                  <div className="row top-space">
                    <button
                      type="button"
                      disabled={pagedCollections.page === 0}
                      onClick={() => setCollectionsListPage((p) => Math.max(0, p - 1))}
                    >
                      Назад
                    </button>
                    <span className="muted">
                      Страница {pagedCollections.page + 1} из {pagedCollections.totalPages} ({pagedCollections.total}{" "}
                      {pagedCollections.total === 1 ? "подборка" : "подборок"})
                    </span>
                    <button
                      type="button"
                      disabled={pagedCollections.page >= pagedCollections.totalPages - 1}
                      onClick={() => setCollectionsListPage((p) => p + 1)}
                    >
                      Вперёд
                    </button>
                  </div>
                )}
              </div>

              <div className="top-space" ref={currentCollectionRef}>
                <h4>Текущая подборка</h4>
                {!state.currentCollection ? (
                  <div className="muted">Выберите подборку из списка.</div>
                ) : (
                  <>
                    <div className="muted">
                      <b>{state.currentCollection.name}</b>
                      {!canEditCurrentCollection && (
                        <span className="muted"> · владелец: {collectionOwnerLabel(state.currentCollection.ownerUsername, state.currentCollection.ownerUserId)}</span>
                      )}
                      <br />
                      {state.currentCollection.description || "Описание не задано."}
                    </div>
                    {canEditCurrentCollection && (
                      <>
                        <div className="top-space">
                          <h4>{state.currentCollection.description ? "Изменить описание" : "Добавить описание"}</h4>
                          <div className="row">
                            <textarea
                              value={collectionDescriptionDraft}
                              onChange={(e) => setCollectionDescriptionDraft(e.target.value)}
                              rows={2}
                              cols={46}
                              placeholder="Описание подборки"
                            />
                            <button onClick={() => void saveCollectionDescription()}>Сохранить</button>
                          </div>
                        </div>

                        <div className="top-space">
                          <h4>Добавить книгу</h4>
                          <div className="row">
                            <select value={collectionBookIsbn} onChange={(e) => setCollectionBookIsbn(e.target.value)}>
                              <option value="">Выберите книгу из каталога</option>
                              {state.catalog.map((book) => (
                                <option key={book.isbn} value={book.isbn}>
                                  {book.title} ({book.isbn})
                                </option>
                              ))}
                            </select>
                            <button onClick={() => void addPickedBookToCurrentCollection()}>Добавить</button>
                            <button onClick={() => void loadCatalog(false)}>Загрузить каталог</button>
                          </div>
                        </div>
                      </>
                    )}
                    <h4 className="top-space">Книги в подборке</h4>
                    <ul className="list top-space">
                      {currentCollectionBooks.length === 0 ? (
                        <li>В подборке пока нет книг.</li>
                      ) : (
                        pagedCollectionBooks.items.map((b) => (
                          <li key={b.isbn}>
                            {b.title}
                            <button type="button" style={{ marginLeft: 8 }} onClick={() => void openBook(b.isbn)}>
                              Открыть
                            </button>
                            {canEditCurrentCollection && (
                              <button type="button" style={{ marginLeft: 8 }} onClick={() => void removeBookFromCurrentCollection(b.isbn)}>
                                Удалить из подборки
                              </button>
                            )}
                          </li>
                        ))
                      )}
                    </ul>
                    {currentCollectionBooks.length > 0 && (
                      <div className="row top-space">
                        <button
                          type="button"
                          disabled={pagedCollectionBooks.page === 0}
                          onClick={() => setCollectionBooksPage((p) => Math.max(0, p - 1))}
                        >
                          Назад
                        </button>
                        <span className="muted">
                          Страница {pagedCollectionBooks.page + 1} из {pagedCollectionBooks.totalPages} (
                          {pagedCollectionBooks.total} {pagedCollectionBooks.total === 1 ? "книга" : "книг"})
                        </span>
                        <button
                          type="button"
                          disabled={pagedCollectionBooks.page >= pagedCollectionBooks.totalPages - 1}
                          onClick={() => setCollectionBooksPage((p) => p + 1)}
                        >
                          Вперёд
                        </button>
                      </div>
                    )}
                  </>
                )}
              </div>
            </>
          )}
        </div>
      )}

      {/* Auth modal */}
      <div
        className={`auth-overlay ${authOpen ? "show" : ""}`}
        onClick={(e) => {
          if (e.target === e.currentTarget) setAuthOpen(false);
        }}
      >
        <div className="auth-modal">
          <div className="auth-head">
            <h3 className="auth-title">{authMode === "register" ? "Регистрация" : "Вход"}</h3>
            <button onClick={() => setAuthOpen(false)}>Закрыть</button>
          </div>
          <div className="row auth-switch">
            <button className={authMode === "login" ? "active" : ""} onClick={() => setAuthMode("login")}>
              Вход
            </button>
            <button className={authMode === "register" ? "active" : ""} onClick={() => setAuthMode("register")}>
              Регистрация
            </button>
          </div>
          <div className="top-space">
            <input className="full-width" placeholder="Логин" value={authUsername} onChange={(e) => setAuthUsername(e.target.value)} />
          </div>
          {authMode === "register" && (
            <div className="top-space">
              <input className="full-width" placeholder="Email" value={authEmail} onChange={(e) => setAuthEmail(e.target.value)} />
            </div>
          )}
          <div className="top-space">
            <input className="full-width" type="password" placeholder="Пароль" value={authPassword} onChange={(e) => setAuthPassword(e.target.value)} />
          </div>
          <div className="danger top-space">{authError}</div>
          <div className="top-space">
            <button className="full-width" onClick={() => void submitAuth()}>
              {authMode === "register" ? "Создать аккаунт" : "Войти"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
