import axios, { InternalAxiosRequestConfig } from 'axios';
import type {
  LoginPayload,
  RegisterPayload,
  CreateReviewRequest,
  CreateOrderRequest,
} from './types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      const hadToken = !!localStorage.getItem('token');
      ['token', 'userId', 'username', 'email', 'role'].forEach(k => localStorage.removeItem(k));
      if (hadToken) {
        window.dispatchEvent(new CustomEvent('auth:expired'));
      }
    }
    return Promise.reject(err);
  }
);

export const login = (data: LoginPayload) => api.post('/api/auth/login', data);
export const register = (data: RegisterPayload) => api.post('/api/auth/register', data);

export const getBook = (bookId: number | string) => api.get(`/api/books/${bookId}`);
export const getTopBooks = (count = 20) => api.get('/api/books/top-rated', { params: { count } });
export const searchBooks = (query: string, limit = 20) =>
  api.get('/api/books/search', { params: { query, limit } });
export const getBooksByCategory = (categoryId: number) =>
  api.get(`/api/books/category/${categoryId}`);

export const createReview = (data: CreateReviewRequest) =>
  api.post('/api/reviews', { bookId: data.bookId, rating: data.rating, comment: data.comment });
export const getReviewsByUser = (userId: number) => api.get(`/api/reviews/users/${userId}`);
export const getReviewsByBook = (bookId: number | string) =>
  api.get(`/api/reviews/books/${bookId}`);

export const createOrder = (data: CreateOrderRequest) => api.post('/api/orders', data);
export const getOrdersByUser = (userId: number) => api.get(`/api/orders/user/${userId}`);
export const getOrder = (orderId: number) => api.get(`/api/orders/${orderId}`);

export const getPopularRecommendations = (count = 20) =>
  api.get('/api/recommendations/popular', { params: { count } });
export const getUserRecommendations = (userId: number, count = 20) =>
  api.get(`/api/recommendations/users/${userId}`, { params: { count } });
export const invalidateCache = () => api.post('/api/recommendations/invalidate-cache');

export const healthCheck = () => api.get('/api/health');

export const getModeratorOrders = (status?: string) =>
  api.get('/api/moderator/orders', { params: status ? { status } : {} });
export const updateOrderStatus = (orderId: number, status: string) =>
  api.patch(`/api/moderator/orders/${orderId}/status`, { status });
export const getModeratorReviews = (bookId?: number) =>
  api.get('/api/moderator/reviews', { params: bookId ? { bookId } : {} });
export const deleteReviewModerator = (reviewId: number) =>
  api.delete(`/api/moderator/reviews/${reviewId}`);
export const getModeratorStats = () => api.get('/api/moderator/stats');

export const getAdminUsers = () => api.get('/api/admin/users');
export const changeUserRole = (userId: number, role: string) =>
  api.patch(`/api/admin/users/${userId}/role`, { role });
export const deleteAdminUser = (userId: number) =>
  api.delete(`/api/admin/users/${userId}`);

export const getAdminAuthors = () => api.get('/api/admin/authors');
export const createAuthor = (data: { name: string; biography?: string; country?: string }) =>
  api.post('/api/admin/authors', data);
export const updateAuthor = (id: number, data: { name?: string; biography?: string; country?: string }) =>
  api.put(`/api/admin/authors/${id}`, data);
export const deleteAuthor = (id: number) =>
  api.delete(`/api/admin/authors/${id}`);

export const getAdminCategories = () => api.get('/api/admin/categories');
export const createCategory = (data: { name: string; description?: string }) =>
  api.post('/api/admin/categories', data);
export const updateCategory = (id: number, data: { name?: string; description?: string }) =>
  api.put(`/api/admin/categories/${id}`, data);
export const deleteCategory = (id: number) =>
  api.delete(`/api/admin/categories/${id}`);

export const deleteAdminBook = (id: number) =>
  api.delete(`/api/admin/books/${id}`);
export const updateAdminBook = (id: number, data: object) =>
  api.put(`/api/admin/books/${id}`, data);

export const getAdminStats = () => api.get('/api/admin/stats');

export default api;
