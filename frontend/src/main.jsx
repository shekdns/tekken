import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Activity, RefreshCw } from 'lucide-react';
import './styles.css';

function App() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadHealth = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await fetch('/api/health');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      setHealth(await response.json());
    } catch (err) {
      setHealth(null);
      setError('백엔드 서버에 연결할 수 없습니다. Spring Boot를 먼저 실행해 주세요.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHealth();
  }, []);

  return (
    <main className="app-shell">
      <section className="status-panel">
        <div className="title-row">
          <div>
            <p className="eyebrow">Spring Boot + React</p>
            <h1>Tekken Test</h1>
          </div>
          <Activity aria-hidden="true" />
        </div>

        <div className="health-box">
          <span className={health ? 'dot online' : 'dot offline'} />
          <div>
            <strong>{health ? 'Backend online' : 'Backend offline'}</strong>
            <p>{health ? `${health.service} · ${health.timestamp}` : error || '상태 확인 중입니다.'}</p>
          </div>
        </div>

        <button type="button" onClick={loadHealth} disabled={loading}>
          <RefreshCw aria-hidden="true" className={loading ? 'spin' : ''} />
          {loading ? '확인 중' : '다시 확인'}
        </button>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
