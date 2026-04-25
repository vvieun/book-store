import React, { useState, ChangeEvent, FormEvent, useEffect } from 'react';
import { login, register } from '../api';
import type { User } from '../types';

interface AuthProps {
  onAuthSuccess: (user: User) => void;
  onClose?: () => void;
}

type Mode = 'login' | 'register';

function Auth({ onAuthSuccess, onClose }: AuthProps) {
  const [mode,     setMode]    = useState<Mode>('login');
  const [username, setUsername] = useState('');
  const [email,    setEmail]   = useState('');
  const [password, setPassword] = useState('');
  const [error,    setError]   = useState<string | null>(null);
  const [loading,  setLoading] = useState(false);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape' && onClose) onClose(); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [onClose]);

  const reset = () => { setUsername(''); setEmail(''); setPassword(''); setError(null); };

  const switchMode = (m: Mode) => { setMode(m); reset(); };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      let res;
      if (mode === 'login') {
        res = await login({ username, password });
      } else {
        res = await register({ username, email, password });
      }
      const { token, userId, username: uname, email: uemail, role } = res.data;
      localStorage.setItem('token',    token);
      localStorage.setItem('userId',   String(userId));
      localStorage.setItem('username', uname);
      localStorage.setItem('email',    uemail);
      localStorage.setItem('role',     role ?? 'CUSTOMER');
      onAuthSuccess({ token, userId, username: uname, email: uemail, role: role ?? 'CUSTOMER' });
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
        ?? (err instanceof Error ? err.message : 'Ошибка. Проверьте данные.');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={overlay} onClick={e => { if (e.target === e.currentTarget && onClose) onClose(); }}>
      <div style={card}>

        {onClose && (
          <button onClick={onClose} style={closeBtn} aria-label="Закрыть">✕</button>
        )}

        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div style={logoBox}>📚</div>
          <p style={logoName}>BookStore</p>
        </div>

        <div style={tabs}>
          <button style={tab(mode === 'login')}    onClick={() => switchMode('login')}>Вход</button>
          <button style={tab(mode === 'register')} onClick={() => switchMode('register')}>Регистрация</button>
        </div>

        {error && (
          <div style={errorBox}>{error}</div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={field}>
            <label style={label}>Имя пользователя</label>
            <input
              style={input}
              type="text"
              autoComplete="username"
              placeholder="username"
              value={username}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setUsername(e.target.value)}
              required
            />
          </div>

          {mode === 'register' && (
            <div style={field}>
              <label style={label}>Email</label>
              <input
                style={input}
                type="email"
                autoComplete="email"
                placeholder="email@example.com"
                value={email}
                onChange={(e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
                required
              />
            </div>
          )}

          <div style={field}>
            <label style={label}>Пароль</label>
            <input
              style={input}
              type="password"
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              placeholder="••••••••"
              value={password}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
              required
              minLength={6}
            />
          </div>

          <button style={submitBtn(loading)} type="submit" disabled={loading}>
            {loading
              ? 'Загрузка...'
              : mode === 'login' ? 'Войти' : 'Создать аккаунт'}
          </button>
        </form>

        <p style={hint}>
          {mode === 'login'
            ? <>Нет аккаунта? <button style={linkBtn} onClick={() => switchMode('register')}>Зарегистрироваться</button></>
            : <>Уже есть аккаунт? <button style={linkBtn} onClick={() => switchMode('login')}>Войти</button></>}
        </p>
      </div>
    </div>
  );
}

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0,
  background: 'rgba(15,23,42,0.55)',
  backdropFilter: 'blur(4px)',
  display: 'flex', alignItems: 'center', justifyContent: 'center',
  zIndex: 1000, padding: '1rem',
};

const card: React.CSSProperties = {
  background: '#fff',
  borderRadius: '16px',
  padding: '2rem 2.25rem',
  width: '100%', maxWidth: '400px',
  boxShadow: '0 20px 60px rgba(0,0,0,0.2)',
  position: 'relative',
};

const closeBtn: React.CSSProperties = {
  position: 'absolute', top: '1rem', right: '1rem',
  background: 'none', border: 'none',
  fontSize: '1.1rem', cursor: 'pointer',
  color: '#94a3b8', lineHeight: 1,
};

const logoBox: React.CSSProperties = {
  display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
  width: '48px', height: '48px',
  background: '#4f46e5',
  borderRadius: '12px',
  fontSize: '1.5rem',
  marginBottom: '0.5rem',
};

const logoName: React.CSSProperties = {
  fontSize: '1.1rem', fontWeight: 800,
  color: '#0f172a', letterSpacing: '-0.5px',
};

const tabs: React.CSSProperties = {
  display: 'flex',
  background: '#f1f5f9',
  borderRadius: '8px',
  padding: '3px',
  gap: '3px',
  marginBottom: '1.25rem',
};

const tab = (active: boolean): React.CSSProperties => ({
  flex: 1, padding: '0.45rem',
  border: 'none',
  borderRadius: '6px',
  background: active ? '#fff' : 'transparent',
  color: active ? '#0f172a' : '#64748b',
  fontWeight: active ? 700 : 500,
  fontSize: '0.9rem',
  cursor: 'pointer',
  boxShadow: active ? '0 1px 4px rgba(0,0,0,0.1)' : 'none',
  transition: 'all 0.15s',
  fontFamily: 'inherit',
});

const errorBox: React.CSSProperties = {
  background: '#fef2f2',
  color: '#b91c1c',
  border: '1px solid #fca5a5',
  borderRadius: '8px',
  padding: '0.7rem 0.9rem',
  fontSize: '0.875rem',
  marginBottom: '1rem',
};

const field: React.CSSProperties = { marginBottom: '1rem' };

const label: React.CSSProperties = {
  display: 'block',
  fontSize: '0.8rem',
  fontWeight: 600,
  color: '#475569',
  textTransform: 'uppercase',
  letterSpacing: '0.04em',
  marginBottom: '0.35rem',
};

const input: React.CSSProperties = {
  width: '100%',
  padding: '0.65rem 0.875rem',
  border: '1.5px solid #e2e8f0',
  borderRadius: '8px',
  fontSize: '0.95rem',
  color: '#0f172a',
  outline: 'none',
  transition: 'border-color 0.2s, box-shadow 0.2s',
  fontFamily: 'inherit',
  boxSizing: 'border-box',
};

const submitBtn = (loading: boolean): React.CSSProperties => ({
  width: '100%',
  padding: '0.75rem',
  background: loading ? '#818cf8' : '#4f46e5',
  color: '#fff',
  border: 'none',
  borderRadius: '8px',
  fontSize: '0.975rem',
  fontWeight: 700,
  cursor: loading ? 'not-allowed' : 'pointer',
  marginTop: '0.5rem',
  fontFamily: 'inherit',
  transition: 'background 0.2s',
  letterSpacing: '0.01em',
});

const hint: React.CSSProperties = {
  textAlign: 'center',
  marginTop: '1.1rem',
  fontSize: '0.85rem',
  color: '#64748b',
};

const linkBtn: React.CSSProperties = {
  background: 'none', border: 'none',
  color: '#4f46e5', fontWeight: 600,
  cursor: 'pointer', fontSize: '0.85rem',
  textDecoration: 'underline', padding: 0,
  fontFamily: 'inherit',
};

export default Auth;
