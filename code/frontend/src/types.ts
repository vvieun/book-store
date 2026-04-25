export type UserRole = 'CUSTOMER' | 'MODERATOR' | 'ADMIN';

export interface User {
  token: string;
  userId: number;
  username: string;
  email: string;
  role: UserRole;
}

export interface BookDTO {
  bookId: number;
  title: string;
  isbn: string;
  price: number;
  description: string;
  pages: number;
  publicationDate: string;
  avgRating: number;
  ratingCount: number;
  publisherName: string;
  authors: string[];
  categories: string[];
}

export interface Review {
  reviewId: number;
  bookId: number;
  bookTitle?: string;
  username?: string;
  rating: number;
  comment?: string;
  createdAt?: string;
}

export interface OrderItemDTO {
  orderItemId: number;
  bookId: number;
  bookTitle: string;
  quantity: number;
  price: number;
}

export interface OrderDTO {
  orderId: number;
  userId: number;
  totalAmount: number;
  status: string;
  createdAt?: string;
  items: OrderItemDTO[];
}

export type RecommendationType = 'COLLABORATIVE' | 'CONTENT_BASED' | 'HYBRID';

export interface BookRecommendation {
  book: BookDTO;
  score: number;
  type: RecommendationType;
  reason: string;
}

export interface ApiHealthStatus {
  service: string;
  status: string;
  version: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  email: string;
  role: UserRole;
}

export interface AdminUserDTO {
  userId: number;
  username: string;
  email: string;
  role: UserRole;
  createdAt: string;
}

export interface ModeratorOrderDTO extends OrderDTO {
  username?: string;
}

export interface CreateOrderItemRequest {
  bookId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  items: CreateOrderItemRequest[];
}

export interface CreateReviewRequest {
  bookId: number;
  rating: number;
  comment: string;
}
