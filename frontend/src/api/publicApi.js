const API_BASE_URL = 'http://localhost:8081/api/v1'

export async function getPublicStats() {
  const response = await fetch(`${API_BASE_URL}/public/stats`)

  if (!response.ok) {
    throw new Error('Impossible de charger les statistiques publiques')
  }

  return await response.json()
}

export async function getPublicCategories() {
  const response = await fetch(`${API_BASE_URL}/public/categories`)

  if (!response.ok) {
    throw new Error('Impossible de charger les catégories publiques')
  }

  return await response.json()
}