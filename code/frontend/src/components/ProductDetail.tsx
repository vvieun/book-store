import React, { useState, useEffect, FormEvent } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getBook, getReviewsByBook, createReview, createOrder } from '../api';
import type { BookDTO, Review, User } from '../types';
import './ProductDetail.css';

interface Props {
  currentUser: User | null;
  onLoginRequired: () => void;
}

function coverClass(id: number) { return `cover-${id % 8}`; }

function starsEl(rating: number) {
  return Array.from({ length: 5 }, (_, i) => (
    <span key={i} style={{ color: i < rating ? '#f59e0b' : '#d1d5db' }}>★</span>
  ));
}

function ProductDetail({ currentUser, onLoginRequired }: Props) {
  const { id } = useParams<{ id: string }>();

  const [book,         setBook]         = useState<BookDTO | null>(null);
  const [reviews,      setReviews]      = useState<Review[]>([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState<string | null>(null);
  const [reviewText,   setReviewText]   = useState('');
  const [reviewRating, setReviewRating] = useState(5);
  const [submitting,   setSubmitting]   = useState(false);
  const [reviewMsg,    setReviewMsg]    = useState<{ ok: boolean; text: string } | null>(null);
  const [ordering,     setOrdering]     = useState(false);
  const [orderMsg,     setOrderMsg]     = useState<{ ok: boolean; text: string } | null>(null);

  useEffect(() => { loadData(); }, [id]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [bookRes, reviewsRes] = await Promise.all([getBook(id!), getReviewsByBook(id!)]);
      setBook(bookRes.data);
      setReviews(reviewsRes.data ?? []);
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      if (status === 401 || status === 403) {
        setError('Сессия истекла. Войдите в аккаунт снова.');
      } else {
        setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message
          ?? (err instanceof Error ? err.message : 'Не удалось загрузить книгу'));
      }
    } finally {
      setLoading(false);
    }
  };

  const handleOrder = async () => {
    if (!currentUser) { onLoginRequired(); return; }
    try {
      setOrdering(true);
      setOrderMsg(null);
      await createOrder({ items: [{ bookId: parseInt(id!, 10), quantity: 1 }] });
      setOrderMsg({ ok: true, text: 'Заказ оформлен! Скоро с вами свяжутся.' });
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      const msg = (status === 401 || status === 403)
        ? 'Войдите в аккаунт для оформления заказа'
        : (err as { response?: { data?: { message?: string } } })?.response?.data?.message
          ?? (err instanceof Error ? err.message : 'Ошибка');
      setOrderMsg({ ok: false, text: msg });
    } finally {
      setOrdering(false);
    }
  };

  const handleReviewSubmit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setReviewMsg(null);
      await createReview({ bookId: parseInt(id!, 10), rating: reviewRating, comment: reviewText });
      setReviewMsg({ ok: true, text: 'Отзыв опубликован!' });
      setReviewText('');
      setReviewRating(5);
      setTimeout(() => { loadData(); setReviewMsg(null); }, 1200);
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      const msg = (status === 401 || status === 403)
        ? 'Войдите в аккаунт чтобы оставить отзыв'
        : (err as { response?: { data?: { message?: string } } })?.response?.data?.message
          ?? (err instanceof Error ? err.message : 'Ошибка');
      setReviewMsg({ ok: false, text: msg });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="loading">Загрузка...</div>;
  if (error)   return <div className="alert alert-error" style={{ margin: '2rem 0' }}>{error}</div>;
  if (!book)   return <div className="alert alert-error" style={{ margin: '2rem 0' }}>Книга не найдена</div>;

  const avgStars = Math.round(book.avgRating ?? 0);

  return (
    <div>
      {/* Back */}
      <div style={{ marginBottom: '1.25rem' }}>
        <Link to="/" style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
          ← Каталог
        </Link>
      </div>

      <div className="book-page">
        {/* ── Left: cover + buy ─────────────────────────────────── */}
        <div className="book-aside">
          <div className={`book-cover-lg ${coverClass(book.bookId)}`}>📖</div>

          <p className="book-price-lg">{book.price?.toLocaleString('ru-RU')} ₽</p>

          {orderMsg && (
            <div className={`alert ${orderMsg.ok ? 'alert-success' : 'alert-error'}`} style={{ marginBottom: '0.75rem', fontSize: '0.85rem' }}>
              {orderMsg.text}
            </div>
          )}

          {currentUser ? (
            <button className="buy-btn" onClick={handleOrder} disabled={ordering}>
              {ordering ? 'Оформление...' : 'В корзину'}
            </button>
          ) : (
            <button className="buy-btn secondary" onClick={onLoginRequired}>
              Войдите для покупки
            </button>
          )}
        </div>

        {/* ── Right: info ───────────────────────────────────────── */}
        <div className="book-info">
          {book.categories?.length > 0 && (
            <div className="book-tags">
              {book.categories.map(c => <span key={c} className="book-tag">{c}</span>)}
            </div>
          )}

          <h1 className="book-page-title">{book.title}</h1>

          {book.authors?.length > 0 && (
            <p className="book-authors-lg">✍ {book.authors.join(', ')}</p>
          )}

          {book.publisherName && (
            <p className="book-publisher">Издательство: {book.publisherName}</p>
          )}

          {/* Rating */}
          <div className="rating-bar">
            <span className="rating-big">{book.avgRating?.toFixed(1) ?? '—'}</span>
            <div>
              <div className="stars-row">
                {Array.from({ length: 5 }, (_, i) => (
                  <span key={i}>{i < avgStars ? '★' : '☆'}</span>
                ))}
              </div>
              <div className="rating-total">{book.ratingCount ?? 0} отзывов</div>
            </div>
          </div>

          {/* Meta */}
          <table className="meta-table">
            <tbody>
              {book.isbn           && <tr><td>ISBN</td><td>{book.isbn}</td></tr>}
              {book.publicationDate && <tr><td>Год издания</td><td>{new Date(book.publicationDate).getFullYear()}</td></tr>}
              {book.pages          && <tr><td>Страниц</td><td>{book.pages}</td></tr>}
            </tbody>
          </table>

          {/* Description */}
          {book.description && (
            <div className="book-description-block">
              <div className="section-title">Описание</div>
              <p className="book-description-text">{book.description}</p>
            </div>
          )}

          {/* Reviews */}
          <div className="reviews-block">
            <div className="section-title">Отзывы ({reviews.length})</div>

            {/* Review form */}
            {currentUser ? (
              <form onSubmit={handleReviewSubmit} className="review-form-card">
                <h4>Оставить отзыв</h4>
                <div className="star-row">
                  {[1,2,3,4,5].map(s => (
                    <button key={s} type="button"
                      className={`star-btn${s <= reviewRating ? ' on' : ''}`}
                      onClick={() => setReviewRating(s)}>★</button>
                  ))}
                  <span className="star-label">{reviewRating} из 5</span>
                </div>
                <textarea
                  className="review-textarea"
                  rows={3}
                  placeholder="Поделитесь впечатлениями о книге..."
                  value={reviewText}
                  onChange={e => setReviewText(e.target.value)}
                />
                {reviewMsg && (
                  <div className={`alert ${reviewMsg.ok ? 'alert-success' : 'alert-error'}`} style={{ marginTop: '0.5rem', fontSize: '0.85rem' }}>
                    {reviewMsg.text}
                  </div>
                )}
                <button
                  type="submit"
                  className="btn btn-primary"
                  style={{ marginTop: '0.75rem', width: '100%' }}
                  disabled={submitting}
                >
                  {submitting ? 'Публикация...' : 'Опубликовать отзыв'}
                </button>
              </form>
            ) : (
              <div className="login-prompt">
                <span>Войдите, чтобы оставить отзыв</span>
                <button onClick={onLoginRequired}>Войти</button>
              </div>
            )}

            {/* Reviews list */}
            {reviews.length === 0 ? (
              <div className="empty-reviews">Отзывов пока нет. Будьте первым!</div>
            ) : (
              <div className="reviews-list">
                {reviews.map(r => (
                  <div key={r.reviewId} className="review-card">
                    <div className="review-header">
                      <span className="reviewer-name">{r.username ?? 'Покупатель'}</span>
                      <div className="review-stars">{starsEl(r.rating)}</div>
                    </div>
                    {r.createdAt && (
                      <div className="review-date">
                        {new Date(r.createdAt).toLocaleDateString('ru-RU', { day:'numeric', month:'long', year:'numeric' })}
                      </div>
                    )}
                    {r.comment && <p className="review-text">{r.comment}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetail;
