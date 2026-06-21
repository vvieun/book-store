export type AuthMode = "login" | "register";

export type User = {
  userId: number;
  username: string;
  email: string;
  role: string;
};

export type AuthResponse = {
  token: string;
  user: User;
};

/** Ответ PATCH /api/users/{id}/role (без пароля) */
export type UserSummary = {
  userId: number;
  username: string;
  email: string;
  role: string;
  createdAt?: string | null;
};

export type Book = {
  isbn: string;
  title: string;
  description: string | null;
  price: number;
  avgRating: number | null;
  ratingCount?: number | null;
};

export type Review = {
  reviewId: number;
  userId?: number | null;
  username: string | null;
  isbn: string;
  rating: number;
  comment: string | null;
};

export type Recommendation = {
  isbn: string;
  title: string;
  score: number | null;
  reason?: string | null;
  type?: string | null;
};

export type Order = {
  orderId: number;
  status: string;
  totalAmount: number;
  buyerUserId?: number | null;
  buyerUsername?: string | null;
  createdAt?: string | null;
  items?: Array<{ isbn: string; title: string | null; quantity: number; price: number }>;
};

export type CartItem = {
  isbn: string;
  title: string;
  price: number;
  quantity: number;
};

export type WishlistItem = {
  isbn: string;
  title: string;
};

export type Collection = {
  collectionId: number;
  ownerUserId: number | null;
  ownerUsername?: string | null;
  name: string;
  description: string | null;
};

export type CollectionBookItem = {
  isbn: string;
  title: string;
};

export type CollectionDetails = {
  collectionId: number;
  ownerUserId: number | null;
  ownerUsername?: string | null;
  name: string;
  description: string | null;
  books: CollectionBookItem[];
};
