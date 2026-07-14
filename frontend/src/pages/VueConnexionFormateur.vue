<template>
  <div class="connexion-page">
    <div class="connexion-card">
      <div class="icon-box">🎓</div>

      <h1>Connexion à E-OFANA</h1>
      <p class="subtitle">Bienvenue ! Connectez-vous à votre espace.</p>

      <div class="role-tabs">
        <button type="button" class="tab">Apprenant</button>
        <button type="button" class="tab active">Formateur</button>
        <button type="button" class="tab">Admin</button>
      </div>

      <div v-if="errorMessage" class="alert-error">
        ⚠️ {{ errorMessage }}
      </div>

      <form @submit.prevent="seConnecter">
        <div class="form-group">
          <label>Adresse email</label>
          <input
            v-model="email"
            type="email"
            placeholder="Adresse email"
            required
          />
        </div>

        <div class="form-group">
          <label>Mot de passe</label>
          <input
            v-model="motDePasse"
            type="password"
            placeholder="Mot de passe"
            required
          />
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? "Connexion..." : "Se connecter  >" }}
        </button>
      </form>

      <p class="info">
        Vous n'avez pas de compte ? Contactez votre interlocuteur commercial E-ofana.
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const email = ref("tsiky@gmail.com");
const motDePasse = ref("789456123");
const loading = ref(false);
const errorMessage = ref("");

async function seConnecter() {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await fetch("http://localhost:8081/api/v1/auth-formateur/connexion", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email: email.value,
        motDePasse: motDePasse.value,
        password: motDePasse.value,
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "Connexion formateur échouée");
    }

    if (!data.token) {
      throw new Error("Token non reçu depuis le backend");
    }

    localStorage.setItem("tokenFormateur", data.token);
    localStorage.setItem("formateur", JSON.stringify(data));

    console.log("Connexion formateur réussie :", data);

    router.push("/formateur/tableau-de-bord");
  } catch (error) {
    console.error("Erreur connexion formateur :", error);
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.connexion-page {
  min-height: calc(100vh - 80px);
  background: #f5f5f5;
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.connexion-card {
  width: 100%;
  max-width: 520px;
  background: white;
  padding: 40px;
  border-radius: 8px;
}

.icon-box {
  width: 64px;
  height: 64px;
  background: #111;
  color: #d4a851;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  border-radius: 8px;
  font-size: 28px;
}

h1 {
  text-align: center;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 28px;
}

.role-tabs {
  display: flex;
  background: #f1f1f4;
  border-radius: 10px;
  padding: 6px;
  margin-bottom: 24px;
}

.tab {
  flex: 1;
  border: none;
  padding: 12px;
  background: transparent;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.tab.active {
  background: #111;
  color: white;
}

.alert-error {
  background: #fee2e2;
  color: #991b1b;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 18px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
}

input {
  width: 100%;
  height: 52px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 14px;
  font-size: 15px;
}

.btn-login {
  width: 100%;
  height: 54px;
  background: #111;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 12px;
}

.btn-login:disabled {
  opacity: 0.7;
}

.info {
  text-align: center;
  color: #666;
  margin-top: 24px;
}
</style>