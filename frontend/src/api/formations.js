const API_BASE_URL = "http://localhost:8081/api/v1"

function normaliserFormation(item) {
  return {
    ...item,

    id: item.id || item.idFormation || item._id,
    idFormation: item.idFormation || item.id || item._id,

    titre: item.titre || item.nom || "Formation",
    nom: item.nom || item.titre || "Formation",

    description: item.description || "",
    duree: item.duree || "",
    lieu: item.lieu || item.ville || "",
    ville: item.ville || item.lieu || "",

    categorie: item.categorie || item.nomCategorie || "",
    centre: item.centre || item.nomCentre || item.ecole || "",

    prix: Number(item.prix || item.prixPublic || 0),
    prixRemise: Number(item.prixRemise || item.prix || item.prixPublic || 0),

    image: item.image || item.imageUrl || null,

    dateDebut: item.dateDebut || "",
    dateLimite: item.dateLimite || item.dateLimiteInscription || ""
  }
}

export async function obtenirFormations() {
  const response = await fetch(`${API_BASE_URL}/formations`)

  if (!response.ok) {
    throw new Error("Impossible de charger les formations")
  }

  const data = await response.json()

  if (!Array.isArray(data)) {
    return []
  }

  return data.map(normaliserFormation)
}

export async function obtenirFormationParId(id) {
  const response = await fetch(`${API_BASE_URL}/formations/${id}`)

  if (!response.ok) {
    throw new Error("Impossible de charger le détail de la formation")
  }

  const data = await response.json()

  return normaliserFormation(data)
}