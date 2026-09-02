import { useState } from 'react'
import { checkSystemHealth, type SystemHealth } from './api/health'
import './App.css'

function App() {
  const [health, setHealth] = useState<SystemHealth | null>(null)
  const [status, setStatus] = useState<'idle' | 'loading' | 'error'>('idle')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const runCheck = async () => {
    setStatus('loading')
    setErrorMessage(null)
    try {
      const result = await checkSystemHealth()
      setHealth(result)
      setStatus('idle')
    } catch (err) {
      setStatus('error')
      setErrorMessage(err instanceof Error ? err.message : 'Unknown error')
    }
  }

  return (
    <main style={{ fontFamily: 'system-ui, sans-serif', maxWidth: 640, margin: '3rem auto', padding: '0 1rem' }}>
      <h1>Incident Command AI — System Status</h1>
      <p>Level 0 walking skeleton: frontend → backend → ai-engine → simulator.</p>

      <button onClick={runCheck} disabled={status === 'loading'}>
        {status === 'loading' ? 'Checking…' : 'Check system health'}
      </button>

      {status === 'error' && (
        <p style={{ color: 'crimson' }}>Failed to reach backend: {errorMessage}</p>
      )}

      {health && (
        <ul style={{ marginTop: '1.5rem', lineHeight: 1.8 }}>
          <li>Backend: <strong>{health.status}</strong></li>
          <li>AI Engine: <strong>{health.aiEngine?.status ?? 'unreachable'}</strong></li>
          <li>Simulator: <strong>{health.aiEngine?.simulator?.status ?? 'unreachable'}</strong></li>
        </ul>
      )}
    </main>
  )
}

export default App
