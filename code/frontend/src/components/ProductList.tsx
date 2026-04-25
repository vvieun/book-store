import React, { useState, useEffect, useCallback, FormEvent, ChangeEvent } from 'react';
import { Link } from 'react-router-dom';
import { getTopBooks, getBooksByCategory, searchBooks } from '../api';
import type { BookDTO } from '../types';
import './ProductList.css';

interface Category { id: number; name: string; }

const CATEGORIES: Category[] = [
  { id: 13, name: 'Классическая литература' },
  { id: 14, name: 'Фантастика' },
  { id: 15, name: 'Детективы' },
  { id: 16, name: 'Романы' },
  { id: 17, name: 'Исторические книги' },
  { id: 18, name: 'Научная фантастика' },
  { id: 19, name: 'Приключения' },
  { id: 20, name: 'Биографии' },
  { id: 21, name: 'Поэзия' },
  { id: 22, name: 'Философия' },
  { id: 23, name: 'Психология' },
  { id: 24, name: 'Бизнес и экономика' },
];

type ViewMode = 'top' | 'category' | 'search';

function coverClass(id: number) {
  return `cover-${id % 8}`;
}

function ProductList() {
  const [books,       setBooks]       = useState<BookDTO[]>([]);
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState<string | null>(null);
  const [viewMode,    setViewMode]    = useState<ViewMode>('top');
  const [categoryId,  setCategoryId]  = useState<number>(CATEGORIES[0].id);
  const [query,       setQuery]       = useState('');
  const [searchInput, setSearchInput] = useState('');

  const loadBooks = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      let response;
      if      (viewMode === 'top')      response = await getTopBooks(40);
      else if (viewMode === 'category') response = await getBooksByCategory(categoryId);
      else                              response = await searchBooks(query || 'книга', 40);
      setBooks(response.data ?? []);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
        ?? (err instanceof Error ? err.message : 'Неизвестная ошибка');
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [viewMode, categoryId, query]);

  useEffect(() => { loadBooks(); }, [loadBooks]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setQuery(searchInput);
    setViewMode('search');
  };

  const subtitle =
    viewMode === 'top'      ? `${books.length} книг по рейтингу` :
    viewMode === 'category' ? CATEGORIES.find(c => c.id === categoryId)?.name :
                              `Результаты поиска: «${query}»`;

  return (
    <div>
      {/* Header */}
      <div className="catalog-header">
        <div>
          <h1 className="catalog-title">Каталог книг</h1>
          {!loading && !error && (
            <p className="catalog-subtitle">{subtitle}</p>
          )}
        </div>
      </div>

      {/* Toolbar */}
      <div className="catalog-toolbar">
        <div className="tab-group">
          <button className={`tab-btn${viewMode === 'top'      ? ' active' : ''}`} onClick={() => setViewMode('top')}>
            Топ рейтинга
          </button>
          <button className={`tab-btn${viewMode === 'category' ? ' active' : ''}`} onClick={() => setViewMode('category')}>
            По жанру
          </button>
        </div>

        {viewMode === 'category' && (
          <select
            className="catalog-select"
            value={categoryId}
            onChange={(e: ChangeEvent<HTMLSelectElement>) => setCategoryId(parseInt(e.target.value, 10))}
          >
            {CATEGORIES.map(c => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        )}

        <form onSubmit={handleSearch} className="search-form">
          <input
            type="text"
            className="search-input"
            placeholder="Поиск по названию или автору..."
            value={searchInput}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchInput(e.target.value)}
          />
          <button type="submit" className="search-btn">Найти</button>
        </form>
      </div>

      {/* Content */}
      {loading && <div className="loading">Загрузка каталога...</div>}

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>{error}</div>
      )}

      {!loading && !error && (
        <div className="books-grid">
          {books.length === 0 ? (
            <div className="empty-state">
              <div style={{ fontSize: '2.5rem' }}>📭</div>
              <p>Книги не найдены</p>
            </div>
          ) : (
            books.map(book => (
              <Link to={`/product/${book.bookId}`} key={book.bookId} className="book-card">
                <div className={`book-cover ${coverClass(book.bookId)}`}>
                  📖
                </div>
                <div className="book-body">
                  {book.categories?.length > 0 && (
                    <div className="book-categories">
                      {book.categories.slice(0, 2).map(c => (
                        <span key={c} className="category-chip">{c}</span>
                      ))}
                    </div>
                  )}
                  <div className="book-title">{book.title}</div>
                  {book.authors?.length > 0 && (
                    <div className="book-authors">{book.authors.join(', ')}</div>
                  )}
                  {book.description && (
                    <div className="book-description">{book.description}</div>
                  )}
                  <div className="book-footer">
                    <span className="book-price">{book.price?.toLocaleString('ru-RU')} ₽</span>
                    <div className="book-rating">
                      <span className="rating-star">★</span>
                      <span className="rating-score">{book.avgRating?.toFixed(1) ?? '—'}</span>
                      <span className="rating-count">({book.ratingCount ?? 0})</span>
                    </div>
                  </div>
                </div>
              </Link>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default ProductList;
