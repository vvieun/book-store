import type { ApiGateway } from "../application/ports";
import type { AuthResponse, Book, Collection, CollectionDetails, Order, Recommendation, Review, User, UserSummary, WishlistItem } from "../domain/model";

function fallbackMessage(status: number): string {
  if (status === 400) return "Проверьте корректность данных.";
  if (status === 401) return "Требуется вход в систему.";
  if (status === 403) return "Недостаточно прав.";
  if (status === 409) return "Конфликт данных.";
  if (status === 404) return "Данные не найдены.";
  return "Не удалось выполнить действие.";
}

async function request<T>(path: string, getToken: () => string, method = "GET", body: unknown = null): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) headers["X-Auth-Token"] = token;

  const res = await fetch(path, { method, headers, body: body ? JSON.stringify(body) : null });
  if (!res.ok) {
    let message = "";
    try {
      const payload = await res.json();
      message = payload?.message ? String(payload.message) : "";
    } catch {
      message = "";
    }
    throw new Error(message || fallbackMessage(res.status));
  }
  if (res.status === 204) return null as T;
  return (await res.json()) as T;
}

export function createHttpApi(getToken: () => string): ApiGateway {
  return {
    login: (username, password) => request<AuthResponse>("/api/auth/login", getToken, "POST", { username, password }),
    register: (username, email, password) =>
      request<AuthResponse>("/api/auth/register", getToken, "POST", { username, email, password }),
    getCurrentUser: () => request<User>("/api/auth/me", getToken),

    updateUserRole: (userId, role) =>
      request<UserSummary>(`/api/users/${userId}/role`, getToken, "PATCH", { role }),

    getBooks: (query, page = 0, size = 20) => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(size),
      });
      if (query) params.set("query", query);
      const path = `/api/books?${params.toString()}`;
      return request<Book[]>(path, getToken);
    },
    getBook: (isbn) => request<Book>(`/api/books/${encodeURIComponent(isbn)}`, getToken),
    getBookReviews: (isbn) => request<Review[]>(`/api/books/${encodeURIComponent(isbn)}/reviews`, getToken),
    createReview: (isbn, rating, comment) => request<void>("/api/reviews", getToken, "POST", { isbn, rating, comment }),

    getRecommendations: (count) => request<Recommendation[]>(`/api/recommendations?count=${count}`, getToken),
    clearRecommendationsCache: () => request<void>("/api/recommendations/cache/clear", getToken, "POST"),

    createOrder: (isbn, quantity) => request<void>("/api/orders", getToken, "POST", { isbn, quantity }),
    getOrders: () => request<Order[]>("/api/orders/my", getToken),
    getAllOrders: () => request<Order[]>("/api/orders", getToken),
    updateOrderStatus: (orderId, status) =>
      request<Order>(`/api/orders/${orderId}/status`, getToken, "PATCH", { status }),

    getAllReviewsForModeration: () => request<Review[]>("/api/reviews", getToken),
    deleteReview: (reviewId) => request<void>(`/api/reviews/${reviewId}`, getToken, "DELETE"),

    getWishlist: () => request<WishlistItem[]>("/api/wishlist", getToken),
    addToWishlist: (isbn) => request<WishlistItem>("/api/wishlist", getToken, "POST", { isbn }),
    removeFromWishlist: (isbn) => request<void>(`/api/wishlist/${encodeURIComponent(isbn)}`, getToken, "DELETE"),

    getCollections: () => request<Collection[]>("/api/collections", getToken),
    createCollection: (name, description) => request<Collection>("/api/collections", getToken, "POST", { name, description }),
    getCollection: (collectionId) => request<CollectionDetails>(`/api/collections/${collectionId}`, getToken),
    updateCollectionDescription: (collectionId, description) =>
      request<CollectionDetails>(`/api/collections/${collectionId}/description`, getToken, "PATCH", { description }),
    deleteCollection: (collectionId) => request<void>(`/api/collections/${collectionId}`, getToken, "DELETE"),
    addBookToCollection: (collectionId, isbn) =>
      request<CollectionDetails>(`/api/collections/${collectionId}/books`, getToken, "POST", { isbn }),
    removeBookFromCollection: (collectionId, isbn) =>
      request<void>(`/api/collections/${collectionId}/books/${encodeURIComponent(isbn)}`, getToken, "DELETE"),
  };
}
