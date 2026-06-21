import type { ApiGateway, LocalStorageGateway } from "../ports";
import type { Book, CartItem, Collection, CollectionDetails, Order, Recommendation, Review, User, WishlistItem } from "../../domain/model";
import { canModerateRole } from "../../domain/roles";

export type AppState = {
  token: string;
  user: User | null;
  catalog: Book[];
  catalogPage: number;
  catalogHasNext: boolean;
  currentBook: Book | null;
  currentReviews: Review[];
  cart: CartItem[];
  wishlist: WishlistItem[];
  recommendations: Recommendation[];
  orders: Order[];
  staffOrders: Order[];
  moderationReviews: Review[];
  collections: Collection[];
  currentCollection: CollectionDetails | null;
};

export function createInitialState(storage: LocalStorageGateway): AppState {
  const session = storage.loadSession();
  return {
    token: session?.token || "",
    user: session?.user || null,
    catalog: [],
    catalogPage: 0,
    catalogHasNext: false,
    currentBook: null,
    currentReviews: [],
    cart: storage.loadCart(),
    wishlist: [],
    recommendations: [],
    orders: [],
    staffOrders: [],
    moderationReviews: [],
    collections: [],
    currentCollection: null,
  };
}

export function createActions(api: ApiGateway, storage: LocalStorageGateway, get: () => AppState, set: (s: AppState) => void) {
  function ensureOwnCollection(collectionId: number) {
    const st = get();
    const collection = st.collections.find((c) => c.collectionId === collectionId)
      || (st.currentCollection?.collectionId === collectionId ? st.currentCollection : null);
    if (!st.user) throw new Error("Требуется вход в систему.");
    if (!collection || collection.ownerUserId !== st.user.userId) {
      throw new Error("Можно изменять только свои подборки.");
    }
  }

  return {
    setToken(token: string) {
      set({ ...get(), token });
    },

    async refreshAccountData() {
      const st = get();
      if (!st.user) return;
      try {
        const [user, wishlist, collections, orders] = await Promise.all([
          api.getCurrentUser(),
          api.getWishlist(),
          api.getCollections(),
          api.getOrders(),
        ]);
        storage.saveSession({ token: st.token, user });
        set({ ...get(), user, wishlist, collections, orders });
      } catch (e) {
        storage.clearSession();
        set({
          ...get(),
          token: "",
          user: null,
          wishlist: [],
          recommendations: [],
          orders: [],
          staffOrders: [],
          moderationReviews: [],
          collections: [],
          currentCollection: null,
        });
        throw e;
      }
    },

    async login(username: string, password: string) {
      const data = await api.login(username, password);
      storage.saveSession({ token: data.token, user: data.user });
      const [wishlist, collections] = await Promise.all([api.getWishlist(), api.getCollections()]);
      set({ ...get(), token: data.token, user: data.user, wishlist, collections, currentCollection: null });
    },

    async register(username: string, email: string, password: string) {
      const data = await api.register(username, email, password);
      storage.saveSession({ token: data.token, user: data.user });
      const [wishlist, collections] = await Promise.all([api.getWishlist(), api.getCollections()]);
      set({ ...get(), token: data.token, user: data.user, wishlist, collections, currentCollection: null });
    },

    logout() {
      storage.clearSession();
      set({
        ...get(),
        token: "",
        user: null,
        wishlist: [],
        recommendations: [],
        orders: [],
        staffOrders: [],
        moderationReviews: [],
        collections: [],
        currentCollection: null,
      });
    },

    async loadCatalog(query: string, page = 0, size = 20): Promise<number> {
      const books = await api.getBooks(query, page, size + 1);
      const catalog = books.slice(0, size);
      set({ ...get(), catalog, catalogPage: page, catalogHasNext: books.length > size });
      return catalog.length;
    },

    async nextCatalogPage(query: string, size = 20) {
      const st = get();
      if (!st.catalogHasNext) return st.catalog.length;
      const books = await api.getBooks(query, st.catalogPage + 1, size + 1);
      const catalog = books.slice(0, size);
      set({ ...get(), catalog, catalogPage: st.catalogPage + 1, catalogHasNext: books.length > size });
      return catalog.length;
    },

    async previousCatalogPage(query: string, size = 20) {
      const st = get();
      const page = Math.max(0, st.catalogPage - 1);
      const books = await api.getBooks(query, page, size + 1);
      const catalog = books.slice(0, size);
      set({ ...get(), catalog, catalogPage: page, catalogHasNext: books.length > size });
      return catalog.length;
    },

    async openBook(isbn: string) {
      const book = await api.getBook(isbn);
      const reviews = await api.getBookReviews(isbn);
      set({ ...get(), currentBook: book, currentReviews: reviews });
    },

    async saveReview(rating: number, comment: string) {
      const st = get();
      if (!st.user || !st.currentBook) return;
      await api.createReview(st.currentBook.isbn, rating, comment);
      const reviews = await api.getBookReviews(st.currentBook.isbn);
      set({ ...get(), currentReviews: reviews });
    },

    addToCart(book: Book, qty: number) {
      const st = get();
      const cart = [...st.cart];
      const existing = cart.find((i) => i.isbn === book.isbn);
      if (existing) existing.quantity += qty;
      else cart.push({ isbn: book.isbn, title: book.title, price: book.price, quantity: qty });
      storage.saveCart(cart);
      set({ ...st, cart });
    },

    removeCartItem(index: number) {
      const st = get();
      const cart = [...st.cart];
      cart.splice(index, 1);
      storage.saveCart(cart);
      set({ ...st, cart });
    },

    clearCart() {
      const st = get();
      storage.saveCart([]);
      set({ ...st, cart: [] });
    },

    async checkoutCart() {
      const st = get();
      for (const item of st.cart) {
        await api.createOrder(item.isbn, item.quantity);
      }
      storage.saveCart([]);
      set({ ...get(), cart: [] });
    },

    async orderCurrentBook(quantity: number) {
      const st = get();
      if (!st.currentBook) return;
      await api.createOrder(st.currentBook.isbn, quantity);
    },

    async loadRecommendations(count: number) {
      const recs = await api.getRecommendations(count);
      set({ ...get(), recommendations: recs });
    },

    async clearRecommendationsCache(count = 5) {
      await api.clearRecommendationsCache();
      const recs = await api.getRecommendations(count);
      set({ ...get(), recommendations: recs });
    },

    async loadOrders() {
      const st = get();
      if (!st.user) {
        set({ ...st, orders: [] });
        return;
      }
      const orders = await api.getOrders();
      set({ ...get(), orders });
    },

    async loadModerationData() {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      if (!canModerateRole(st.user.role)) {
        throw new Error("Недостаточно прав.");
      }
      const [staffOrders, moderationReviews] = await Promise.all([
        api.getAllOrders(),
        api.getAllReviewsForModeration(),
      ]);
      set({ ...get(), staffOrders, moderationReviews });
    },

    async updateStaffOrderStatus(orderId: number, status: string) {
      const updated = await api.updateOrderStatus(orderId, status);
      const staffOrders = get().staffOrders.map((order) => order.orderId === orderId ? updated : order);
      set({ ...get(), staffOrders });
      return updated;
    },

    async deleteModerationReview(reviewId: number) {
      await api.deleteReview(reviewId);
      const moderationReviews = get().moderationReviews.filter((review) => review.reviewId !== reviewId);
      set({ ...get(), moderationReviews });
    },

    async toggleWishlist(book: Book) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      const wishlist = [...st.wishlist];
      const idx = wishlist.findIndex((w) => w.isbn === book.isbn);
      if (idx >= 0) {
        await api.removeFromWishlist(book.isbn);
        wishlist.splice(idx, 1);
        set({ ...get(), wishlist });
        return false;
      }
      const added = await api.addToWishlist(book.isbn);
      wishlist.push({ isbn: added.isbn || book.isbn, title: added.title || book.title });
      set({ ...get(), wishlist });
      return true;
    },

    async loadCollections() {
      const st = get();
      if (!st.user) {
        set({ ...st, collections: [], currentCollection: null });
        return;
      }
      const collections = await api.getCollections();
      set({ ...get(), collections });
    },

    async openCollection(collectionId: number) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      if (!Number.isFinite(collectionId)) throw new Error("Некорректный идентификатор подборки.");
      const details = await api.getCollection(collectionId);
      const currentCollection: CollectionDetails = { ...details, books: details.books ?? [] };
      set({ ...get(), currentCollection });
      return currentCollection;
    },

    async createCollection(name: string, description: string) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      const created = await api.createCollection(name, description);
      set({ ...get(), collections: [created, ...st.collections] });
      return created;
    },

    async deleteCollection(collectionId: number) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      ensureOwnCollection(collectionId);
      await api.deleteCollection(collectionId);
      const collections = st.collections.filter((c) => c.collectionId !== collectionId);
      const currentCollection = st.currentCollection?.collectionId === collectionId ? null : st.currentCollection;
      set({ ...get(), collections, currentCollection });
    },

    async updateCollectionDescription(collectionId: number, description: string) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      ensureOwnCollection(collectionId);
      const details = await api.updateCollectionDescription(collectionId, description);
      const collections = st.collections.map((c) =>
        c.collectionId === collectionId ? { ...c, description: details.description } : c
      );
      set({ ...get(), collections, currentCollection: details });
      return details;
    },

    async addCurrentBookToCollection(collectionId: number) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      if (!st.currentBook) throw new Error("Сначала откройте карточку книги.");
      ensureOwnCollection(collectionId);
      const details = await api.addBookToCollection(collectionId, st.currentBook.isbn);
      set({ ...get(), currentCollection: details });
      return details;
    },

    async addBookToCollection(collectionId: number, isbn: string) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      if (!isbn.trim()) throw new Error("Выберите книгу.");
      ensureOwnCollection(collectionId);
      const details = await api.addBookToCollection(collectionId, isbn.trim());
      set({ ...get(), currentCollection: details });
      return details;
    },

    async removeBookFromCollection(collectionId: number, isbn: string) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      ensureOwnCollection(collectionId);
      await api.removeBookFromCollection(collectionId, isbn);
      if (st.currentCollection?.collectionId === collectionId) {
        const books = st.currentCollection.books.filter((b) => b.isbn !== isbn);
        set({ ...get(), currentCollection: { ...st.currentCollection, books } });
      }
    },

    async updateUserRole(userId: number, role: string) {
      const st = get();
      if (!st.user) throw new Error("Требуется вход в систему.");
      const summary = await api.updateUserRole(userId, role);
      const nextUser =
        st.user.userId === summary.userId ? { ...st.user, role: summary.role } : st.user;
      if (st.token && nextUser) {
        storage.saveSession({ token: st.token, user: nextUser });
      }
      set({ ...get(), user: nextUser });
      return summary;
    },
  };
}
