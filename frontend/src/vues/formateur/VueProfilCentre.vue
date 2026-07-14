<template>
  <div>
    <h2 class="fw-bold mb-4">Mon profil centre</h2>

    <div v-if="loading" class="alert alert-info">Chargement...</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-else class="card border-0 shadow-sm">
      <div class="card-body">
        <div v-if="success" class="alert alert-success">
          {{ success }}
        </div>

        <div class="alert alert-warning">
          Toute modification doit être validée par un administrateur avant publication.
        </div>

        <form @submit.prevent="soumettre">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Nom du centre</label>
              <input v-model="form.nom" class="form-control" />
            </div>

            <div class="col-md-6">
              <label class="form-label">Ville / lieu</label>
              <input v-model="form.ville" class="form-control" />
            </div>

            <div class="col-12">
              <label class="form-label">Description</label>
              <textarea v-model="form.description" class="form-control" rows="4"></textarea>
            </div>

            <div class="col-md-6">
              <label class="form-label">Téléphone</label>
              <input v-model="form.telephone" class="form-control" />
            </div>

            <div class="col-md-6">
              <label class="form-label">Email</label>
              <input v-model="form.email" class="form-control" />
            </div>

            <div class="col-md-6">
              <label class="form-label">Site web</label>
              <input v-model="form.siteWeb" class="form-control" />
            </div>

            <div class="col-md-6">
              <label class="form-label">Compte Mobile Money</label>
              <input v-model="form.compteMobileMoney" class="form-control" />
            </div>
          </div>

          <button class="btn btn-dark mt-4" type="submit">
            Soumettre les modifications
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue"
import {
  getProfilCentreFormateur,
  modifierProfilCentreFormateur
} from "../../api/formateurApi"

const form = ref({})
const loading = ref(false)
const error = ref("")
const success = ref("")

async function chargerProfil() {
  loading.value = true
  error.value = ""

  try {
    form.value = await getProfilCentreFormateur()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function soumettre() {
  success.value = ""
  error.value = ""

  try {
    const result = await modifierProfilCentreFormateur(form.value)
    success.value = result.message || "Demande envoyée."
  } catch (e) {
    error.value = e.message
  }
}

onMounted(() => {
  chargerProfil()
})
</script>