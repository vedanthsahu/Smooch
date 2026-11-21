const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL ?? 'http://localhost:8080'

// Field names are camelCase, matching every other DTO in this API -- see
// backend's ScenarioStartedDto for why this isn't a passthrough of the
// simulator's own snake_case shape.
export interface ScenarioStartResponse {
  scenario: string
  targetServiceId: string
  startedAt: number
}

async function postJson<T>(path: string): Promise<T> {
  const response = await fetch(`${BACKEND_BASE_URL}${path}`, { method: 'POST' })
  if (!response.ok) {
    throw new Error(`${path} returned ${response.status}`)
  }
  if (response.status === 204) {
    return undefined as T
  }
  return response.json()
}

export function startQueueConsumerFailureScenario(): Promise<ScenarioStartResponse> {
  return postJson<ScenarioStartResponse>('/api/simulator/scenarios/queue-consumer-failure/start')
}

export function resetScenario(): Promise<void> {
  return postJson<void>('/api/simulator/scenarios/reset')
}
