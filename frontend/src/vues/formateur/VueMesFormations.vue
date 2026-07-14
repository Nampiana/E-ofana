<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold mb-0">Mes formations</h2>

      <router-link class="btn btn-dark" to="/formateur/formations/nouveau">
        + Publier une formation
      </router-link>
    </div>

    <div class="mb-3 d-flex gap-2 flex-wrap">
      <button class="btn btn-sm btn-outline-dark" @click="filtrer('')">Toutes</button>
      <button class="btn btn-sm btn-outline-warning" @click="filtrer('EN_ATTENTE')">En attente</button>
      <button class="btn btn-sm btn-outline-success" @click="filtrer('APPROUVE')">Approuvées</button>
      <button class="btn btn-sm btn-outline-danger" @click="filtrer('REJETE')">Rejetées</button>
    </div>

    <div v-if="loading" class="alert alert-info">Chargement...</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-else class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead>
            <tr>
              <th>Titre</th>
              <th>Statut</th>
              <th>Prix</th>
              <th>Inscrits</th>
              <th>Places restantes</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="formation in formations" :key="formation.idFormation || formation.id">
              <td class="fw-bold">{{ formation.titre }}</td>
              <td>
                <span class="badge bg-secondary">
                  {{ formation.statut || "EN_ATTENTE" }}
                </span>
              </td>
              <td>{{ formatMontant(formation.prix) }}</td>
              <td>{{ formation.nbInscrits || formation.totalInscrits || 0 }}</td>
              <td>{{ formation.placesRestantes ?? "-" }}</td>
              <td class="d-flex gap-2">
                <router-link
                  class="btn btn-sm btn-outline-primary"
                  :to="`/formateur/formations/${formation.idFormation || formation.id}/statistiques`"
                >
                  Stats
                </router-link>

                <router-link
                  class="btn btn-sm btn-outline-dark"
                  :to="`/formateur/formations/${formation.idFormation || formation.id}/inscrits`"
                >
                  Inscrits
                </router-link>

                <button class="btn btn-sm btn-outline-danger" @click="supprimer(formation.idFormation || formation.id)">
                  Supprimer
                </button>
              </td>
            </tr>

            <tr v-if="formations.length === 0">
              <td colspan="6" class="text-center text-muted py-4">
                Aucune formation trouvée.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import {
  getMesFormationsFormateur,
  supprimerFormationFormateur
} from "../../api/formateurApi"

const formations = ref([])
const loading = ref(false)
const error = ref("")
const statutActif = ref("")

function formatMontant(value) {
  return `${Number(value || 0).toLocaleString("fr-FR")} Ar`
}

async function chargerFormations() {
  loading.value = true
  error.value = ""

  try {
    formations.value = await getMesFormationsFormateur(statutActif.value)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function filtrer(statut) {
  statutActif.value = statut
  chargerFormations()
}

async function supprimer(id) {
  if (!confirm("Voulez-vous vraiment supprimer cette formation ?")) {
    return
  }

  try {
    await supprimerFormationFormateur(id)
    await chargerFormations()
  } catch (e) {
    alert(e.message)
  }
}

onMounted(() => {
  chargerFormations()
})
</script>