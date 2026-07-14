<template>
  <div>
    <h2 class="fw-bold mb-4">Finances</h2>

    <div v-if="loading" class="alert alert-info">Chargement...</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-else>
      <div class="row g-3 mb-4">
        <div class="col-md-3">
          <div class="card-stat">
            <span>Revenus bruts</span>
            <strong>{{ formatMontant(finances.revenusBruts || finances.montantBrut) }}</strong>
          </div>
        </div>

        <div class="col-md-3">
          <div class="card-stat">
            <span>Commissions</span>
            <strong>{{ formatMontant(finances.commissionsPrelevees || finances.commissionPrelevee) }}</strong>
          </div>
        </div>

        <div class="col-md-3">
          <div class="card-stat">
            <span>Revenus nets</span>
            <strong>{{ formatMontant(finances.revenusNets || finances.montantNet) }}</strong>
          </div>
        </div>

        <div class="col-md-3">
          <div class="card-stat">
            <span>Prochain virement</span>
            <strong>{{ formatMontant(finances.prochainVirementMontantEstime) }}</strong>
          </div>
        </div>
      </div>

      <div class="card border-0 shadow-sm">
        <div class="card-header bg-white fw-bold">
          Historique des virements
        </div>

        <div class="table-responsive">
          <table class="table mb-0 align-middle">
            <thead>
              <tr>
                <th>Date</th>
                <th>Montant net</th>
                <th>Opérateur</th>
                <th>Statut</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="virement in virements" :key="virement.idVirement || virement.id">
                <td>{{ virement.dateVirement || virement.date || "-" }}</td>
                <td>{{ formatMontant(virement.montantNet) }}</td>
                <td>{{ virement.operateur || "-" }}</td>
                <td>
                  <span class="badge bg-secondary">
                    {{ virement.statut || "-" }}
                  </span>
                </td>
              </tr>

              <tr v-if="virements.length === 0">
                <td colspan="4" class="text-center text-muted py-4">
                  Aucun virement.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import {
  getFinancesFormateur,
  getVirementsFormateur
} from "../../api/formateurApi"

const finances = ref({})
const virements = ref([])
const loading = ref(false)
const error = ref("")

function formatMontant(value) {
  return `${Number(value || 0).toLocaleString("fr-FR")} Ar`
}

async function chargerFinances() {
  loading.value = true
  error.value = ""

  try {
    finances.value = await getFinancesFormateur()
    virements.value = await getVirementsFormateur()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  chargerFinances()
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
  font-size: 22px;
}
</style>