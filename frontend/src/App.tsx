import { useState } from 'react'
import { checkSystemHealth, type SystemHealth } from './api/health'
import { IncidentList } from './components/IncidentList'
import { IncidentDetail } from './components/IncidentDetail'
import './App.css'

function SystemHealthPanel() {
  const [health, setHealth] = useState<SystemHealth | null>(null)
  const [status, setStatus] = useState<'idle' | 'loading' | 'error'>('idle')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [open, setOpen] = useState(false)

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
    <details open={open} onToggle={(e) => setOpen(e.currentTarget.open)} style={{ marginBottom: '1.5rem', color: '#666' }}>
      <summary style={{ cursor: 'pointer' }}>System status (backend → ai-engine → simulator)</summary>
      <div style={{ marginTop: '0.5rem' }}>
        <button onClick={runCheck} disabled={status === 'loading'}>
          {status === 'loading' ? 'Checking…' : 'Check system health'}
        </button>
        {status === 'error' && <p style={{ color: 'crimson' }}>Failed to reach backend: {errorMessage}</p>}
        {health && (
          <ul>
            <li>Backend: <strong>{health.status}</strong></li>
            <li>AI Engine: <strong>{health.aiEngine?.status ?? 'unreachable'}</strong></li>
            <li>Simulator: <strong>{health.aiEngine?.simulator?.status ?? 'unreachable'}</strong></li>
          </ul>
        )}
      </div>
    </details>
  )
}

function App() {
  const [selectedIncidentId, setSelectedIncidentId] = useState<number | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)

  return (
    <main style={{ fontFamily: 'system-ui, sans-serif', maxWidth: 800, margin: '3rem auto', padding: '0 1rem' }}>
      <h1>Incident Command AI</h1>
      <SystemHealthPanel />

      {selectedIncidentId === null ? (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ margin: 0 }}>Incidents</h2>
            <button onClick={() => setRefreshToken((t) => t + 1)}>Refresh</button>
          </div>
          <IncidentList onSelect={setSelectedIncidentId} refreshToken={refreshToken} />
        </>
      ) : (
        <IncidentDetail incidentId={selectedIncidentId} onBack={() => setSelectedIncidentId(null)} />
      )}
    </main>
  )
}

export default App
