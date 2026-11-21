const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL ?? 'http://localhost:8080'

export interface LogLine {
  timestampMillis: number
  message: string
}

export async function getSimulatorLogs(): Promise<LogLine[]> {
  const response = await fetch(`${BACKEND_BASE_URL}/api/logs/simulator`)
  if (!response.ok) {
    throw new Error(`/api/logs/simulator returned ${response.status}`)
  }
  return response.json()
}
