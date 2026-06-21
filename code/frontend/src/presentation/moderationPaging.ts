export const LIST_PAGE_SIZE = 10;

export function filterByNumericId<T>(
  items: T[],
  query: string,
  getId: (item: T) => number
): T[] {
  const trimmed = query.trim();
  if (!trimmed) return items;
  const id = Number(trimmed);
  if (!Number.isFinite(id)) return [];
  return items.filter((item) => getId(item) === id);
}

export function paginateSlice<T>(items: T[], page: number, pageSize: number): {
  items: T[];
  page: number;
  totalPages: number;
  total: number;
} {
  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));
  const safePage = Math.min(Math.max(0, page), totalPages - 1);
  const start = safePage * pageSize;
  return {
    items: items.slice(start, start + pageSize),
    page: safePage,
    totalPages,
    total,
  };
}
