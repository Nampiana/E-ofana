<template>
  <div>
    <h2 class="fw-bold mb-4">Tableau de bord</h2>

    <div v-if="loading" class="alert alert-info">Chargement...</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-else>
      <div class="row g-3 mb-4">
        <div class="col-12 col-md-3">
          <div class="card-stat">
            <span>Total inscrits</span>
            <strong>{{ dashboard.nbTotalInscrits || dashboard.totalInscrits || 0 }}</strong>
          </div>
        </div>

        <div class="col-12 col-md-3">
          <div class="card-stat">
            <span>CA net du mois</span>
            <strong>{{ formatMontant(dashboard.chiffreAffairesNetMoisEnCours || dashboard.caNetMois || 0) }}</strong>
          </div>
        </div>

        <div class="col-12 col-md-3">
          <div class="card-stat">
            <span>Total visites</span>
            <strong>{{ dashboard.nbTotalVisites ?? "Indisponible" }}</strong>
          </div>
        </div>

        <div class="col-12 col-md-3">
          <div class="card-stat">
            <span>Taux inscription</span>
            <strong>{{ dashboard.tauxInscription ?? "Indisponible" }}</strong>
          </div>
        </div>
      </div>

      <div v-if="dashboard.motifIndisponibilite" class="alert alert-warning">
        {{ dashboard.motifIndisponibilite }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import { getDashboardFormateur } from "../../api/formateurApi"

const dashboard = ref({})
const loading = ref(false)
const error = ref("")

function formatMontant(value) {
  return `${Number(value || 0).toLocaleString("fr-FR")} Ar`
}

async function chargerDashboard() {
  loading.value = true
  error.value = ""

  try {
    dashboard.value = await getDashboardFormateur()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  chargerDashboard()
})
</script>

<style scoped>
.card-stat {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.card-stat span {
  display: block;
  color: #666666;
  margin-bottom: 8px;
}

.card-stat strong {
  font-size: 24px;
}
</style>