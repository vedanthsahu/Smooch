import { useState } from 'react'
import { resetScenario, startQueueConsumerFailureScenario } from '../api/simulator'

/**
 * Drives the Production Simulator's only implemented scenario (queue
 * consumer failure, see simulator/app/main.py) so a viewer can watch the
 * whole incident lifecycle live instead of needing curl. Fires straight at
 * the simulator via the backend's proxy endpoints -- DetectionService's
 * existing 5s poll loop is what actually notices the resulting degradation
 * and creates/investigates the incident; this component has no incident
 * logic of its own.
 */
export function ScenarioControls({ onTriggered }: { onTriggered: () => void }) {
  const [status, setStatus] = useState<'idle' | 'working' | 'ok' | 'error'>('idle')
  const [message, setMessage] = useState<string | null>(null)

  const run = async (action: 'start' | 'reset') => {
    setStatus('working')
    setMessage(null)
    try {
      if (action === 'start') {
        const result = await startQueueConsumerFailureScenario()
        setMessage(`Injected: ${result.scenario} on ${result.targetServiceId}. Detection polls every 5s.`)
      } else {
        await resetScenario()
        setMessage('Scenario reset -- service will report healthy on the next poll.')
      }
      setStatus('ok')
      onTriggered()
    } catch (err) {
      setStatus('error')
      setMessage(err instanceof Error ? err.message : 'Request failed')
    }
  }

  return (
    <div className="scenario-bar">
      <button className="primary" onClick={() => run('start')} disabled={status === 'working'}>
        {status === 'working' ? 'Working…' : 'Start demo incident'}
      </button>
      <button className="ghost" onClick={() => run('reset')} disabled={status === 'working'}>
        Reset scenario
      </button>
      {message && <span className={`scenario-status ${status === 'error' ? 'error' : 'ok'}`}>{message}</span>}
    </div>
  )
}
