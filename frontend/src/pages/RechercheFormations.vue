<template>
  <div>
    <div class="bg-white border-bottom py-4 mb-5">
      <div class="container-xl">
        <div class="search-pill-container">
          <div class="flex-grow-1 search-field-wrapper">
            <span class="material-symbols-outlined search-field-icon">search</span>
            <input 
              v-model="searchQuery" 
              class="search-pill-input" 
              placeholder="Formation, compétence, domaine..." 
              type="text" 
            />
          </div>

          <div class="search-pill-divider"></div>

          <div class="search-field-wrapper" style="min-width: 180px;">
            <span class="material-symbols-outlined search-field-icon">location_on</span>
            <select v-model="filtres.ville" class="form-select search-pill-select">
              <option value="all">Toutes les villes</option>
              <option value="Antananarivo">Antananarivo</option>
              <option value="Toamasina">Toamasina</option>
              <option value="Antsirabe">Antsirabe</option>
              <option value="Fianarantsoa">Fianarantsoa</option>
            </select>
          </div>

          <button class="btn-pill-submit" @click="handleSearch">
            <span class="material-symbols-outlined" style="font-size: 18px;">search</span>
            <span>Rechercher</span>
          </button>
        </div>
      </div>
    </div>

    <div class="container-xl mb-5">
      <div class="row g-4">
        
        <aside class="col-lg-3">
          <div class="card card-filter p-4 mb-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h2 class="h6 fw-bold m-0 d-flex align-items-center gap-2">
                <i class="bi bi-sliders"></i> Filtres
              </h2>
              <a class="text-decoration-none small text-muted" href="#" @click.prevent="clearFilters">
                Effacer tout
              </a>
            </div>

            <div class="mb-4">
              <label class="filter-section-title d-block">Catégories</label>
              <div v-if="categoriesDisponibles.length === 0" class="text-muted small">Aucune catégorie disponible.</div>
              <div
                v-for="categorie in categoriesDisponibles"
                :key="categorie"
                class="form-check mb-2"
              >
                <input
                  class="form-check-input"
                  type="checkbox"
                  :id="`categorie-${categorie}`"
                  :value="categorie"
                  v-model="filtres.categories"
                />

                <label class="form-check-label" :for="`categorie-${categorie}`">
                  {{ categorie }}
                </label>
              </div>
            </div>

            <div class="mb-4">
              <label class="filter-section-title d-block">Centre de formation</label>
              <select v-model="filtres.centre" class="form-select">
                <option value="">Tous les centres</option>
                <option
                  v-for="centre in centresDisponibles"
                  :key="centre"
                  :value="centre"
                >
                  {{ centre }}
                </option>
              </select>
            </div>

            <div class="mb-4">
              <h3 class="filter-section-title">Prix</h3>
              <div class="d-flex gap-2 mb-2">
                <input
                  v-model.number="filtres.prixMin"
                  type="number"
                  min="0"
                  class="form-control form-control-sm"
                  placeholder="Prix min"
                />
                <input
                  v-model.number="filtres.prixMax"
                  type="number"
                  min="0"
                  class="form-control form-control-sm"
                  placeholder="Prix max"
                />
              </div>
              <input
                v-model.number="filtres.prixMax"
                class="form-range"
                max="500000"
                min="0"
                step="50000"
                type="range"
              />
              <div class="d-flex justify-content-between text-muted small mt-2">
                <span>0 Ar</span>
                <span class="fw-bold text-dark">{{ formatPrice(filtres.prixMax || 0) }} Ar</span>
              </div>
            </div>

            <div>
              <h3 class="filter-section-title">Durée</h3>
              <div class="form-check mb-2">
                <input id="dur-short" v-model="durationFilters.short" class="form-check-input" type="checkbox" />
                <label class="form-check-label small" for="dur-short">&lt; 1 mois</label>
              </div>
              <div class="form-check mb-2">
                <input id="dur-med" v-model="durationFilters.medium" class="form-check-input" type="checkbox" />
                <label class="form-check-label small" for="dur-med">1 à 3 mois</label>
              </div>
              <div class="form-check mb-3">
                <input id="dur-long" v-model="durationFilters.long" class="form-check-input" type="checkbox" />
                <label class="form-check-label small" for="dur-long">&gt; 3 mois</label>
              </div>

              <div class="mb-3">
                <label class="filter-section-title d-block">Date de début</label>
                <input
                  v-model="filtres.dateDebut"
                  type="date"
                  class="form-control form-control-sm"
                />
              </div>

              <div class="form-check mb-2">
                <input id="places-disponibles" v-model="filtres.placesDisponibles" class="form-check-input" type="checkbox" />
                <label class="form-check-label small" for="places-disponibles">Places disponibles uniquement</label>
              </div>
            </div>

            <div class="d-grid gap-2 mt-4">
              <button class="btn btn-dark" @click="handleSearch">Appliquer les filtres</button>
            </div>
          </div>
        </aside>

        <main class="col-lg-9">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <p class="text-muted small m-0">
              <span class="fw-bold text-dark">{{ filteredFormations.length }}</span> formations trouvées
            </p>
            <div class="d-flex align-items-center gap-2">
              <label class="small text-muted text-nowrap" for="sort-select">Trier par :</label>
              <select id="sort-select" v-model="sortBy" class="form-select form-select-sm" style="width: 160px;">
                <option value="relevant">Les plus pertinentes</option>
                <option value="price-asc">Prix : les plus bas</option>
                <option value="price-desc">Prix : les plus hauts</option>
              </select>
            </div>
          </div>

          <div class="d-flex flex-column gap-3">
            <div v-if="chargement" class="text-center py-5 text-muted">
              Chargement des formations...
            </div>

            <div v-else-if="filteredFormations.length === 0" class="text-center py-5 text-muted">
              Aucune formation trouvée.
            </div>

            <div v-else>
              <div
                class="card card-result p-3"
                v-for="formation in filteredFormations"
                :key="formation.idFormation"
              >
                <div class="row g-3 align-items-center">
                  <div class="col-md-3">
                    <div class="result-img-placeholder">
                      <span class="material-symbols-outlined" style="font-size: 40px; opacity: 0.4">code</span>
                    </div>
                  </div>
                  <div class="col-md-5">
                    <span class="badge bg-light text-secondary mb-2" style="font-size: 11px;">
                      {{ formation.categorie || 'Sans catégorie' }}
                    </span>
                    <h2 class="h5 fw-bold mb-1">{{ formation.titre }}</h2>
                    <p class="text-muted small mb-3">{{ formation.centre || formation.nomCentre || formation.ecole || 'Centre inconnu' }}</p>
                    <div class="d-flex flex-wrap gap-3 text-muted" style="font-size: 12px;">
                      <span class="d-flex align-items-center gap-1"><i class="bi bi-geo-alt"></i> {{ formation.ville || formation.lieu || 'Lieu inconnu' }}</span>
                      <span class="d-flex align-items-center gap-1"><i class="bi bi-clock"></i> {{ formation.duree || 'Durée inconnue' }}</span>
                      <span class="d-flex align-items-center gap-1"><i class="bi bi-people"></i> {{ formation.placesDisponibles || formation.placesRestantes || 0 }} places restantes</span>
                    </div>
                  </div>
                  <div class="col-md-4 text-md-end d-flex flex-md-column justify-content-between align-items-center align-items-md-end mt-3 mt-md-0">
                    <div>
                      <p class="h4 fw-bold text-brand-orange mb-0">{{ formatPrice(formation.prixRemise || formation.prix) }} Ar</p>
                      <div class="d-flex align-items-center justify-content-md-end gap-1 small">
                        <i class="bi bi-star-fill text-brand-gold"></i>
                        <span class="fw-bold">{{ formation.noteMoyenne ?? 0 }}</span>
                        <span class="text-muted" style="font-size: 11px;">({{ formation.nbAvis ?? 0 }})</span>
                      </div>
                    </div>
                    <button class="btn btn-dark fw-semibold px-4 py-2 mt-md-4" @click="goToDetail(formation.idFormation)">
                      Voir détail
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { obtenirFormations } from '../api/formations.js'

const router = useRouter()

const searchQuery = ref('')
const filtres = reactive({
  categories: [],
  ville: '',
  prixMin: '',
  prixMax: '',
  duree: '',
  centre: '',
  dateDebut: '',
  placesDisponibles: false
})
const sortBy = ref('relevant')
const durationFilters = reactive({
  short: false,
  medium: false,
  long: false
})
const formations = ref([])
const chargement = ref(false)

const categoriesDisponibles = computed(() => {
  return [
    ...new Set(
      formations.value
        .map((formation) => formation.categorie)
        .filter(Boolean)
    )
  ]
})

const centresDisponibles = computed(() => {
  return [
    ...new Set(
      formations.value
        .map((formation) => formation.centre || formation.nomCentre || formation.ecole)
        .filter(Boolean)
    )
  ]
})

const filteredFormations = computed(() => {
  let result = [...formations.value]
  const query = searchQuery.value?.trim().toLowerCase() || ''

  if (query) {
    result = result.filter((formation) => {
      return [
        formation.titre,
        formation.description,
        formation.categorie,
        formation.centre,
        formation.ville,
        formation.lieu
      ]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(query))
    })
  }

  if (filtres.categories.length > 0) {
    result = result.filter(
      (formation) => formation.categorie && filtres.categories.includes(formation.categorie)
    )
  }

  if (filtres.ville && filtres.ville !== 'all') {
    result = result.filter((formation) => {
      const ville = (formation.ville || formation.lieu || '').toLowerCase()
      return ville.includes(filtres.ville.toLowerCase())
    })
  }

  if (filtres.centre) {
    result = result.filter((formation) => {
      const centre = (formation.centre || formation.nomCentre || formation.ecole || '').toLowerCase()
      return centre.includes(filtres.centre.toLowerCase())
    })
  }

  if (filtres.prixMin !== '') {
    result = result.filter((formation) => Number(formation.prix || 0) >= Number(filtres.prixMin))
  }

  if (filtres.prixMax !== '') {
    result = result.filter((formation) => Number(formation.prix || 0) <= Number(filtres.prixMax))
  }

  const anyDurationSelected = durationFilters.short || durationFilters.medium || durationFilters.long
  if (anyDurationSelected) {
    result = result.filter((formation) => {
      const months = getMonthsFromDuree(formation.duree)
      if (months === null) {
        return true
      }

      if (durationFilters.short && months < 1) return true
      if (durationFilters.medium && months >= 1 && months <= 3) return true
      if (durationFilters.long && months > 3) return true

      return false
    })
  }

  if (filtres.dateDebut) {
    result = result.filter((formation) => {
      return formation.dateDebut && formation.dateDebut.startsWith(filtres.dateDebut)
    })
  }

  if (filtres.placesDisponibles) {
    result = result.filter((formation) => Number(formation.placesDisponibles || formation.placesRestantes || 0) > 0)
  }

  switch (sortBy.value) {
    case 'price-asc':
      return result.sort((a, b) => Number(a.prix || 0) - Number(b.prix || 0))
    case 'price-desc':
      return result.sort((a, b) => Number(b.prix || 0) - Number(a.prix || 0))
    default:
      return result
  }
})

const loadFormations = async () => {
  chargement.value = true

  try {
    formations.value = await obtenirFormations()
  } catch (error) {
    console.error('Impossible de charger les formations', error)
    formations.value = []
  } finally {
    chargement.value = false
  }
}

onMounted(loadFormations)

const handleSearch = () => {
  // Les filtres sont appliqués automatiquement via filteredFormations
}

const clearFilters = () => {
  searchQuery.value = ''
  filtres.categories = []
  filtres.ville = ''
  filtres.prixMin = ''
  filtres.prixMax = ''
  filtres.duree = ''
  filtres.centre = ''
  filtres.dateDebut = ''
  filtres.placesDisponibles = false
  durationFilters.short = false
  durationFilters.medium = false
  durationFilters.long = false
  sortBy.value = 'relevant'
}

const formatPrice = (value) => {
  return Number(value || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
}

const getMonthsFromDuree = (duree) => {
  if (!duree) return null

  const mois = String(duree).match(/(\d+)\s*mois?/i)
  if (mois) {
    return Number(mois[1])
  }

  const semaines = String(duree).match(/(\d+)\s*semaines?/i)
  if (semaines) {
    return Number(semaines[1]) / 4
  }

  return null
}

const goToDetail = (id) => {
  router.push(`/formations/${id}`)
}
</script>

<style scoped>
:root {
  --primary-color: #c59d5f;
  --surface-color: #fbf9f8;
  --surface-dim: #dbdad9;
  --dark-color: #1a1a1a;
  --brand-dark: #1a1a1a;
  --brand-gold: #c49a3d;
  --brand-orange: #d97706;
  --border-radius-custom: 8px;
}

/* BARRE DE RECHERCHE PRINCIPALE (Pilule) */
.search-pill-container {
  background-color: #ffffff;
  border-radius: 99px;
  padding: 0.35rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
}

.search-field-wrapper {
  display: flex;
  align-items: center;
  padding: 0 1.25rem;
  height: 44px;
}

.search-field-icon {
  color: #94a3b8;
  margin-right: 0.75rem;
  font-size: 20px;
  flex-shrink: 0;
}

.search-pill-input {
  border: none;
  width: 100%;
  font-size: 0.95rem;
  color: #1e293b;
  background: transparent;
}

.search-pill-input:focus {
  outline: none;
}

.search-pill-divider {
  width: 1px;
  height: 24px;
  background-color: #e2e8f0;
  flex-shrink: 0;
}

.search-pill-select {
  border: none;
  background: transparent;
  font-size: 0.95rem;
  color: #1e293b;
  font-weight: 500;
  width: 100%;
  cursor: pointer;
}

.search-pill-select:focus {
  outline: none;
  box-shadow: none;
}

.btn-pill-submit {
  background-color: #111111;
  color: #ffffff;
  font-weight: 600;
  font-size: 0.95rem;
  border: none;
  border-radius: 99px;
  height: 44px;
  padding: 0 1.5rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
}

.btn-pill-submit:hover {
  background-color: #2d3748;
}

/* FILTRES & RÉSULTATS */
.filter-section-title {
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  font-weight: 700;
  margin-bottom: 1rem;
}

.card-filter {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.custom-select-filter {
  background-color: #ffffff; 
  border-color: rgba(0,0,0,0.12); 
  color: #1a1a1a;
}

.card-result {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.card-result:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.04);
}

.result-img-placeholder {
  width: 100%;
  height: 140px;
  background: linear-gradient(135deg, #0f2a41, #1e3a8a);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.result-img-placeholder.orange {
  background: linear-gradient(135deg, #7c2d12, #d97706);
}

.form-check-input:checked {
  background-color: #c59d5f;
  border-color: #c59d5f;
}

.btn-dark {
  background-color: #1a1a1a;
  border: none;
  border-radius: 8px;
}

.btn-dark:hover {
  background-color: #2d3748;
}

.text-brand-orange {
  color: #d97706;
}

.text-brand-gold {
  color: #c49a3d;
}

/* RESPONSIVE IPAD / MOBILE */
@media (max-width: 767.98px) {
  .search-pill-container {
    flex-direction: column;
    border-radius: 16px;
    padding: 0.5rem;
  }

  .search-field-wrapper {
    width: 100%;
    padding: 0 0.5rem;
  }

  .search-pill-divider {
    display: none;
  }

  .btn-pill-submit {
    width: 100%;
    justify-content: center;
    margin-top: 0.5rem;
    border-radius: 8px;
  }
}
</style>