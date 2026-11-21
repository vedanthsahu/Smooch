import { useEffect, useRef, useState } from 'react'
import { getIncident, type IncidentDetail as IncidentDetailData } from '../api/incidents'
import { SlaCountdown } from './SlaCountdown'
import { SeverityBadge, StatusBadge } from './Badges'
import './components.css'

const POLL_MS = 3000

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
  const loadedOnce = useRef(false)

  useEffect(() => {
    let cancelled = false
    loadedOnce.current = false
    setDetail(null)
    const load = () => {
      getIncident(incidentId)
        .then((data) => {
          if (cancelled) return
          setDetail(data)
          setError(null)
          loadedOnce.current = true
        })
        .catch((err) => {
          if (!cancelled) setError(err instanceof Error ? err.message : 'failed to load incident')
        })
    }
    load()
    const interval = setInterval(load, POLL_MS)
    return () => {
      cancelled = true
      clearInterval(interval)
    }
  }, [incidentId])

  if (error && !loadedOnce.current) return <p className="error-state">Failed to load incident: {error}</p>
  if (detail === null) return <p className="empty-state">Loading incident #{incidentId}…</p>

  const { incident, timeline, investigation } = detail
  const evidence = parseEvidence(investigation?.evidenceJson ?? null)

  return (
    <div>
      <button className="ghost" onClick={onBack} style={{ marginBottom: '1rem' }}>
        ← back to incidents
      </button>

      <div className="card">
        <div className="detail-header">
          <div>
            <p className="detail-title">
              #{incident.id} — {incident.serviceName} ({incident.productionName})
            </p>
            <div className="detail-meta">
              <SeverityBadge severity={incident.severity} />
              <StatusBadge status={incident.status} />
            </div>
          </div>
          <div className="sla-block">
            <div className="label">SLA remaining</div>
            <SlaCountdown deadlineAt={incident.slaDeadlineAt} resolved={incident.status === 'RESOLVED'} />
          </div>
        </div>
      </div>

      <div className="card">
        <p className="section-title">AI investigation</p>
        {investigation === null && <p className="empty-state">Not yet investigated.</p>}
        {investigation !== null && (
          <>
            <div className="investigation-meta">
              <span className="chip">{investigation.status}</span>
              <span className="chip">
                {investigation.provider ?? '—'}/{investigation.model ?? '—'}
              </span>
              <span className="chip">tier={investigation.tier ?? '—'}</span>
              {investigation.totalLatencyMs != null && <span className="chip">{investigation.totalLatencyMs} ms</span>}
            </div>
            <p className="diagnosis-text">{investigation.diagnosis}</p>
            <p className="disclaimer">
              Diagnosis quality is not calibrated to a confidence score yet — treat this as a starting hypothesis,
              verify against the evidence below.
            </p>

            {evidence.length > 0 && (
              <>
                <p className="section-title" style={{ marginTop: '1rem' }}>
                  Evidence examined {investigation.servicesExamined ? `(${investigation.servicesExamined})` : ''}
                </p>
                <ul className="evidence-list">
                  {evidence.map((e, i) => (
                    <li key={i}>
                      <span className="evidence-source">[{e.service_id}]</span>
                      {e.source_type} {e.available ? `(${e.latency_ms}ms)` : '(unavailable)'}
                      <span className="evidence-content">{JSON.stringify(e.content)}</span>
                    </li>
                  ))}
                </ul>
              </>
            )}
          </>
        )}
      </div>

      <div className="card">
        <p className="section-title">Timeline</p>
        <ul className="timeline">
          {timeline.map((event) => (
            <li key={event.id}>
              <span className="timeline-time">{new Date(event.createdAt).toLocaleTimeString()}</span>
              <strong>{event.eventType}</strong> <span className="timeline-actor">({event.actor})</span>
              {event.payload ? `: ${event.payload}` : ''}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
