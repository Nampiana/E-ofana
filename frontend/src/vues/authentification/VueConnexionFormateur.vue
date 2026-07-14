<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-12 col-md-6 col-lg-5">
        <div class="card border-0 shadow">
          <div class="card-body p-4">
            <h1 class="h3 fw-bold text-center mb-2">Connexion formateur</h1>

            <p class="text-muted text-center mb-4">
              Connectez-vous à votre espace centre de formation.
            </p>

            <div v-if="error" class="alert alert-danger">
              {{ error }}
            </div>

            <form @submit.prevent="submitLogin">
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input
                  v-model="email"
                  type="email"
                  class="form-control"
                  placeholder="formateur@test.com"
                  required
                />
              </div>

              <div class="mb-3">
                <label class="form-label">Mot de passe</label>
                <input
                  v-model="motDePasse"
                  type="password"
                  class="form-control"
                  placeholder="12345678"
                  required
                />
              </div>

              <button class="btn btn-dark w-100" type="submit" :disabled="loading">
                <span v-if="loading">Connexion...</span>
                <span v-else>Se connecter</span>
              </button>
            </form>

            <p class="text-muted small mt-4 mb-0">
              Vous n'avez pas de compte ? Contactez votre interlocuteur commercial E-OFANA.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from "vue"
import { useRouter } from "vue-router"
import { useAuthFormateurStore } from "../../../stores/authFormateurStore"

const router = useRouter()
const authStore = useAuthFormateurStore()

const email = ref("")
const motDePasse = ref("")

const loading = computed(() => authStore.loading)
const error = computed(() => authStore.error)

async function submitLogin() {
  const ok = await authStore.login(email.value, motDePasse.value)

  if (ok) {
    router.push("/formateur/tableau-de-bord")
  }
}
</script>