export function SeverityBadge({ severity }: { severity: string }) {
  return <span className={`badge badge-severity-${severity}`}>{severity}</span>
}

export function StatusBadge({ status }: { status: string }) {
  return <span className={`badge badge-status badge-status-${status}`}>{status.replaceAll('_', ' ')}</span>
}
