import React, { useState, useEffect, useCallback } from 'react';
import type { User, AdminUserDTO } from '../types';
import {
  getAdminUsers, changeUserRole, deleteAdminUser,
  getAdminAuthors, createAuthor, updateAuthor, deleteAuthor,
  getAdminCategories, createCategory, updateCategory, deleteCategory,
  getAdminStats,
} from '../api';
import './AdminPanel.css';

interface Props {
  currentUser: User | null;
  onLoginRequired: () => void;
}

type Tab = 'stats' | 'users' | 'authors' | 'categories';

interface Author {
  authorId: number;
  name: string;
  biography?: string;
  country?: string;
}

interface Category {
  categoryId: number;
  name: string;
  description?: string;
}

const ROLE_LABELS: Record<string, string> = {
  CUSTOMER:  'Покупатель',
  MODERATOR: 'Модератор',
  ADMIN:     'Администратор',
};

export default function AdminPanel({ currentUser, onLoginRequired }: Props) {
  const [tab, setTab]             = useState<Tab>('stats');
  const [users, setUsers]         = useState<AdminUserDTO[]>([]);
  const [authors, setAuthors]     = useState<Author[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [stats, setStats]         = useState<Record<string, number> | null>(null);
  const [loading, setLoading]     = useState(false);
  const [msg, setMsg]             = useState('');
  const [msgType, setMsgType]     = useState<'ok' | 'err'>('ok');

  const [editAuthorId, setEditAuthorId]     = useState<number | null>(null);
  const [editAuthorData, setEditAuthorData] = useState({ name: '', biography: '', country: '' });
  const [newAuthor, setNewAuthor]           = useState({ name: '', biography: '', country: '' });
  const [showNewAuthor, setShowNewAuthor]   = useState(false);

  const [editCatId, setEditCatId]         = useState<number | null>(null);
  const [editCatData, setEditCatData]     = useState({ name: '', description: '' });
  const [newCat, setNewCat]               = useState({ name: '', description: '' });
  const [showNewCat, setShowNewCat]       = useState(false);

  const isAdmin = currentUser?.role === 'ADMIN';

  const flash = (text: string, type: 'ok' | 'err' = 'ok') => {
    setMsg(text); setMsgType(type);
  };

  const loadUsers      = useCallback(async () => {
    setLoading(true);
    try { setUsers((await getAdminUsers()).data); } catch { flash('Ошибка загрузки пользователей', 'err'); }
    setLoading(false);
  }, []);

  const loadAuthors    = useCallback(async () => {
    setLoading(true);
    try { setAuthors((await getAdminAuthors()).data); } catch { flash('Ошибка загрузки авторов', 'err'); }
    setLoading(false);
  }, []);

  const loadCategories = useCallback(async () => {
    setLoading(true);
    try { setCategories((await getAdminCategories()).data); } catch { flash('Ошибка загрузки категорий', 'err'); }
    setLoading(false);
  }, []);

  const loadStats      = useCallback(async () => {
    setLoading(true);
    try { setStats((await getAdminStats()).data); } catch { flash('Ошибка загрузки статистики', 'err'); }
    setLoading(false);
  }, []);

  useEffect(() => {
    if (!isAdmin) return;
    if (tab === 'stats')      loadStats();
    if (tab === 'users')      loadUsers();
    if (tab === 'authors')    loadAuthors();
    if (tab === 'categories') loadCategories();
  }, [tab, isAdmin, loadStats, loadUsers, loadAuthors, loadCategories]);

  const handleRoleChange = async (uid: number, role: string) => {
    try {
      await changeUserRole(uid, role);
      flash(`Роль пользователя #${uid} изменена`);
      loadUsers();
    } catch { flash('Ошибка смены роли', 'err'); }
  };

  const handleDeleteUser = async (uid: number) => {
    if (!window.confirm('Удалить пользователя?')) return;
    try {
      await deleteAdminUser(uid);
      flash('Пользователь удалён');
      loadUsers();
    } catch { flash('Ошибка удаления', 'err'); }
  };

  const handleSaveAuthor = async () => {
    if (!newAuthor.name.trim()) { flash('Имя автора обязательно', 'err'); return; }
    try {
      await createAuthor(newAuthor);
      flash('Автор создан');
      setShowNewAuthor(false);
      setNewAuthor({ name: '', biography: '', country: '' });
      loadAuthors();
    } catch { flash('Ошибка создания', 'err'); }
  };

  const handleUpdateAuthor = async (id: number) => {
    try {
      await updateAuthor(id, editAuthorData);
      flash('Автор обновлён');
      setEditAuthorId(null);
      loadAuthors();
    } catch { flash('Ошибка обновления', 'err'); }
  };

  const handleDeleteAuthor = async (id: number) => {
    if (!window.confirm('Удалить автора?')) return;
    try {
      await deleteAuthor(id);
      flash('Автор удалён');
      loadAuthors();
    } catch { flash('Ошибка удаления', 'err'); }
  };

  const handleSaveCat = async () => {
    if (!newCat.name.trim()) { flash('Название обязательно', 'err'); return; }
    try {
      await createCategory(newCat);
      flash('Категория создана');
      setShowNewCat(false);
      setNewCat({ name: '', description: '' });
      loadCategories();
    } catch { flash('Ошибка создания', 'err'); }
  };

  const handleUpdateCat = async (id: number) => {
    try {
      await updateCategory(id, editCatData);
      flash('Категория обновлена');
      setEditCatId(null);
      loadCategories();
    } catch { flash('Ошибка обновления', 'err'); }
  };

  const handleDeleteCat = async (id: number) => {
    if (!window.confirm('Удалить категорию?')) return;
    try {
      await deleteCategory(id);
      flash('Категория удалена');
      loadCategories();
    } catch { flash('Ошибка удаления', 'err'); }
  };

  if (!currentUser) {
    return (
      <div className="admin-gate">
        <div className="admin-gate-icon">🔒</div>
        <h2>Требуется авторизация</h2>
        <p>Войдите как администратор</p>
        <button className="btn btn-primary" onClick={onLoginRequired}>Войти</button>
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="admin-gate">
        <div className="admin-gate-icon">⛔</div>
        <h2>Недостаточно прав</h2>
        <p>Панель доступна только администраторам</p>
      </div>
    );
  }

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <div>
          <h1 className="admin-title">Панель администратора</h1>
          <p className="admin-sub">Управление пользователями, авторами и каталогом</p>
        </div>
      </div>

      {msg && (
        <div className={`admin-alert ${msgType}`} onClick={() => setMsg('')}>
          {msg} <span style={{ float: 'right', cursor: 'pointer' }}>✕</span>
        </div>
      )}

      <div className="admin-tabs">
        {(['stats', 'users', 'authors', 'categories'] as Tab[]).map(t => (
          <button
            key={t}
            className={`admin-tab${tab === t ? ' active' : ''}`}
            onClick={() => setTab(t)}
          >
            {{ stats: 'Статистика', users: 'Пользователи', authors: 'Авторы', categories: 'Категории' }[t]}
          </button>
        ))}
      </div>

      {loading && <div className="admin-loading">Загрузка...</div>}

      {tab === 'stats' && !loading && stats && (
        <div className="admin-kpi-grid">
          {[
            { label: 'Пользователи',   val: stats.totalUsers,      icon: '👤', color: '#7c3aed' },
            { label: 'Книги',          val: stats.totalBooks,      icon: '📚', color: '#0369a1' },
            { label: 'Авторы',         val: stats.totalAuthors,    icon: '✍️', color: '#0891b2' },
            { label: 'Категории',      val: stats.totalCategories, icon: '🏷️', color: '#059669' },
            { label: 'Отзывы',        val: stats.totalReviews,    icon: '💬', color: '#d97706' },
            { label: 'Заказы',         val: stats.totalOrders,     icon: '📦', color: '#dc2626' },
          ].map(({ label, val, icon, color }) => (
            <div key={label} className="admin-kpi-card" style={{ '--kpi-color': color } as React.CSSProperties}>
              <div className="admin-kpi-icon">{icon}</div>
              <div className="admin-kpi-val">{val}</div>
              <div className="admin-kpi-label">{label}</div>
            </div>
          ))}
        </div>
      )}

      {tab === 'users' && !loading && (
        <div className="admin-section">
          <div className="admin-filter-row">
            <span className="admin-count">{users.length} пользователей</span>
            <button className="btn btn-outline" onClick={loadUsers}>Обновить</button>
          </div>
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th><th>Логин</th><th>Email</th><th>Роль</th><th>Дата</th><th></th>
                </tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.userId}>
                    <td className="admin-id">#{u.userId}</td>
                    <td><strong>{u.username}</strong></td>
                    <td className="admin-email">{u.email}</td>
                    <td>
                      <select
                        className="admin-role-select"
                        value={u.role}
                        onChange={e => handleRoleChange(u.userId, e.target.value)}
                        disabled={u.userId === currentUser.userId}
                      >
                        {Object.entries(ROLE_LABELS).map(([k, v]) => (
                          <option key={k} value={k}>{v}</option>
                        ))}
                      </select>
                    </td>
                    <td className="admin-date">
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString('ru-RU') : '—'}
                    </td>
                    <td>
                      {u.userId !== currentUser.userId && (
                        <button className="admin-del-btn" onClick={() => handleDeleteUser(u.userId)}>🗑</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'authors' && !loading && (
        <div className="admin-section">
          <div className="admin-filter-row">
            <span className="admin-count">{authors.length} авторов</span>
            <button className="btn btn-primary" onClick={() => setShowNewAuthor(!showNewAuthor)}>
              + Добавить автора
            </button>
          </div>

          {showNewAuthor && (
            <div className="admin-form-card">
              <h3>Новый автор</h3>
              <div className="admin-form-grid">
                <input className="admin-input" placeholder="Имя *" value={newAuthor.name}
                  onChange={e => setNewAuthor({ ...newAuthor, name: e.target.value })} />
                <input className="admin-input" placeholder="Страна" value={newAuthor.country}
                  onChange={e => setNewAuthor({ ...newAuthor, country: e.target.value })} />
                <textarea className="admin-input admin-textarea" placeholder="Биография" value={newAuthor.biography}
                  onChange={e => setNewAuthor({ ...newAuthor, biography: e.target.value })} />
              </div>
              <div className="admin-form-btns">
                <button className="btn btn-primary" onClick={handleSaveAuthor}>Сохранить</button>
                <button className="btn btn-outline" onClick={() => setShowNewAuthor(false)}>Отмена</button>
              </div>
            </div>
          )}

          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr><th>ID</th><th>Имя</th><th>Страна</th><th>Биография</th><th></th></tr>
              </thead>
              <tbody>
                {authors.map(a => (
                  <tr key={a.authorId}>
                    <td className="admin-id">#{a.authorId}</td>
                    {editAuthorId === a.authorId ? (
                      <>
                        <td><input className="admin-input-inline" value={editAuthorData.name}
                          onChange={e => setEditAuthorData({ ...editAuthorData, name: e.target.value })} /></td>
                        <td><input className="admin-input-inline" value={editAuthorData.country}
                          onChange={e => setEditAuthorData({ ...editAuthorData, country: e.target.value })} /></td>
                        <td><input className="admin-input-inline" value={editAuthorData.biography}
                          onChange={e => setEditAuthorData({ ...editAuthorData, biography: e.target.value })} /></td>
                        <td>
                          <button className="admin-save-btn" onClick={() => handleUpdateAuthor(a.authorId)}>✓</button>
                          <button className="admin-cancel-btn" onClick={() => setEditAuthorId(null)}>✕</button>
                        </td>
                      </>
                    ) : (
                      <>
                        <td><strong>{a.name}</strong></td>
                        <td>{a.country ?? '—'}</td>
                        <td className="admin-bio">{a.biography ?? '—'}</td>
                        <td>
                          <button className="admin-edit-btn" onClick={() => {
                            setEditAuthorId(a.authorId);
                            setEditAuthorData({ name: a.name, country: a.country ?? '', biography: a.biography ?? '' });
                          }}>✏️</button>
                          <button className="admin-del-btn" onClick={() => handleDeleteAuthor(a.authorId)}>🗑</button>
                        </td>
                      </>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'categories' && !loading && (
        <div className="admin-section">
          <div className="admin-filter-row">
            <span className="admin-count">{categories.length} категорий</span>
            <button className="btn btn-primary" onClick={() => setShowNewCat(!showNewCat)}>
              + Добавить категорию
            </button>
          </div>

          {showNewCat && (
            <div className="admin-form-card">
              <h3>Новая категория</h3>
              <div className="admin-form-grid">
                <input className="admin-input" placeholder="Название *" value={newCat.name}
                  onChange={e => setNewCat({ ...newCat, name: e.target.value })} />
                <input className="admin-input" placeholder="Описание" value={newCat.description}
                  onChange={e => setNewCat({ ...newCat, description: e.target.value })} />
              </div>
              <div className="admin-form-btns">
                <button className="btn btn-primary" onClick={handleSaveCat}>Сохранить</button>
                <button className="btn btn-outline" onClick={() => setShowNewCat(false)}>Отмена</button>
              </div>
            </div>
          )}

          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr><th>ID</th><th>Название</th><th>Описание</th><th></th></tr>
              </thead>
              <tbody>
                {categories.map(c => (
                  <tr key={c.categoryId}>
                    <td className="admin-id">#{c.categoryId}</td>
                    {editCatId === c.categoryId ? (
                      <>
                        <td><input className="admin-input-inline" value={editCatData.name}
                          onChange={e => setEditCatData({ ...editCatData, name: e.target.value })} /></td>
                        <td><input className="admin-input-inline" value={editCatData.description}
                          onChange={e => setEditCatData({ ...editCatData, description: e.target.value })} /></td>
                        <td>
                          <button className="admin-save-btn" onClick={() => handleUpdateCat(c.categoryId)}>✓</button>
                          <button className="admin-cancel-btn" onClick={() => setEditCatId(null)}>✕</button>
                        </td>
                      </>
                    ) : (
                      <>
                        <td><strong>{c.name}</strong></td>
                        <td className="admin-bio">{c.description ?? '—'}</td>
                        <td>
                          <button className="admin-edit-btn" onClick={() => {
                            setEditCatId(c.categoryId);
                            setEditCatData({ name: c.name, description: c.description ?? '' });
                          }}>✏️</button>
                          <button className="admin-del-btn" onClick={() => handleDeleteCat(c.categoryId)}>🗑</button>
                        </td>
                      </>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
