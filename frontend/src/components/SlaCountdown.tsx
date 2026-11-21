import { useEffect, useState } from 'react'
import './components.css'

function formatRemaining(ms: number): string {
  if (ms <= 0) return 'BREACHED'
  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

export function SlaCountdown({ deadlineAt, resolved }: { deadlineAt: string; resolved: boolean }) {
  const [now, setNow] = useState(Date.now())

  useEffect(() => {
    if (resolved) return
    const interval = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(interval)
  }, [resolved])

  if (resolved) {
    return <span className="sla-countdown resolved">resolved</span>
  }

  const remainingMs = new Date(deadlineAt).getTime() - now
  const breached = remainingMs <= 0
  const warn = !breached && remainingMs < 5 * 60_000
  const className = `sla-countdown ${breached ? 'breached' : warn ? 'warn' : 'ok'}`

  return <span className={className}>{formatRemaining(remainingMs)}</span>
}
