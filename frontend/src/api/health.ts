const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL ?? 'http://localhost:8080'

export interface SystemHealth {
  status: string
  service: string
  aiEngine?: {
    status: string
    service: string
    simulator?: {
      status: string
      service: string
    }
  }
}

export async function checkSystemHealth(): Promise<SystemHealth> {
  const response = await fetch(`${BACKEND_BASE_URL}/api/health/deep`)
  if (!response.ok) {
    throw new Error(`backend returned ${response.status}`)
  }
  return response.json()
}
