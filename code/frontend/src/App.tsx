import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import './App.css';
import ProductList from './components/ProductList';
import ProductDetail from './components/ProductDetail';
import Recommendations from './components/Recommendations';
import CreateRating from './components/CreateRating';
import UserStatistics from './components/UserStatistics';
import ApiStatus from './components/ApiStatus';
import Auth from './components/Auth';
import ModeratorPanel from './components/ModeratorPanel';
import AdminPanel from './components/AdminPanel';
import type { User, UserRole } from './types';

function getStoredUser(): User | null {
  const token    = localStorage.getItem('token');
  const userId   = localStorage.getItem('userId');
  const username = localStorage.getItem('username');
  const email    = localStorage.getItem('email');
  const role     = localStorage.getItem('role') as UserRole | null;
  if (token && userId && username && email) {
    return { token, userId: parseInt(userId, 10), username, email, role: role ?? 'CUSTOMER' };
  }
  return null;
}

function roleBadge(role: UserRole) {
  if (role === 'ADMIN')     return { label: 'Администратор', cls: 'role-admin' };
  if (role === 'MODERATOR') return { label: 'Модератор',     cls: 'role-moderator' };
  return null;
}

function App() {
  const [currentUser, setCurrentUser] = useState<User | null>(getStoredUser());
  const [showAuth, setShowAuth]         = useState(false);
  const [sessionMsg, setSessionMsg]     = useState<string | null>(null);

  const handleAuthSuccess = (user: User) => {
    localStorage.setItem('role', user.role ?? 'CUSTOMER');
    setCurrentUser({ ...user, userId: parseInt(String(user.userId), 10) });
    setShowAuth(false);
  };

  const handleLogout = () => {
    ['token', 'userId', 'username', 'email', 'role'].forEach(k => localStorage.removeItem(k));
    setCurrentUser(null);
  };

  React.useEffect(() => {
    const handler = () => {
      setCurrentUser(null);
      setSessionMsg('Сессия истекла — войдите снова');
      setTimeout(() => setSessionMsg(null), 4000);
    };
    window.addEventListener('auth:expired', handler);
    return () => window.removeEventListener('auth:expired', handler);
  }, []);

  const initials = currentUser?.username.slice(0, 2).toUpperCase() ?? '';
  const badge    = currentUser ? roleBadge(currentUser.role) : null;

  const isMod   = currentUser?.role === 'MODERATOR' || currentUser?.role === 'ADMIN';
  const isAdmin  = currentUser?.role === 'ADMIN';

  return (
    <Router>
      <div className="App">

        <header className="App-header">
          <div className="header-inner">

            <NavLink to="/" className="header-logo">
              <div className="logo-icon">📚</div>
              <span className="logo-text">Book<span>Store</span></span>
            </NavLink>

            <nav className="header-nav">
              <NavLink to="/" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`} end>
                Каталог
              </NavLink>
              <NavLink to="/recommendations" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
                Рекомендации
              </NavLink>
              {currentUser && (
                <NavLink to="/create-rating" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
                  Мои отзывы
                </NavLink>
              )}
              {currentUser && (
                <NavLink to="/statistics" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
                  Статистика
                </NavLink>
              )}
              {isMod && (
                <NavLink to="/moderator" className={({ isActive }) => `nav-link nav-link-mod${isActive ? ' active' : ''}`}>
                  Модератор
                </NavLink>
              )}
              {isAdmin && (
                <NavLink to="/admin" className={({ isActive }) => `nav-link nav-link-admin${isActive ? ' active' : ''}`}>
                  Админ
                </NavLink>
              )}
              <NavLink to="/status" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
                API
              </NavLink>
            </nav>

            <div className="header-user">
              {currentUser ? (
                <>
                  <div className="user-chip">
                    <div className={`user-avatar ${badge?.cls ?? ''}`}>{initials}</div>
                    <div className="user-info">
                      <span className="user-name">{currentUser.username}</span>
                      {badge && <span className={`user-role-badge ${badge.cls}`}>{badge.label}</span>}
                    </div>
                  </div>
                  <button className="btn btn-danger" onClick={handleLogout}>
                    Выйти
                  </button>
                </>
              ) : (
                <button className="btn btn-primary" onClick={() => setShowAuth(true)}>
                  Войти
                </button>
              )}
            </div>
          </div>
        </header>

        {showAuth && !currentUser && (
          <Auth onAuthSuccess={handleAuthSuccess} onClose={() => setShowAuth(false)} />
        )}

        {sessionMsg && (
          <div className="session-toast" onClick={() => setSessionMsg(null)}>
            🔒 {sessionMsg}
          </div>
        )}

        <main className="App-main">
          <Routes>
            <Route path="/" element={<ProductList />} />
            <Route path="/product/:id" element={
              <ProductDetail currentUser={currentUser} onLoginRequired={() => setShowAuth(true)} />
            } />
            <Route path="/recommendations" element={
              <Recommendations currentUser={currentUser} onLoginRequired={() => setShowAuth(true)} />
            } />
            <Route path="/create-rating" element={
              <CreateRating currentUser={currentUser} onLoginRequired={() => setShowAuth(true)} />
            } />
            <Route path="/statistics" element={
              <UserStatistics currentUser={currentUser} onLoginRequired={() => setShowAuth(true)} />
            } />
            <Route path="/moderator" element={
              <ModeratorPanel currentUser={currentUser} onLoginRequired={() => setShowAuth(true)} />
            } />
            <Route path="/admin" element={
              <AdminPanel currentUser={currentUser} onLoginRequired={() => setShowAuth(true)} />
            } />
            <Route path="/status" element={<ApiStatus />} />
          </Routes>
        </main>

        <footer className="App-footer">
          Курсовой проект по дисциплине «Базы данных» · ИУ7-61Б · 2026
        </footer>
      </div>
    </Router>
  );
}

export default App;
