import { useEffect, useState } from 'react'
import { getSimulatorLogs, type LogLine } from '../api/logs'

const POLL_MS = 5000

function formatTimestamp(ms: number): string {
  return new Date(ms).toLocaleTimeString(undefined, { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

/**
 * Read-only view into the Production Simulator's real CloudWatch Logs (see
 * infra/aws/cloudwatch/production-simulator-logging.json) -- a demo viewer
 * sees actual simulator activity without ever touching AWS. If
 * CLOUDWATCH_ENABLED=false on the backend (the default), this just shows an
 * explanatory empty state rather than an error.
 */
export function LogsPanel() {
  const [lines, setLines] = useState<LogLine[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    const poll = () => {
      getSimulatorLogs()
        .then((data) => {
          if (!cancelled) {
            setLines(data)
            setError(null)
          }
        })
        .catch((err) => {
          if (!cancelled) setError(err instanceof Error ? err.message : 'failed to load logs')
        })
    }
    poll()
    const interval = setInterval(poll, POLL_MS)
    return () => {
      cancelled = true
      clearInterval(interval)
    }
  }, [])

  return (
    <div className="card">
      <p className="section-title">Simulator logs (AWS CloudWatch, EC2-hosted instance)</p>
      <p className="disclaimer" style={{ marginBottom: '0.75rem' }}>
        Real log lines from the Production Simulator's EC2 instance, forwarded by the CloudWatch agent. Read-only
        and server-mediated -- this page never holds an AWS credential. Only populated when the simulator you're
        driving is the EC2-hosted one; the default local demo runs a separate local simulator instance.
      </p>
      {error && <p className="error-state">Failed to load logs: {error}</p>}
      {!error && lines === null && <p className="empty-state">Loading…</p>}
      {!error && lines !== null && lines.length === 0 && (
        <p className="empty-state">
          No log events in the last few minutes. Either CloudWatch logging is disabled on this backend
          (CLOUDWATCH_ENABLED=false), or the EC2 simulator hasn't had any activity recently.
        </p>
      )}
      {!error && lines !== null && lines.length > 0 && (
        <div className="log-viewer">
          {lines.map((line, i) => (
            <div className="log-line" key={`${line.timestampMillis}-${i}`}>
              <span className="log-timestamp">{formatTimestamp(line.timestampMillis)}</span>
              <span className="log-message">{line.message}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
