import { useEffect, useState } from 'react'
import { listIncidents, type Incident } from '../api/incidents'
import { SlaCountdown } from './SlaCountdown'

const STATUS_COLORS: Record<string, string> = {
  CREATED: '#c77700',
  INVESTIGATING: '#c77700',
  DIAGNOSED: '#1f6feb',
  RESOLVED: '#2a8f4d',
  ESCALATED_TO_HUMAN: 'crimson',
}

export function IncidentList({ onSelect, refreshToken }: { onSelect: (id: number) => void; refreshToken: number }) {
  const [incidents, setIncidents] = useState<Incident[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    listIncidents()
      .then((data) => {
        if (!cancelled) setIncidents(data.slice().reverse())
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'failed to load incidents')
      })
    return () => {
      cancelled = true
    }
  }, [refreshToken])

  if (error) return <p style={{ color: 'crimson' }}>Failed to load incidents: {error}</p>
  if (incidents === null) return <p>Loading incidents…</p>
  if (incidents.length === 0) return <p style={{ color: '#666' }}>No incidents yet.</p>

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ textAlign: 'left', borderBottom: '1px solid #ddd' }}>
          <th style={{ padding: '6px 8px' }}>#</th>
          <th style={{ padding: '6px 8px' }}>Service</th>
          <th style={{ padding: '6px 8px' }}>Severity</th>
          <th style={{ padding: '6px 8px' }}>Status</th>
          <th style={{ padding: '6px 8px' }}>SLA</th>
        </tr>
      </thead>
      <tbody>
        {incidents.map((incident) => (
          <tr
            key={incident.id}
            onClick={() => onSelect(incident.id)}
            style={{ cursor: 'pointer', borderBottom: '1px solid #eee' }}
          >
            <td style={{ padding: '6px 8px' }}>{incident.id}</td>
            <td style={{ padding: '6px 8px' }}>
              {incident.serviceName} <span style={{ color: '#888' }}>({incident.productionName})</span>
            </td>
            <td style={{ padding: '6px 8px' }}>{incident.severity}</td>
            <td style={{ padding: '6px 8px', color: STATUS_COLORS[incident.status] ?? 'inherit' }}>
              {incident.status}
            </td>
            <td style={{ padding: '6px 8px' }}>
              <SlaCountdown deadlineAt={incident.slaDeadlineAt} resolved={incident.status === 'RESOLVED'} />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
