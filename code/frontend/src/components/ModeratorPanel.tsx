import React, { useState, useEffect, useCallback } from 'react';
import type { User, OrderDTO, Review } from '../types';
import {
  getModeratorOrders, updateOrderStatus,
  getModeratorReviews, deleteReviewModerator,
  getModeratorStats,
} from '../api';
import './ModeratorPanel.css';

interface Props {
  currentUser: User | null;
  onLoginRequired: () => void;
}

type Tab = 'orders' | 'reviews' | 'stats';

const STATUS_LABELS: Record<string, string> = {
  PENDING:    'Ожидание',
  PROCESSING: 'В обработке',
  SHIPPED:    'Отправлен',
  DELIVERED:  'Доставлен',
  CANCELLED:  'Отменён',
};

const STATUS_NEXT: Record<string, string[]> = {
  PENDING:    ['PROCESSING', 'CANCELLED'],
  PROCESSING: ['SHIPPED',    'CANCELLED'],
  SHIPPED:    ['DELIVERED',  'CANCELLED'],
  DELIVERED:  [],
  CANCELLED:  [],
};

export default function ModeratorPanel({ currentUser, onLoginRequired }: Props) {
  const [tab, setTab]         = useState<Tab>('orders');
  const [orders, setOrders]   = useState<OrderDTO[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [stats, setStats]     = useState<Record<string, number> | null>(null);
  const [filterStatus, setFilterStatus] = useState('');
  const [loading, setLoading] = useState(false);
  const [msg, setMsg]         = useState('');

  const isAuthorized = currentUser?.role === 'MODERATOR' || currentUser?.role === 'ADMIN';

  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const r = await getModeratorOrders(filterStatus || undefined);
      setOrders(r.data);
    } catch { /* ignore */ }
    setLoading(false);
  }, [filterStatus]);

  const loadReviews = useCallback(async () => {
    setLoading(true);
    try {
      const r = await getModeratorReviews();
      setReviews(r.data);
    } catch { /* ignore */ }
    setLoading(false);
  }, []);

  const loadStats = useCallback(async () => {
    setLoading(true);
    try {
      const r = await getModeratorStats();
      setStats(r.data);
    } catch { /* ignore */ }
    setLoading(false);
  }, []);

  useEffect(() => {
    if (!isAuthorized) return;
    if (tab === 'orders')  loadOrders();
    if (tab === 'reviews') loadReviews();
    if (tab === 'stats')   loadStats();
  }, [tab, isAuthorized, loadOrders, loadReviews, loadStats]);

  const handleStatusChange = async (orderId: number, newStatus: string) => {
    try {
      await updateOrderStatus(orderId, newStatus);
      setMsg(`Статус заказа #${orderId} изменён на ${STATUS_LABELS[newStatus]}`);
      loadOrders();
    } catch {
      setMsg('Ошибка при смене статуса');
    }
  };

  const handleDeleteReview = async (reviewId: number) => {
    if (!window.confirm('Удалить этот отзыв?')) return;
    try {
      await deleteReviewModerator(reviewId);
      setMsg('Отзыв удалён');
      loadReviews();
    } catch {
      setMsg('Ошибка при удалении');
    }
  };

  if (!currentUser) {
    return (
      <div className="mod-gate">
        <div className="mod-gate-icon">🔒</div>
        <h2>Требуется авторизация</h2>
        <p>Войдите как модератор или администратор</p>
        <button className="btn btn-primary" onClick={onLoginRequired}>Войти</button>
      </div>
    );
  }

  if (!isAuthorized) {
    return (
      <div className="mod-gate">
        <div className="mod-gate-icon">⛔</div>
        <h2>Недостаточно прав</h2>
        <p>Панель доступна только модераторам и администраторам</p>
      </div>
    );
  }

  return (
    <div className="mod-panel">
      <div className="mod-header">
        <div>
          <h1 className="mod-title">Панель модератора</h1>
          <p className="mod-sub">Управление заказами и модерация контента</p>
        </div>
      </div>

      {msg && (
        <div className="mod-alert" onClick={() => setMsg('')}>
          {msg} <span style={{float:'right',cursor:'pointer'}}>✕</span>
        </div>
      )}

      {/* Tabs */}
      <div className="mod-tabs">
        <button className={`mod-tab${tab === 'orders'  ? ' active' : ''}`} onClick={() => setTab('orders')}>
          Заказы
        </button>
        <button className={`mod-tab${tab === 'reviews' ? ' active' : ''}`} onClick={() => setTab('reviews')}>
          Отзывы
        </button>
        <button className={`mod-tab${tab === 'stats'   ? ' active' : ''}`} onClick={() => setTab('stats')}>
          Статистика
        </button>
      </div>

      {loading && <div className="mod-loading">Загрузка...</div>}

      {/* ── Orders ─────────────────────────────────────────────────────── */}
      {tab === 'orders' && !loading && (
        <div className="mod-section">
          <div className="mod-filter-row">
            <select className="mod-select" value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
              <option value="">Все статусы</option>
              {Object.entries(STATUS_LABELS).map(([k, v]) => (
                <option key={k} value={k}>{v}</option>
              ))}
            </select>
            <button className="btn btn-outline" onClick={loadOrders}>Обновить</button>
          </div>

          {orders.length === 0 ? (
            <div className="mod-empty">Заказов не найдено</div>
          ) : (
            <div className="mod-table-wrap">
              <table className="mod-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Польз.</th>
                    <th>Сумма</th>
                    <th>Статус</th>
                    <th>Дата</th>
                    <th>Действие</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map(o => (
                    <tr key={o.orderId}>
                      <td className="mod-id">#{o.orderId}</td>
                      <td>user {o.userId}</td>
                      <td>{Number(o.totalAmount).toLocaleString('ru-RU')} ₽</td>
                      <td>
                        <span className={`status-chip status-${o.status.toLowerCase()}`}>
                          {STATUS_LABELS[o.status] ?? o.status}
                        </span>
                      </td>
                      <td className="mod-date">
                        {o.createdAt ? new Date(o.createdAt).toLocaleDateString('ru-RU') : '—'}
                      </td>
                      <td>
                        {STATUS_NEXT[o.status]?.length > 0 ? (
                          <div className="mod-actions">
                            {STATUS_NEXT[o.status].map(s => (
                              <button
                                key={s}
                                className={`mod-action-btn ${s === 'CANCELLED' ? 'cancel' : 'advance'}`}
                                onClick={() => handleStatusChange(o.orderId, s)}
                              >
                                {STATUS_LABELS[s]}
                              </button>
                            ))}
                          </div>
                        ) : (
                          <span className="mod-final">—</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ── Reviews ────────────────────────────────────────────────────── */}
      {tab === 'reviews' && !loading && (
        <div className="mod-section">
          <div className="mod-filter-row">
            <span className="mod-count">{reviews.length} отзывов</span>
            <button className="btn btn-outline" onClick={loadReviews}>Обновить</button>
          </div>
          {reviews.length === 0 ? (
            <div className="mod-empty">Отзывов нет</div>
          ) : (
            <div className="mod-table-wrap">
              <table className="mod-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Польз.</th>
                    <th>Книга</th>
                    <th>Оценка</th>
                    <th>Комментарий</th>
                    <th>Дата</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {reviews.map(r => (
                    <tr key={r.reviewId}>
                      <td className="mod-id">#{r.reviewId}</td>
                      <td>{r.username ?? `user ${r.bookId}`}</td>
                      <td className="mod-book-title">{r.bookTitle ?? `Книга #${r.bookId}`}</td>
                      <td>
                        <span className="review-stars">{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
                      </td>
                      <td className="mod-comment">{r.comment ?? <em>без комментария</em>}</td>
                      <td className="mod-date">
                        {r.createdAt ? new Date(r.createdAt).toLocaleDateString('ru-RU') : '—'}
                      </td>
                      <td>
                        <button className="mod-del-btn" onClick={() => handleDeleteReview(r.reviewId)}
                                title="Удалить отзыв">
                          🗑
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ── Stats ──────────────────────────────────────────────────────── */}
      {tab === 'stats' && !loading && stats && (
        <div className="mod-section">
          <div className="mod-kpi-grid">
            {[
              { label: 'Всего заказов',    value: stats.totalOrders,      icon: '📦' },
              { label: 'Ожидают',          value: stats.pendingOrders,    icon: '⏳' },
              { label: 'В обработке',      value: stats.processingOrders, icon: '⚙️' },
              { label: 'Доставлено',       value: stats.deliveredOrders,  icon: '✅' },
              { label: 'Отменено',         value: stats.cancelledOrders,  icon: '❌' },
              { label: 'Всего отзывов',    value: stats.totalReviews,     icon: '💬' },
            ].map(({ label, value, icon }) => (
              <div key={label} className="mod-kpi-card">
                <div className="mod-kpi-icon">{icon}</div>
                <div className="mod-kpi-val">{value}</div>
                <div className="mod-kpi-label">{label}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
