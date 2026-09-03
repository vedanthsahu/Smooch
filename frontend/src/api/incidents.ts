const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL ?? 'http://localhost:8080'

export interface Incident {
  id: number
  serviceId: number
  serviceName: string
  productionName: string
  category: string
  severity: string
  status: string
  slaDeadlineAt: string
  createdAt: string
  resolvedAt: string | null
}

export interface IncidentEvent {
  id: number
  eventType: string
  actor: string
  payload: string | null
  createdAt: string
}

export interface Investigation {
  status: string
  diagnosis: string | null
  evidenceJson: string | null
  servicesExamined: string | null
  model: string | null
  provider: string | null
  tier: string | null
  totalLatencyMs: number | null
  startedAt: string
  completedAt: string | null
}

export interface IncidentDetail {
  incident: Incident
  timeline: IncidentEvent[]
  investigation: Investigation | null
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${BACKEND_BASE_URL}${path}`)
  if (!response.ok) {
    throw new Error(`${path} returned ${response.status}`)
  }
  return response.json()
}

export function listIncidents(): Promise<Incident[]> {
  return getJson<Incident[]>('/api/incidents')
}

export function getIncident(id: number): Promise<IncidentDetail> {
  return getJson<IncidentDetail>(`/api/incidents/${id}`)
}
