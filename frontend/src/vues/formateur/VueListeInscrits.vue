<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold mb-0">Liste des inscrits</h2>

      <button class="btn btn-dark" @click="exporterPdf">
        Exporter PDF
      </button>
    </div>

    <input
      v-model="search"
      class="form-control mb-3"
      placeholder="Rechercher par nom..."
    />

    <div v-if="loading" class="alert alert-info">Chargement...</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-else class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table mb-0 align-middle">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Prénom</th>
              <th>Date inscription</th>
              <th>Statut paiement</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="inscrit in inscritsFiltres" :key="`${inscrit.nom}-${inscrit.prenom}-${inscrit.dateInscription}`">
              <td>{{ inscrit.nom }}</td>
              <td>{{ inscrit.prenom }}</td>
              <td>{{ inscrit.dateInscription || inscrit.createdAt || "-" }}</td>
              <td>
                <span
                  class="badge"
                  :class="isPaye(inscrit) ? 'bg-success' : 'bg-warning text-dark'"
                >
                  {{ isPaye(inscrit) ? "Payé" : "Réservé" }}
                </span>
              </td>
            </tr>

            <tr v-if="inscritsFiltres.length === 0">
              <td colspan="4" class="text-center text-muted py-4">
                Aucun inscrit.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import {
  getInscritsFormation,
  exporterInscritsPdf
} from "../../api/formateurApi"

const route = useRoute()
const idFormation = route.params.id

const inscrits = ref([])
const search = ref("")
const loading = ref(false)
const error = ref("")

const inscritsFiltres = computed(() => {
  const q = search.value.trim().toLowerCase()

  if (!q) {
    return inscrits.value
  }

  return inscrits.value.filter((i) =>
    `${i.nom || ""} ${i.prenom || ""}`.toLowerCase().includes(q)
  )
})

function isPaye(inscrit) {
  return inscrit.statutPaiement === "VALIDE" || inscrit.statutPaiement === "PAYE" || inscrit.statut === "VALIDE"
}

async function chargerInscrits() {
  loading.value = true
  error.value = ""

  try {
    inscrits.value = await getInscritsFormation(idFormation)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function exporterPdf() {
  try {
    await exporterInscritsPdf(idFormation)
  } catch (e) {
    alert(e.message)
  }
}

onMounted(() => {
  chargerInscrits()
})
</script>