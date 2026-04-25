import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getUserRecommendations, getPopularRecommendations, invalidateCache } from '../api';
import type { BookRecommendation, User } from '../types';
import './Recommendations.css';

interface Props {
  currentUser: User | null;
  onLoginRequired: () => void;
}

function coverClass(id: number) { return `cover-${id % 8}`; }

function badgeClass(type: string) {
  if (type === 'COLLABORATIVE') return 'rec-badge badge-cf';
  if (type === 'CONTENT_BASED') return 'rec-badge badge-cb';
  return 'rec-badge badge-pop';
}

function badgeLabel(type: string) {
  if (type === 'COLLABORATIVE') return 'CF';
  if (type === 'CONTENT_BASED') return 'CB';
  return 'Топ';
}

function Recommendations({ currentUser, onLoginRequired }: Props) {
  const [recs,       setRecs]       = useState<BookRecommendation[]>([]);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const isAuthed = !!currentUser;

  useEffect(() => { loadRecs(); }, [currentUser]);

  const loadRecs = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = isAuthed
        ? await getUserRecommendations(currentUser!.userId, 20)
        : await getPopularRecommendations(20);
      setRecs(res.data ?? []);
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      if (status === 401 || status === 403) {
        setError('Сессия истекла. Войдите в аккаунт снова.');
      } else {
        const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
          ?? (err instanceof Error ? err.message : 'Не удалось загрузить рекомендации');
        setError(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    if (!isAuthed) { await loadRecs(); return; }
    try {
      setRefreshing(true);
      setError(null);
      setSuccessMsg(null);
      await invalidateCache();
      await loadRecs();
      setSuccessMsg('Рекомендации обновлены!');
      setTimeout(() => setSuccessMsg(null), 3000);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
        ?? (err instanceof Error ? err.message : 'Неизвестная ошибка');
      setError(msg);
    } finally {
      setRefreshing(false);
    }
  };

  return (
    <div>
      {/* Header */}
      <div className="rec-page-header">
        <div>
          <h1 className="rec-page-title">
            {isAuthed ? `Рекомендации для вас` : 'Популярные книги'}
          </h1>
          <p className="rec-page-subtitle">
            {isAuthed
              ? 'Подобраны на основе ваших оценок и предпочтений'
              : 'Самые популярные книги среди наших читателей'}
          </p>
        </div>
        <button className="refresh-btn" onClick={handleRefresh} disabled={refreshing || loading}>
          {refreshing ? 'Обновление...' : '↻ Обновить'}
        </button>
      </div>

      {/* Guest banner */}
      {!isAuthed && (
        <div className="guest-banner">
          <span>Войдите, чтобы получить персональные рекомендации на основе ваших оценок.</span>
          <button onClick={onLoginRequired}>Войти</button>
        </div>
      )}

      {successMsg && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>{successMsg}</div>
      )}
      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>{error}</div>
      )}

      {loading && <div className="loading">Загрузка рекомендаций...</div>}

      {!loading && !error && (
        <div className="rec-grid">
          {recs.length === 0 ? (
            <div className="rec-empty">
              <div style={{ fontSize: '2.5rem' }}>🔍</div>
              <p style={{ marginTop: '0.5rem' }}>Рекомендации не найдены</p>
            </div>
          ) : (
            recs.map((rec, idx) => {
              const book = rec.book;
              if (!book) return null;
              return (
                <Link to={`/product/${book.bookId}`} key={idx} className="rec-card">
                  <div className={`rec-cover ${coverClass(book.bookId)}`}>
                    📖
                    <span className={badgeClass(rec.type)}>{badgeLabel(rec.type)}</span>
                  </div>
                  <div className="rec-body">
                    {book.categories?.length > 0 && (
                      <div className="rec-cats">
                        {book.categories.slice(0, 2).map(c => (
                          <span key={c} className="rec-cat-chip">{c}</span>
                        ))}
                      </div>
                    )}
                    <div className="rec-title">{book.title}</div>
                    {book.authors?.length > 0 && (
                      <div className="rec-authors">{book.authors.join(', ')}</div>
                    )}
                    {rec.reason && <div className="rec-reason">{rec.reason}</div>}
                    <div className="rec-footer">
                      <span className="rec-price">{book.price?.toLocaleString('ru-RU')} ₽</span>
                      <div className="rec-rating">
                        <span className="rec-star">★</span>
                        <span className="rec-score">{book.avgRating?.toFixed(1) ?? '—'}</span>
                        <span className="rec-cnt">({book.ratingCount ?? 0})</span>
                      </div>
                    </div>
                  </div>
                </Link>
              );
            })
          )}
        </div>
      )}
    </div>
  );
}

export default Recommendations;
