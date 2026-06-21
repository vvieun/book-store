import type { LocalStorageGateway } from "../application/ports";
import type { CartItem, User } from "../domain/model";

export function createBrowserStorage(): LocalStorageGateway {
  const cartKey = "bookstore_cart";
  const sessionKey = "bookstore_session";
  return {
    loadCart(): CartItem[] {
      try {
        return JSON.parse(localStorage.getItem(cartKey) || "[]");
      } catch {
        return [];
      }
    },
    saveCart(items: CartItem[]): void {
      localStorage.setItem(cartKey, JSON.stringify(items));
    },
    loadSession(): { token: string; user: User } | null {
      try {
        const value = JSON.parse(localStorage.getItem(sessionKey) || "null");
        if (!value?.token || !value?.user) return null;
        return value;
      } catch {
        return null;
      }
    },
    saveSession(session: { token: string; user: User }): void {
      localStorage.setItem(sessionKey, JSON.stringify(session));
    },
    clearSession(): void {
      localStorage.removeItem(sessionKey);
    },
  };
}
