<template>
  <div class="formateur-layout">
    <aside class="sidebar">
      <h3 class="logo">E-OFANA</h3>

      <div class="centre-box">
        <div class="avatar">{{ initials }}</div>
        <div>
          <div class="small">Centre connecté</div>
          <strong>{{ displayName }}</strong>
        </div>
      </div>

      <nav class="menu">
        <router-link to="/formateur/tableau-de-bord">Tableau de bord</router-link>
        <router-link to="/formateur/formations">Mes formations</router-link>
        <router-link to="/formateur/profil-centre">Mon profil</router-link>
        <router-link to="/formateur/finances">Finances</router-link>

        <button class="logout-btn" @click="logout">
          Déconnexion
        </button>
      </nav>
    </aside>

    <main class="main-content">
      <header class="topbar">
        <h1>Espace formateur</h1>
        <span>{{ displayName }}</span>
      </header>

      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useAuthFormateurStore } from "../stores/authFormateurStore"

const router = useRouter()
const authStore = useAuthFormateurStore()

onMounted(() => {
  authStore.chargerDepuisStorage()
})

const displayName = computed(() => {
  const f = authStore.formateur

  if (!f) {
    return "Formateur"
  }

  return `${f.prenom || ""} ${f.nom || ""}`.trim() || f.email || "Formateur"
})

const initials = computed(() => {
  const name = displayName.value || "F"
  return name.charAt(0).toUpperCase()
})

async function logout() {
  await authStore.logout()
  router.push("/formateur/connexion")
}
</script>

<style scoped>
.formateur-layout {
  min-height: 100vh;
  display: flex;
  background: #f5f5f5;
}

.sidebar {
  width: 270px;
  background: #111111;
  color: white;
  padding: 24px;
}

.logo {
  font-weight: 800;
  margin-bottom: 32px;
}

.centre-box {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 32px;
  padding: 12px;
  background: #1f1f1f;
  border-radius: 12px;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #c9a15b;
  color: #111111;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.small {
  color: #bbbbbb;
  font-size: 12px;
}

.menu {
  display: grid;
  gap: 10px;
}

.menu a,
.logout-btn {
  text-decoration: none;
  color: white;
  background: #222222;
  border: none;
  border-radius: 8px;
  padding: 12px 14px;
  text-align: left;
  font-weight: 600;
  cursor: pointer;
}

.menu a.router-link-active {
  background: #c9a15b;
  color: #111111;
}

.logout-btn {
  background: #dc2626;
  margin-top: 20px;
}

.main-content {
  flex: 1;
}

.topbar {
  background: white;
  padding: 18px 28px;
  border-bottom: 1px solid #dddddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.topbar h1 {
  font-size: 20px;
  margin: 0;
  font-weight: 800;
}

.content {
  padding: 28px;
}

@media (max-width: 768px) {
  .formateur-layout {
    display: block;
  }

  .sidebar {
    width: 100%;
  }
}
</style>