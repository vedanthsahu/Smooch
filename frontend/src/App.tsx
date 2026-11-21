import { useEffect, useState } from 'react'
import { checkSystemHealth, type SystemHealth } from './api/health'
import { IncidentList } from './components/IncidentList'
import { IncidentDetail } from './components/IncidentDetail'
import { ScenarioControls } from './components/ScenarioControls'
import { LogsPanel } from './components/LogsPanel'
import './App.css'

const HEALTH_POLL_MS = 10000

function HealthPill() {
  const [health, setHealth] = useState<SystemHealth | null>(null)
  const [unreachable, setUnreachable] = useState(false)

  useEffect(() => {
    let cancelled = false
    const check = () => {
      checkSystemHealth()
        .then((result) => {
          if (!cancelled) {
            setHealth(result)
            setUnreachable(false)
          }
        })
        .catch(() => {
          if (!cancelled) setUnreachable(true)
        })
    }
    check()
    const interval = setInterval(check, HEALTH_POLL_MS)
    return () => {
      cancelled = true
      clearInterval(interval)
    }
  }, [])

  const aiEngineOk = health?.aiEngine?.status === 'ok'
  const simulatorOk = health?.aiEngine?.simulator?.status === 'ok' || health?.aiEngine?.simulator?.status === 'healthy'
  const dotClass = unreachable ? 'bad' : health && aiEngineOk && simulatorOk ? 'ok' : health ? 'bad' : 'unknown'
  const label = unreachable
    ? 'backend unreachable'
    : health
      ? `backend · ai-engine ${aiEngineOk ? 'ok' : 'down'} · simulator ${simulatorOk ? 'ok' : 'down'}`
      : 'checking…'

  return (
    <span className="health-pill">
      <span className={`health-dot ${dotClass}`} />
      {label}
    </span>
  )
}

type Tab = 'incidents' | 'logs'

function App() {
  const [tab, setTab] = useState<Tab>('incidents')
  const [selectedIncidentId, setSelectedIncidentId] = useState<number | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-brand">
          <h1>Incident Command AI</h1>
          <span className="tagline">AI-assisted incident command for SLA protection — live demo</span>
        </div>
        <div className="app-tabs">
          <button
            data-active={tab === 'incidents'}
            onClick={() => {
              setTab('incidents')
              setSelectedIncidentId(null)
            }}
          >
            Incidents
          </button>
          <button data-active={tab === 'logs'} onClick={() => setTab('logs')}>
            Simulator logs
          </button>
        </div>
        <div className="app-header-right">
          <HealthPill />
        </div>
      </header>

      <main className="app-main">
        {tab === 'incidents' && (
          <>
            <div className="card" style={{ marginBottom: '1rem' }}>
              <p className="section-title">Demo controls</p>
              <ScenarioControls onTriggered={() => setRefreshToken((t) => t + 1)} />
            </div>

            {selectedIncidentId === null ? (
              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                  <p className="section-title" style={{ margin: 0 }}>
                    Incidents
                  </p>
                  <button className="ghost" onClick={() => setRefreshToken((t) => t + 1)}>
                    Refresh
                  </button>
                </div>
                <IncidentList onSelect={setSelectedIncidentId} refreshToken={refreshToken} />
              </div>
            ) : (
              <IncidentDetail incidentId={selectedIncidentId} onBack={() => setSelectedIncidentId(null)} />
            )}
          </>
        )}

        {tab === 'logs' && <LogsPanel />}
      </main>
    </div>
  )
}

export default App
