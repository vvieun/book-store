import React, { useState, useEffect } from 'react';
import { getReviewsByUser, getOrdersByUser } from '../api';
import type { Review, OrderDTO, User } from '../types';
import './UserStatistics.css';

interface Props {
  currentUser: User | null;
  onLoginRequired: () => void;
}

function statusLabel(s: string) {
  if (s === 'DELIVERED')  return { label: 'Доставлен',   cls: 'status-delivered'  };
  if (s === 'PENDING')    return { label: 'Ожидает',      cls: 'status-pending'    };
  if (s === 'PROCESSING') return { label: 'В обработке', cls: 'status-processing' };
  if (s === 'CANCELLED')  return { label: 'Отменён',      cls: 'status-cancelled'  };
  return { label: s, cls: 'status-pending' };
}

function starsEl(n: number) {
  return Array.from({ length: 5 }, (_, i) => (
    <span key={i} style={{ color: i < n ? '#f59e0b' : '#d1d5db' }}>★</span>
  ));
}

function UserStatistics({ currentUser, onLoginRequired }: Props) {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [orders,  setOrders]  = useState<OrderDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState<string | null>(null);

  useEffect(() => {
    if (currentUser) loadData();
    else setLoading(false);
  }, [currentUser]);

  const loadData = async () => {
    if (!currentUser) return;
    try {
      setLoading(true);
      setError(null);
      const [rRes, oRes] = await Promise.all([
        getReviewsByUser(currentUser.userId),
        getOrdersByUser(currentUser.userId),
      ]);
      setReviews(rRes.data ?? []);
      setOrders(oRes.data ?? []);
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      if (status === 401 || status === 403) {
        setError('Сессия истекла. Войдите в аккаунт снова.');
      } else {
        const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
          ?? (err instanceof Error ? err.message : 'Не удалось загрузить данные');
        setError(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  if (!currentUser) {
    return (
      <div className="stats-page">
        <div className="auth-gate-stats">
          <div style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>📊</div>
          <h2 style={{ fontWeight: 700, marginBottom: '0.5rem' }}>Личная статистика</h2>
          <p>Войдите в аккаунт, чтобы увидеть историю заказов и отзывов.</p>
          <button className="submit-btn" style={{ maxWidth: '220px', margin: '0 auto' }} onClick={onLoginRequired}>
            Войти
          </button>
        </div>
      </div>
    );
  }

  if (loading) return <div className="loading">Загрузка статистики...</div>;
  if (error)   return <div className="alert alert-error" style={{ margin: '2rem 0' }}>{error}</div>;

  const avgRating  = reviews.length
    ? (reviews.reduce((s, r) => s + r.rating, 0) / reviews.length).toFixed(1) : '—';
  const delivered  = orders.filter(o => o.status === 'DELIVERED');
  const totalSpent = delivered.reduce((s, o) => s + (o.totalAmount ?? 0), 0).toFixed(0);
  const initials   = currentUser.username.slice(0, 2).toUpperCase();

  const kpis = [
    { icon: '📝', value: reviews.length,                                    label: 'Отзывов'    },
    { icon: '⭐', value: avgRating,                                          label: 'Ср. оценка' },
    { icon: '🛒', value: orders.length,                                      label: 'Заказов'    },
    { icon: '✅', value: delivered.length,                                   label: 'Доставлено' },
    { icon: '💰', value: `${parseInt(totalSpent).toLocaleString('ru-RU')} ₽`, label: 'Потрачено' },
    { icon: '⏳', value: orders.filter(o => o.status === 'PENDING').length,  label: 'Ожидают'   },
  ];

  return (
    <div className="stats-page">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.25rem' }}>
        <div style={{
          width: '40px', height: '40px', borderRadius: '50%',
          background: 'var(--color-primary)', color: 'white',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '0.95rem', fontWeight: 700,
        }}>{initials}</div>
        <h1 style={{ margin: 0 }}>{currentUser.username}</h1>
      </div>
      <p className="page-sub">{currentUser.email}</p>

      {/* KPI */}
      <div className="kpi-grid">
        {kpis.map(k => (
          <div key={k.label} className="kpi-card">
            <div className="kpi-icon">{k.icon}</div>
            <div className="kpi-value">{k.value}</div>
            <div className="kpi-label">{k.label}</div>
          </div>
        ))}
      </div>

      {/* Reviews */}
      <div className="stats-section">
        <div className="section-head">Последние отзывы</div>
        {reviews.length === 0 ? (
          <div className="empty-row">Вы ещё не оставляли отзывов</div>
        ) : reviews.slice(0, 10).map(r => (
          <div key={r.reviewId} className="review-row">
            <div>
              <div className="review-row-title">{r.bookTitle ?? `Книга #${r.bookId}`}</div>
              {r.createdAt && (
                <div className="review-row-date">
                  {new Date(r.createdAt).toLocaleDateString('ru-RU', { day:'numeric', month:'long', year:'numeric' })}
                </div>
              )}
              {r.comment && <div className="review-row-comment">{r.comment}</div>}
            </div>
            <div className="review-stars-sm">{starsEl(r.rating)}</div>
          </div>
        ))}
      </div>

      {/* Orders */}
      <div className="stats-section">
        <div className="section-head">История заказов</div>
        {orders.length === 0 ? (
          <div className="empty-row">Заказов пока нет</div>
        ) : orders.slice(0, 8).map(o => {
          const { label, cls } = statusLabel(o.status);
          return (
            <div key={o.orderId} className="order-row">
              <div>
                <div className="order-row-id">Заказ #{o.orderId}</div>
                {o.createdAt && (
                  <div className="order-row-date">
                    {new Date(o.createdAt).toLocaleDateString('ru-RU', { day:'numeric', month:'long', year:'numeric' })}
                  </div>
                )}
              </div>
              <div className="order-right">
                <span className={`status-badge ${cls}`}>{label}</span>
                <span className="order-amount">{o.totalAmount?.toLocaleString('ru-RU')} ₽</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default UserStatistics;
