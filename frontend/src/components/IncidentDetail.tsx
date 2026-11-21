import { useEffect, useState } from 'react'
import { getIncident, type IncidentDetail as IncidentDetailData } from '../api/incidents'
import { SlaCountdown } from './SlaCountdown'

interface EvidenceEntry {
  service_id: string
  source_type: string
  content: unknown
  latency_ms: number
  available: boolean
}

function parseEvidence(evidenceJson: string | null): EvidenceEntry[] {
  if (!evidenceJson) return []
  try {
    return JSON.parse(evidenceJson) as EvidenceEntry[]
  } catch {
    return []
  }
}

export function IncidentDetail({ incidentId, onBack }: { incidentId: number; onBack: () => void }) {
  const [detail, setDetail] = useState<IncidentDetailData | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setDetail(null)
    getIncident(incidentId)
      .then((data) => {
        if (!cancelled) setDetail(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'failed to load incident')
      })
    return () => {
      cancelled = true
    }
  }, [incidentId])

  if (error) return <p style={{ color: 'crimson' }}>Failed to load incident: {error}</p>
  if (detail === null) return <p>Loading incident #{incidentId}…</p>

  const { incident, timeline, investigation } = detail
  const evidence = parseEvidence(investigation?.evidenceJson ?? null)

  return (
    <div>
      <button onClick={onBack} style={{ marginBottom: '1rem' }}>
        ← back to incidents
      </button>

      <h2>
        #{incident.id} — {incident.serviceName} ({incident.productionName})
      </h2>
      <p>
        Severity: <strong>{incident.severity}</strong> &nbsp;|&nbsp; Status: <strong>{incident.status}</strong>{' '}
        &nbsp;|&nbsp; SLA remaining: <SlaCountdown deadlineAt={incident.slaDeadlineAt} resolved={incident.status === 'RESOLVED'} />
      </p>

      <h3>Investigation</h3>
      {investigation === null && <p style={{ color: '#888' }}>Not yet investigated.</p>}
      {investigation !== null && (
        <div style={{ border: '1px solid #ddd', borderRadius: 6, padding: '0.75rem 1rem', marginBottom: '1rem' }}>
          <p>
            <strong>Diagnosis</strong> ({investigation.status}, {investigation.provider}/{investigation.model}, tier=
            {investigation.tier}, {investigation.totalLatencyMs} ms):
          </p>
          <p style={{ whiteSpace: 'pre-wrap' }}>{investigation.diagnosis}</p>
          <p style={{ color: '#888', fontSize: '0.85rem' }}>
            Confidence/quality is not calibrated -- see docs/09-ai-engine-architecture.md. Treat as a starting
            hypothesis, verify against the evidence below.
          </p>

          {evidence.length > 0 && (
            <>
              <p>
                <strong>Evidence examined</strong> ({investigation.servicesExamined}):
              </p>
              <ul>
                {evidence.map((e, i) => (
                  <li key={i}>
                    <code>[{e.service_id}]</code> {e.source_type} ({e.available ? `${e.latency_ms}ms` : 'unavailable'}
                    ): <code>{JSON.stringify(e.content)}</code>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      )}

      <h3>Timeline</h3>
      <ul>
        {timeline.map((event) => (
          <li key={event.id}>
            {new Date(event.createdAt).toLocaleTimeString()} — <strong>{event.eventType}</strong> ({event.actor})
            {event.payload ? `: ${event.payload}` : ''}
          </li>
        ))}
      </ul>
    </div>
  )
}
