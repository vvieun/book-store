import type { AuthResponse, Book, Collection, CollectionDetails, Order, Recommendation, Review, User, UserSummary, WishlistItem } from "../domain/model";

export type ApiGateway = {
  login(username: string, password: string): Promise<AuthResponse>;
  register(username: string, email: string, password: string): Promise<AuthResponse>;
  getCurrentUser(): Promise<User>;

  /** Только для ADMIN: смена роли пользователя */
  updateUserRole(userId: number, role: string): Promise<UserSummary>;

  getBooks(query: string, page?: number, size?: number): Promise<Book[]>;
  getBook(isbn: string): Promise<Book>;
  getBookReviews(isbn: string): Promise<Review[]>;
  createReview(isbn: string, rating: number, comment: string): Promise<void>;

  getRecommendations(count: number): Promise<Recommendation[]>;
  clearRecommendationsCache(): Promise<void>;

  createOrder(isbn: string, quantity: number): Promise<void>;
  getOrders(): Promise<Order[]>;
  getAllOrders(): Promise<Order[]>;
  updateOrderStatus(orderId: number, status: string): Promise<Order>;

  getAllReviewsForModeration(): Promise<Review[]>;
  deleteReview(reviewId: number): Promise<void>;

  getWishlist(): Promise<WishlistItem[]>;
  addToWishlist(isbn: string): Promise<WishlistItem>;
  removeFromWishlist(isbn: string): Promise<void>;

  getCollections(): Promise<Collection[]>;
  createCollection(name: string, description: string): Promise<Collection>;
  getCollection(collectionId: number): Promise<CollectionDetails>;
  updateCollectionDescription(collectionId: number, description: string): Promise<CollectionDetails>;
  deleteCollection(collectionId: number): Promise<void>;
  addBookToCollection(collectionId: number, isbn: string): Promise<CollectionDetails>;
  removeBookFromCollection(collectionId: number, isbn: string): Promise<void>;
};

export type LocalStorageGateway = {
  loadCart(): Array<{ isbn: string; title: string; price: number; quantity: number }>;
  saveCart(items: Array<{ isbn: string; title: string; price: number; quantity: number }>): void;
  loadSession(): { token: string; user: User } | null;
  saveSession(session: { token: string; user: User }): void;
  clearSession(): void;
};
