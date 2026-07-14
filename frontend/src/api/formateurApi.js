const API_BASE_URL = "http://localhost:8081/api/v1"

function getAuthFormateur() {
  const raw = localStorage.getItem("authFormateur")

  if (!raw) {
    return {
      token: null,
      formateur: null
    }
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    return {
      token: null,
      formateur: null
    }
  }
}

function getTokenFormateur() {
  const auth = getAuthFormateur()
  return auth.token || null
}

async function requestApi(path, options = {}) {
  const token = getTokenFormateur()

  const headers = {
    ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }),
    ...(options.headers || {})
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers
  })

  if (!response.ok) {
    let message = "Erreur serveur"

    try {
      const data = await response.json()
      message = data.message || data.error || message
    } catch (error) {
      message = response.statusText || message
    }

    throw new Error(message)
  }

  if (options.responseType === "blob") {
    return await response.blob()
  }

  const text = await response.text()

  if (!text) {
    return {}
  }

  return JSON.parse(text)
}

export async function connexionFormateur(email, motDePasse) {
  return await requestApi("/auth-formateur/connexion", {
    method: "POST",
    body: JSON.stringify({
      email,
      motDePasse,
      password: motDePasse
    })
  })
}

export async function deconnexionFormateur() {
  return await requestApi("/auth-formateur/deconnexion", {
    method: "POST"
  })
}

export async function getDashboardFormateur() {
  return await requestApi("/formateurs/tableau-de-bord")
}

export async function getStatsFormationFormateur(idFormation) {
  return await requestApi(`/formateurs/tableau-de-bord/formations/${idFormation}`)
}

export async function getProfilCentreFormateur() {
  return await requestApi("/formateurs/profil")
}

export async function modifierProfilCentreFormateur(data) {
  return await requestApi("/formateurs/profil", {
    method: "PUT",
    body: JSON.stringify(data)
  })
}

export async function getMesFormationsFormateur(statut = "") {
  const query = statut ? `?statut=${encodeURIComponent(statut)}` : ""
  const data = await requestApi(`/formations/mes-formations${query}`)

  if (Array.isArray(data)) {
    return data
  }

  return data.content || data.results || []
}

export async function publierFormationFormateur(data) {
  return await requestApi("/formations", {
    method: "POST",
    body: JSON.stringify(data)
  })
}

export async function modifierFormationFormateur(idFormation, data) {
  return await requestApi(`/formations/${idFormation}`, {
    method: "PUT",
    body: JSON.stringify(data)
  })
}

export async function supprimerFormationFormateur(idFormation) {
  return await requestApi(`/formations/${idFormation}`, {
    method: "DELETE"
  })
}

export async function getFinancesFormateur() {
  return await requestApi("/formateurs/finances")
}

export async function getVirementsFormateur() {
  const data = await requestApi("/formateurs/finances/virements")

  if (Array.isArray(data)) {
    return data
  }

  return data.content || data.results || []
}

export async function getInscritsFormation(idFormation) {
  const data = await requestApi(`/formations/${idFormation}/inscrits`)

  if (Array.isArray(data)) {
    return data
  }

  return data.content || data.results || []
}

export async function exporterInscritsPdf(idFormation) {
  const blob = await requestApi(`/formations/${idFormation}/inscrits/export-pdf`, {
    responseType: "blob"
  })

  const url = window.URL.createObjectURL(blob)
  const link = document.createElement("a")

  link.href = url
  link.download = `inscrits-formation-${idFormation}.pdf`

  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  window.URL.revokeObjectURL(url)
}