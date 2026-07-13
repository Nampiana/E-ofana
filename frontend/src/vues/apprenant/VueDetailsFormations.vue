<template>
  <div class="detail-page">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Chargement...</span>
      </div>
    </div>

    <div v-else-if="erreur" class="container py-5">
      <div class="alert alert-danger">
        {{ erreur }}
      </div>
    </div>

    <div v-else class="container py-4">
      <div class="mb-4 detail-breadcrumb">
        {{ formation.titre }}
      </div>

      <div class="row g-4">
        <div class="col-12 col-lg-8">
          <div class="formation-hero">
            <div class="hero-overlay">
              <span class="badge-category">
                {{ formation.categorie || "Catégorie non définie" }}
              </span>

              <h1 class="formation-title">
                {{ formation.titre || "Formation non définie" }}
              </h1>

              <div class="formation-rating">
                <span class="star">★</span>
                <span>{{ formation.note || "Non notée" }}</span>
                <span v-if="formation.nombreAvis">
                  ({{ formation.nombreAvis }} avis)
                </span>
              </div>
            </div>
          </div>

          <div class="info-card mt-4">
            <div class="row g-4">
              <div class="col-6 col-md-3">
                <div class="info-item">
                  <i class="bi bi-clock"></i>
                  <div>
                    <p>Durée</p>
                    <strong>{{ formation.duree || "Durée non définie" }}</strong>
                  </div>
                </div>
              </div>

              <div class="col-6 col-md-3">
                <div class="info-item">
                  <i class="bi bi-geo-alt"></i>
                  <div>
                    <p>Lieu exact</p>
                    <strong>
                      {{ formation.lieu || formation.ville || "Lieu non défini" }}
                    </strong>
                  </div>
                </div>
              </div>

              <div class="col-6 col-md-3">
                <div class="info-item">
                  <i class="bi bi-calendar"></i>
                  <div>
                    <p>Date début</p>
                    <strong>{{ formatDate(formation.dateDebut) }}</strong>
                  </div>
                </div>
              </div>

              <div class="col-6 col-md-3">
                <div class="info-item">
                  <i class="bi bi-calendar-x"></i>
                  <div>
                    <p>Limite inscription</p>
                    <strong>{{ formatDate(formation.dateLimite) }}</strong>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="formation.placesRestantes" class="alert alert-warning mt-4 mb-0">
              <i class="bi bi-exclamation-triangle me-2"></i>
              Attention ! Il ne reste plus que {{ formation.placesRestantes }} places disponibles pour cette session.
            </div>

            <div v-else class="alert alert-info mt-4 mb-0">
              <i class="bi bi-info-circle me-2"></i>
              Places disponibles non définies.
            </div>
          </div>

          <div class="content-card mt-4">
            <h2>Description de la formation</h2>

            <p>
              {{ formation.description || "Aucune description disponible pour cette formation." }}
            </p>
          </div>

          <div class="content-card mt-4">
            <h2>Centre de formation</h2>

            <div class="centre-box">
              <div class="centre-icon">
                <i class="bi bi-building"></i>
              </div>

              <div>
                <h3>{{ formation.centre || "Centre non défini" }}</h3>
                <p>
                  <i class="bi bi-geo-alt me-1"></i>
                  {{ formation.adresseCentre || formation.lieu || formation.ville || "Adresse non définie" }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="col-12 col-lg-4">
          <div class="price-card">
            <h3>TARIFICATION</h3>

            <div class="price-row">
              <span>Prix public conseillé :</span>
              <span class="old-price">
                {{ formatPrix(formation.prix) }}
              </span>
            </div>

            <div class="price-row">
              <span>Prix remisé centre :</span>
              <strong>
                {{ formatPrix(formation.prixRemise || formation.prix) }}
              </strong>
            </div>

            <div class="promo-box">
              <span class="promo-badge">-5% E-HOFANA</span>

              <div class="final-price">
                {{ formatPrix(prixFinal) }}
              </div>

              <div class="small-text">
                Tarif préférentiel · Paiement unique
              </div>
            </div>

            <button class="btn-main" @click="sinscrire">
              S'inscrire maintenant
            </button>

            <button class="btn-secondary-custom" @click="reserver">
              Réserver une place
            </button>

            <hr />

            <h4>INCLUS D'OFFICE :</h4>

            <ul class="included-list">
              <li>
                <i class="bi bi-award"></i>
                Certificat de réussite émis par
                {{ formation.centre || "le centre de formation" }}
              </li>

              <li>
                <i class="bi bi-chat-square-text"></i>
                Accès à l'espace d'entraide
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { obtenirFormationParId } from "../../api/formations.js"

const route = useRoute()
const router = useRouter()

const formation = ref({})
const loading = ref(false)
const erreur = ref("")

const idFormation = route.params.id

const prixFinal = computed(() => {
  const prixBase = Number(formation.value.prixRemise || formation.value.prix || 0)
  return Math.round(prixBase * 0.95)
})

function formatPrix(value) {
  const number = Number(value || 0)

  if (number === 0) {
    return "Prix non défini"
  }

  return `${number.toLocaleString("fr-FR")} Ar`
}

function formatDate(value) {
  if (!value) {
    return "Date non définie"
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "long",
    year: "numeric"
  })
}

async function chargerFormation() {
  loading.value = true
  erreur.value = ""

  try {
    const data = await obtenirFormationParId(idFormation)

    formation.value = {
      ...data,

      id: data.id || data.idFormation || idFormation,

      titre: data.titre || data.nom || "Formation",
      description: data.description || "",

      duree: data.duree || "",
      lieu: data.lieu || data.ville || "",
      ville: data.ville || data.lieu || "",

      categorie: data.categorie || data.nomCategorie || "",
      centre: data.centre || data.nomCentre || data.ecole || "",

      adresseCentre: data.adresseCentre || data.adresse || "",

      prix: Number(data.prix || data.prixPublic || 0),
      prixRemise: Number(data.prixRemise || data.prix || data.prixPublic || 0),

      dateDebut: data.dateDebut || data.dateDebutSession || "",
      dateLimite: data.dateLimite || data.dateLimiteInscription || "",

      placesRestantes: data.placesRestantes || data.nbPlacesRestantes || null,

      note: data.note || data.rating || null,
      nombreAvis: data.nombreAvis || data.nbAvis || null
    }

    console.log("Détail formation depuis la base :", formation.value)
  } catch (error) {
    console.error("Erreur chargement détail formation", error)
    erreur.value = "Impossible de charger le détail de cette formation."
  } finally {
    loading.value = false
  }
}

function sinscrire() {
  const id = formation.value.id || idFormation

  localStorage.setItem("formationSelectionnee", JSON.stringify(formation.value))

  router.push({
    path: "/inscription",
    query: {
      formationId: id
    }
  })
}

function reserver() {
  const id = formation.value.id || idFormation

  localStorage.setItem("formationSelectionnee", JSON.stringify(formation.value))

  router.push({
    path: "/inscription",
    query: {
      formationId: id,
      type: "reservation"
    }
  })
}

onMounted(() => {
  chargerFormation()
})
</script>

<style scoped>
.detail-page {
  background: #f8f8f8;
  min-height: 100vh;
}

.detail-breadcrumb {
  color: #b8792f;
  font-size: 14px;
}

.formation-hero {
  min-height: 320px;
  border-radius: 12px;
  overflow: hidden;
  background:
    linear-gradient(rgba(10, 25, 40, 0.75), rgba(10, 25, 40, 0.75)),
    url("https://images.unsplash.com/photo-1498050108023-c5249f4df085?q=80&w=1200&auto=format&fit=crop");
  background-size: cover;
  background-position: center;
  color: white;
}

.hero-overlay {
  padding: 40px;
}

.badge-category {
  background: #c9a15b;
  color: #111111;
  border-radius: 999px;
  padding: 8px 16px;
  font-weight: 600;
  display: inline-block;
  margin-bottom: 16px;
}

.formation-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 12px;
}

.formation-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.star {
  color: #f59e0b;
}

.info-card,
.content-card,
.price-card {
  background: white;
  border-radius: 12px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.info-item i {
  color: #c9a15b;
  font-size: 22px;
}

.info-item p {
  margin: 0;
  color: #666666;
  font-size: 14px;
}

.info-item strong {
  color: #111111;
}

.content-card h2 {
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: 700;
}

.content-card p {
  color: #b8792f;
  line-height: 1.8;
}

.centre-box {
  display: flex;
  align-items: center;
  gap: 16px;
}

.centre-icon {
  width: 60px;
  height: 60px;
  background: #f4f4f4;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c9a15b;
  font-size: 28px;
}

.centre-box h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
}

.centre-box p {
  margin: 0;
  color: #666666;
}

.price-card {
  position: sticky;
  top: 100px;
}

.price-card h3 {
  font-weight: 800;
  margin-bottom: 24px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
  gap: 12px;
}

.old-price {
  text-decoration: line-through;
  color: #666666;
}

.promo-box {
  border: 1px solid #f0b75b;
  border-radius: 8px;
  padding: 18px;
  margin: 22px 0;
}

.promo-badge {
  background: #dc2626;
  color: white;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 13px;
  font-weight: 700;
}

.final-price {
  color: #c9a15b;
  font-size: 34px;
  font-weight: 800;
  margin-top: 12px;
}

.small-text {
  color: #666666;
  font-size: 13px;
}

.btn-main {
  width: 100%;
  background: #c9a15b;
  color: #111111;
  border: none;
  border-radius: 8px;
  padding: 16px;
  font-weight: 800;
  margin-bottom: 14px;
  cursor: pointer;
}

.btn-main:hover {
  background: #b88d44;
}

.btn-secondary-custom {
  width: 100%;
  background: #f0f0f0;
  color: #111111;
  border: none;
  border-radius: 8px;
  padding: 16px;
  font-weight: 700;
  cursor: pointer;
}

.btn-secondary-custom:hover {
  background: #e0e0e0;
}

.included-list {
  list-style: none;
  padding: 0;
  margin: 18px 0 0;
}

.included-list li {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  color: #b8792f;
}

.included-list i {
  color: #c9a15b;
}

.alert-warning {
  background: #f8d7da;
  border-color: #f1aeb5;
  color: #58151c;
}

.alert-info {
  background: #e0f2fe;
  border-color: #7dd3fc;
  color: #0c4a6e;
}

@media (max-width: 768px) {
  .formation-title {
    font-size: 30px;
  }

  .hero-overlay {
    padding: 24px;
  }

  .price-card {
    position: static;
  }
}
</style>