import { useEffect, useRef, useState } from 'react'
import { listIncidents, type Incident } from '../api/incidents'
import { SlaCountdown } from './SlaCountdown'
import { SeverityBadge, StatusBadge } from './Badges'
import './components.css'

const POLL_MS = 4000

export function IncidentList({ onSelect, refreshToken }: { onSelect: (id: number) => void; refreshToken: number }) {
  const [incidents, setIncidents] = useState<Incident[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const loadedOnce = useRef(false)

  useEffect(() => {
    let cancelled = false
    const load = () => {
      listIncidents()
        .then((data) => {
          if (cancelled) return
          setIncidents(data.slice().reverse())
          setError(null)
          loadedOnce.current = true
        })
        .catch((err) => {
          if (!cancelled) setError(err instanceof Error ? err.message : 'failed to load incidents')
        })
    }
    load()
    const interval = setInterval(load, POLL_MS)
    return () => {
      cancelled = true
      clearInterval(interval)
    }
    // refreshToken bumps trigger an immediate re-poll (e.g. right after
    // starting a demo scenario) on top of the steady background poll.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshToken])

  if (error && !loadedOnce.current) return <p className="error-state">Failed to load incidents: {error}</p>
  if (incidents === null) return <p className="empty-state">Loading incidents…</p>
  if (incidents.length === 0) {
    return (
      <p className="empty-state">
        No incidents yet. Click &ldquo;Start demo incident&rdquo; above to inject Scenario A.
      </p>
    )
  }

  return (
    <table className="incident-table">
      <thead>
        <tr>
          <th>#</th>
          <th>Service</th>
          <th>Severity</th>
          <th>Status</th>
          <th style={{ textAlign: 'right' }}>SLA</th>
        </tr>
      </thead>
      <tbody>
        {incidents.map((incident) => (
          <tr key={incident.id} className="incident-row" onClick={() => onSelect(incident.id)}>
            <td className="incident-id">#{incident.id}</td>
            <td>
              <span className="incident-service">{incident.serviceName}</span>{' '}
              <span className="incident-production">({incident.productionName})</span>
            </td>
            <td>
              <SeverityBadge severity={incident.severity} />
            </td>
            <td>
              <StatusBadge status={incident.status} />
            </td>
            <td style={{ textAlign: 'right' }}>
              <SlaCountdown deadlineAt={incident.slaDeadlineAt} resolved={incident.status === 'RESOLVED'} />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
