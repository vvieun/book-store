import React, { useState, useEffect } from 'react';
import { healthCheck } from '../api';
import type { ApiHealthStatus } from '../types';
import './ApiStatus.css';

function ApiStatus() {
  const [status, setStatus] = useState<ApiHealthStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkStatus();
    const interval = setInterval(checkStatus, 10000);
    return () => clearInterval(interval);
  }, []);

  const checkStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await healthCheck();
      setStatus(response.data);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Неизвестная ошибка';
      setError('API недоступен: ' + msg);
      setStatus(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="api-status-header">
        <h2>🏥 Состояние API</h2>
        <button className="btn btn-secondary" onClick={checkStatus} disabled={loading}>
          {loading ? '⏳' : '🔄'} Обновить
        </button>
      </div>

      {error ? (
        <div className="status-error">
          <div className="status-icon">❌</div>
          <h3>API недоступен</h3>
          <p>{error}</p>
        </div>
      ) : status ? (
        <div className="status-success">
          <div className="status-icon">✅</div>
          <h3>API работает нормально</h3>

          <div className="status-details">
            <div className="status-item">
              <span className="status-label">Сервис:</span>
              <span className="status-value healthy">{status.service}</span>
            </div>
            <div className="status-item">
              <span className="status-label">Статус:</span>
              <span className="status-value healthy">{status.status}</span>
            </div>
            <div className="status-item">
              <span className="status-label">Версия:</span>
              <span className="status-value">{status.version}</span>
            </div>
          </div>

          <div className="api-info">
            <h4>📚 Документация API</h4>
            <div className="api-links">
              <a
                href="http://localhost:8080/swagger-ui.html"
                target="_blank"
                rel="noopener noreferrer"
                className="api-link"
              >
                Swagger UI →
              </a>
              <a
                href="http://localhost:8080/api-docs"
                target="_blank"
                rel="noopener noreferrer"
                className="api-link"
              >
                OpenAPI JSON →
              </a>
            </div>
          </div>
        </div>
      ) : (
        <div className="loading">Проверка...</div>
      )}
    </div>
  );
}

export default ApiStatus;
