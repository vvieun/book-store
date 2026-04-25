import React, { useState, FormEvent, ChangeEvent } from 'react';
import { createReview } from '../api';
import type { User } from '../types';
import './CreateRating.css';

interface Props {
  currentUser: User | null;
  onLoginRequired: () => void;
}

function CreateRating({ currentUser, onLoginRequired }: Props) {
  const [bookId,  setBookId]  = useState('');
  const [rating,  setRating]  = useState(5);
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  if (!currentUser) {
    return (
      <div className="rate-page">
        <div className="auth-gate-card">
          <div className="auth-gate-icon">⭐</div>
          <h2 style={{ fontWeight: 700, marginBottom: '0.5rem' }}>Оставить отзыв</h2>
          <p>Войдите в аккаунт, чтобы оценить книгу и поделиться впечатлениями.</p>
          <button className="submit-btn" style={{ maxWidth: '220px', margin: '0 auto' }} onClick={onLoginRequired}>
            Войти
          </button>
        </div>
      </div>
    );
  }

  const initials = currentUser.username.slice(0, 2).toUpperCase();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!bookId) { setError('Введите ID книги'); return; }
    try {
      setLoading(true);
      setError(null);
      setSuccess(null);
      await createReview({ bookId: parseInt(bookId, 10), rating, comment });
      setSuccess(`Отзыв опубликован! Оценка: ${rating}/5`);
      setBookId('');
      setComment('');
      setRating(5);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
        ?? (err instanceof Error ? err.message : 'Неизвестная ошибка');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rate-page">
      <h1>Оценить книгу</h1>
      <p className="page-sub">Ваш отзыв помогает другим читателям сделать выбор</p>

      <div className="rate-card">
        {/* Current user */}
        <div className="user-info-row">
          <div className="user-avatar-lg">{initials}</div>
          <span>Вы вошли как <strong>{currentUser.username}</strong></span>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-field">
            <label className="form-label">ID книги</label>
            <input
              className="form-input-rate"
              type="number"
              min="1"
              placeholder="Например: 1, 42, 100..."
              value={bookId}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setBookId(e.target.value)}
              required
            />
          </div>

          <div className="form-field">
            <label className="form-label">Оценка</label>
            <div className="star-row">
              {[1,2,3,4,5].map(s => (
                <button
                  key={s}
                  type="button"
                  className={`star-btn${s <= rating ? ' on' : ''}`}
                  onClick={() => setRating(s)}
                >★</button>
              ))}
              <span className="star-label">{rating} из 5</span>
            </div>
          </div>

          <div className="form-field">
            <label className="form-label">Комментарий <span style={{ fontWeight: 400, textTransform: 'none', letterSpacing: 0 }}>(необязательно)</span></label>
            <textarea
              className="form-input-rate"
              rows={4}
              placeholder="Поделитесь впечатлениями о книге..."
              value={comment}
              onChange={(e: ChangeEvent<HTMLTextAreaElement>) => setComment(e.target.value)}
              style={{ resize: 'vertical', minHeight: '90px' }}
            />
          </div>

          {error   && <div className="alert alert-error"   style={{ marginBottom: '0.75rem' }}>{error}</div>}
          {success && <div className="alert alert-success" style={{ marginBottom: '0.75rem' }}>{success}</div>}

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? 'Публикация...' : 'Опубликовать отзыв'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default CreateRating;
