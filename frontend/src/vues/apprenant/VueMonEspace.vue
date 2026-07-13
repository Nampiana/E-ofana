<template>
  <div class="apprenant-space">
    <section class="hero">
      <div class="avatar">{{ initials }}</div>

      <div>
        <h1>Bienvenue, {{ displayName }} !</h1>
        <p>Gérez votre espace apprenant</p>

        <div class="hero-actions">
          <button @click="goAccueil">
            <i class="bi bi-house-door"></i>
            Accueil
          </button>

          <button @click="goRecherche">
            <i class="bi bi-search"></i>
            Recherche
          </button>
        </div>
      </div>
    </section>

    <section class="space-body">
      <aside class="sidebar">
        <button
          class="menu-item"
          :class="{ active: activeTab === 'overview' }"
          @click="activeTab = 'overview'"
        >
          <i class="bi bi-grid-fill"></i>
          Vue d'ensemble
        </button>

        <button
          class="menu-item"
          :class="{ active: activeTab === 'inscriptions' }"
          @click="activeTab = 'inscriptions'"
        >
          <i class="bi bi-card-checklist"></i>
          Mes inscriptions
        </button>

        <button
          class="menu-item"
          :class="{ active: activeTab === 'recus' }"
          @click="activeTab = 'recus'"
        >
          <i class="bi bi-receipt"></i>
          Mes reçus
        </button>

        <button
          class="menu-item"
          :class="{ active: activeTab === 'courses' }"
          @click="activeTab = 'courses'"
        >
          <i class="bi bi-book-fill"></i>
          Mes formations
        </button>

        <button
          class="menu-item"
          :class="{ active: activeTab === 'certificates' }"
          @click="activeTab = 'certificates'"
        >
          <i class="bi bi-award-fill"></i>
          Certificats
        </button>

        <button
          class="menu-item"
          :class="{ active: activeTab === 'profile' }"
          @click="activeTab = 'profile'"
        >
          <i class="bi bi-person-gear"></i>
          Mon profil
        </button>

        <button
          class="menu-item"
          :class="{ active: activeTab === 'settings' }"
          @click="activeTab = 'settings'"
        >
          <i class="bi bi-gear-fill"></i>
          Paramètres
        </button>

        <hr />

        <button class="logout-btn" @click="logout">
          <i class="bi bi-box-arrow-right"></i>
          Déconnexion
        </button>
      </aside>

      <main class="content">
        <div v-if="loading" class="content-card">
          Chargement...
        </div>

        <div v-else-if="apiError" class="content-card error-box">
          {{ apiError }}
        </div>

        <!-- Vue d'ensemble -->
        <div v-else-if="activeTab === 'overview'" class="content-card">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon black">
                <i class="bi bi-book-fill"></i>
              </div>

              <div>
                <h3>{{ stats.formationsEnCours }}</h3>
                <p>Formations en cours</p>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon green">
                <i class="bi bi-check-circle-fill"></i>
              </div>

              <div>
                <h3>{{ stats.formationsTerminees }}</h3>
                <p>Formations terminées</p>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon gold">
                <i class="bi bi-award-fill"></i>
              </div>

              <div>
                <h3>{{ stats.certificats }}</h3>
                <p>Certificats obtenus</p>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon gray">
                <i class="bi bi-clock-fill"></i>
              </div>

              <div>
                <h3>{{ stats.tempsFormation }}</h3>
                <p>Temps de formation</p>
              </div>
            </div>
          </div>

          <h2 class="section-title">
            <i class="bi bi-clock-history"></i>
            Activité récente
          </h2>

          <div v-if="inscriptions.length === 0" class="empty-state">
            <h3>Aucune activité récente</h3>
            <p>Vos activités apparaîtront ici après une inscription.</p>
          </div>

          <div v-else class="activity-list">
            <div
              v-for="inscription in inscriptions.slice(0, 3)"
              :key="inscription.id"
              class="activity-item"
            >
              <div class="activity-icon">
                <i class="bi bi-play-circle-fill"></i>
              </div>

              <div class="activity-content">
                <h4>{{ inscription.formation }}</h4>
                <p>{{ formatDate(inscription.date) }} • {{ getStatusLabel(inscription.statut) }}</p>
              </div>

              <div class="activity-progress">
                <div class="progress">
                  <div
                    class="progress-bar"
                    :style="{ width: `${inscription.progression || 0}%` }"
                  ></div>
                </div>

                <small>{{ inscription.progression || 0 }}%</small>
              </div>
            </div>
          </div>
        </div>

        <!-- Mes inscriptions -->
        <div v-else-if="activeTab === 'inscriptions'" class="content-card">
          <h2 class="section-title">
            <i class="bi bi-card-checklist"></i>
            Mes inscriptions
          </h2>

          <div v-if="inscriptions.length === 0" class="empty-state">
            <h3>Aucune inscription</h3>
            <p>Vos inscriptions apparaîtront ici.</p>
          </div>

          <table v-else class="data-table">
            <thead>
              <tr>
                <th>Formation</th>
                <th>Date</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="inscription in inscriptions" :key="inscription.id">
                <td>
                  <strong>{{ inscription.formation }}</strong>
                  <br />
                  <small>{{ inscription.centre }}</small>
                </td>

                <td>{{ formatDate(inscription.date) }}</td>

                <td>
                  <span class="badge" :class="getStatusClass(inscription.statut)">
                    {{ getStatusLabel(inscription.statut) }}
                  </span>
                </td>

                <td>
                  <button class="small-btn" @click="viewDetails(inscription)">
                    <i class="bi bi-eye"></i>
                  </button>

                  <button
                    v-if="inscription.numeroRecu"
                    class="small-btn success"
                    @click="downloadRecu(inscription)"
                  >
                    <i class="bi bi-download"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Mes reçus -->
        <div v-else-if="activeTab === 'recus'" class="content-card">
          <h2 class="section-title">
            <i class="bi bi-receipt"></i>
            Mes reçus
          </h2>

          <div v-if="recus.length === 0" class="empty-state">
            <h3>Aucun reçu disponible</h3>
            <p>Vos reçus apparaîtront ici après un paiement.</p>
          </div>

          <table v-else class="data-table">
            <thead>
              <tr>
                <th>Référence</th>
                <th>Formation</th>
                <th>Date</th>
                <th>Montant</th>
                <th>Méthode</th>
                <th>Statut</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="recu in recus" :key="recu.id">
                <td class="reference">{{ recu.reference }}</td>
                <td>{{ recu.formation }}</td>
                <td>{{ formatDate(recu.date) }}</td>
                <td>
                  <strong>{{ formatPrice(recu.montant) }} Ar</strong>
                </td>
                <td>{{ recu.methode }}</td>
                <td>
                  <span class="badge" :class="getStatusClass(recu.statut)">
                    {{ getStatusLabel(recu.statut) }}
                  </span>
                </td>
                <td>
                  <button class="download-btn" @click="downloadRecu(recu)">
                    <i class="bi bi-download"></i>
                    Télécharger
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Mes formations -->
        <div v-else-if="activeTab === 'courses'" class="content-card">
          <h2 class="section-title">
            <i class="bi bi-book-fill"></i>
            Mes formations
          </h2>

          <div v-if="inscriptions.length === 0" class="empty-state">
            <h3>Aucune formation suivie</h3>
            <p>Vos formations apparaîtront ici après une inscription.</p>
          </div>

          <div v-else class="courses-grid">
            <div
              v-for="inscription in inscriptions"
              :key="inscription.id"
              class="course-card"
            >
              <div class="course-image">
                <span>{{ inscription.categorie }}</span>
              </div>

              <div class="course-content">
                <h3>{{ inscription.formation }}</h3>

                <p>{{ inscription.description || 'Formation suivie sur E-OFANA' }}</p>

                <div class="course-meta">
                  <span>
                    <i class="bi bi-clock"></i>
                    {{ inscription.duree || 'Durée non définie' }}
                  </span>

                  <span>
                    <i class="bi bi-geo-alt"></i>
                    {{ inscription.lieu || inscription.ville || 'Lieu non défini' }}
                  </span>
                </div>

                <div class="progress">
                  <div
                    class="progress-bar"
                    :style="{ width: `${inscription.progression || 0}%` }"
                  ></div>
                </div>

                <p>{{ inscription.progression || 0 }}% complété</p>

                <button class="continue-btn" @click="viewDetails(inscription)">
                  Continuer
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Certificats -->
        <div v-else-if="activeTab === 'certificates'" class="content-card">
          <h2 class="section-title">
            <i class="bi bi-award-fill"></i>
            Mes certificats
          </h2>

          <div class="empty-state">
            <h3>Aucun certificat disponible</h3>
            <p>Vos certificats apparaîtront ici après avoir terminé une formation.</p>
          </div>
        </div>

        <!-- Profil -->
        <div v-else-if="activeTab === 'profile'" class="content-card">
          <h2 class="section-title">
            <i class="bi bi-person-gear"></i>
            Mon profil
          </h2>

          <div class="profile-box">
            <p><strong>Nom :</strong> {{ displayName }}</p>
            <p><strong>Membre depuis :</strong> {{ memberSince }}</p>
            <p><strong>Total inscriptions :</strong> {{ totalInscriptions }}</p>
            <p><strong>Total reçus :</strong> {{ totalRecus }}</p>
          </div>
        </div>

        <!-- Paramètres -->
        <div v-else-if="activeTab === 'settings'" class="content-card">
          <h2 class="section-title">
            <i class="bi bi-gear-fill"></i>
            Paramètres
          </h2>

          <div class="settings-box">
            <div class="setting-row">
              <div>
                <h3>Notifications par email</h3>
                <p>Recevoir des notifications sur votre progression</p>
              </div>
              <input type="checkbox" checked />
            </div>

            <div class="setting-row">
              <div>
                <h3>Mode sombre</h3>
                <p>Activer le thème sombre</p>
              </div>
              <input type="checkbox" />
            </div>

            <div class="setting-row">
              <div>
                <h3>Langue</h3>
                <p>Choisir la langue de l'interface</p>
              </div>
              <select>
                <option>Français</option>
              </select>
            </div>
          </div>
        </div>
      </main>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import {
  getMesInscriptions,
  getMesRecus,
  getMesStats,
  telechargerRecuApprenant
} from '../../api/apprenantEspace'

console.log('✅ MON ESPACE APPRENANT CHARGÉ')



const activeTab = ref('overview')
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const apiError = ref('')

const inscriptions = ref([])
const recus = ref([])

const stats = ref({
  totalInscriptions: 0,
  formationsEnCours: 0,
  formationsTerminees: 0,
  certificats: 0,
  tempsFormation: '0h',
  totalPaye: 0
})

const getUtilisateurConnecte = () => {
  if (authStore.user) return authStore.user

  const auth = localStorage.getItem('auth')
  const utilisateur = localStorage.getItem('utilisateur')

  if (auth) {
    try {
      const parsed = JSON.parse(auth)
      if (parsed?.user) return parsed.user
    } catch (error) {
      console.error('Erreur lecture auth', error)
    }
  }

  if (utilisateur) {
    try {
      return JSON.parse(utilisateur)
    } catch (error) {
      console.error('Erreur lecture utilisateur', error)
    }
  }

  return null
}

const getIdUser = () => {
  const utilisateur = getUtilisateurConnecte()

  if (utilisateur?.idUser) return Number(utilisateur.idUser)
  if (utilisateur?.id) return Number(utilisateur.id)

  const token = localStorage.getItem('token') || localStorage.getItem('authToken')

  if (token && token.startsWith('TOKEN-DEMO-')) {
    const id = Number(token.replace('TOKEN-DEMO-', ''))
    if (!Number.isNaN(id)) return id
  }

  return 1
}

const initials = computed(() => {
  const utilisateur = getUtilisateurConnecte()

  const prenom = utilisateur?.prenom || ''
  const nom = utilisateur?.nom || ''

  if (prenom && nom) return `${prenom[0]}${nom[0]}`.toUpperCase()
  if (utilisateur?.email) return utilisateur.email[0].toUpperCase()

  return 'U'
})

const displayName = computed(() => {
  const utilisateur = getUtilisateurConnecte()

  const prenom = utilisateur?.prenom || ''
  const nom = utilisateur?.nom || ''

  if (prenom && nom) return `${prenom} ${nom}`

  return utilisateur?.email?.split('@')[0] || 'Utilisateur'
})

const memberSince = computed(() => {
  const utilisateur = getUtilisateurConnecte()

  if (utilisateur?.createdAt) {
    return formatDate(utilisateur.createdAt)
  }

  return '2026'
})

const enCours = computed(() => {
  return inscriptions.value.filter((item) => item.statut !== 'termine' && item.statut !== 'annule')
})

const termine = computed(() => {
  return inscriptions.value.filter((item) => item.statut === 'termine')
})

const totalInscriptions = computed(() => inscriptions.value.length)
const totalRecus = computed(() => recus.value.length)

const normaliserInscription = (item) => {
  const statutOriginal = String(item.statut || item.statutOriginal || '').toLowerCase()

  let statut = 'inscrit'

  if (statutOriginal.includes('reserve')) {
    statut = 'reserve'
  } else if (statutOriginal.includes('termine')) {
    statut = 'termine'
  } else if (statutOriginal.includes('annule')) {
    statut = 'annule'
  } else if (statutOriginal.includes('attente')) {
    statut = 'enAttente'
  }

  return {
    id: item.idInscription || item.id,
    idInscription: item.idInscription || item.id,
    idFormation: item.idFormation,

    formation: item.formation || item.titre || 'Formation',
    titre: item.formation || item.titre || 'Formation',

    ecole: item.ecole || item.centre || 'Centre',
    centre: item.centre || item.ecole || 'Centre',

    categorie: item.categorie || 'Formation',
    description: item.description || '',
    lieu: item.lieu || item.ville || '',
    ville: item.ville || item.lieu || '',

    date: item.date || item.createdAt || item.dateDebut,
    dateDebut: item.dateDebut,
    dateFin: item.dateFin,

    statut,
    statutOriginal: item.statutOriginal || item.statut,

    paiement_confirme: statut !== 'enAttente',

    progression: Number(item.progression || 0),

    prix: Number(item.montantPaye || item.montant || item.prix || 0),
    montant: Number(item.montantPaye || item.montant || item.prix || 0),
    montantPaye: Number(item.montantPaye || item.montant || 0),

    duree: item.duree || '',

    numeroRecu: item.numeroRecu,
    operateur: item.operateur,
    transactionId: item.transactionId
  }
}

const normaliserRecu = (item) => {
  const statutOriginal = String(item.statut || item.statutOriginal || '').toLowerCase()

  let statut = 'enAttente'

  if (
    statutOriginal.includes('paye') ||
    statutOriginal.includes('payé') ||
    statutOriginal.includes('confirme')
  ) {
    statut = 'paye'
  }

  return {
    id: item.idPaiement || item.id || item.idInscription,
    idPaiement: item.idPaiement,
    idInscription: item.idInscription,

    formation: item.formation || 'Formation',
    centre: item.centre || 'Centre',

    date: item.date || item.datePaiement || item.dateInscription,

    montant: Number(item.montant || 0),

    reference: item.reference || item.numeroRecu || `RECU-${item.idInscription}`,
    numeroRecu: item.numeroRecu,
    numeroTransaction: item.numeroTransaction,

    methode: item.methode || 'Mobile Money',
    operateur: item.operateur || '',

    statut
  }
}

function formatPrice(value) {
  return new Intl.NumberFormat('fr-FR').format(Number(value || 0))
}

function formatDate(date) {
  if (!date) return '-'

  const d = new Date(date)

  if (Number.isNaN(d.getTime())) return date

  return d.toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

function getStatusLabel(statut) {
  switch (statut) {
    case 'inscrit':
      return 'Inscrit'
    case 'reserve':
      return 'Réservé'
    case 'termine':
      return 'Terminé'
    case 'enAttente':
      return 'En attente'
    case 'annule':
      return 'Annulé'
    case 'paye':
      return 'Payé'
    default:
      return statut || 'En attente'
  }
}

function getStatusClass(statut) {
  switch (statut) {
    case 'inscrit':
      return 'bg-primary'
    case 'reserve':
      return 'bg-warning'
    case 'termine':
      return 'bg-success'
    case 'enAttente':
      return 'bg-warning'
    case 'annule':
      return 'bg-danger'
    case 'paye':
      return 'bg-success'
    default:
      return 'bg-secondary'
  }
}

function viewDetails(inscription) {
  const idFormation = inscription.idFormation || inscription.id

  if (idFormation) {
    router.push(`/formations/${idFormation}`)
  }
}

async function downloadRecu(item) {
  try {
    const idInscription = item.idInscription || item.id

    if (!idInscription) {
      alert('Aucune inscription trouvée pour ce reçu.')
      return
    }

    await telechargerRecuApprenant(idInscription)
  } catch (error) {
    console.error('Erreur téléchargement reçu :', error)
    alert('Erreur lors du téléchargement du reçu PDF.')
  }
}

async function loadInscriptions() {
  const idUser = getIdUser()
  const data = await getMesInscriptions(idUser)

  console.log('✅ Inscriptions apprenant API =', data)

  inscriptions.value = data.map(normaliserInscription)
}

async function loadRecus() {
  const idUser = getIdUser()
  const data = await getMesRecus(idUser)

  console.log('✅ Reçus apprenant API =', data)

  recus.value = data.map(normaliserRecu)
}

async function loadStats() {
  const idUser = getIdUser()
  const data = await getMesStats(idUser)

  console.log('✅ Stats apprenant API =', data)

  stats.value = {
    totalInscriptions: Number(data.totalInscriptions || 0),
    formationsEnCours: Number(data.formationsEnCours || 0),
    formationsTerminees: Number(data.formationsTerminees || 0),
    certificats: Number(data.certificats || 0),
    tempsFormation: data.tempsFormation || '0h',
    totalPaye: Number(data.totalPaye || 0)
  }
}

async function loadData() {
  loading.value = true
  apiError.value = ''

  try {
    await Promise.all([
      loadInscriptions(),
      loadRecus(),
      loadStats()
    ])
  } catch (error) {
    console.error('Erreur chargement espace apprenant :', error)
    apiError.value = 'Erreur lors du chargement de votre espace.'
  } finally {
    loading.value = false
  }
}

function goAccueil() {
  router.push('/accueil')
}

function goRecherche() {
  router.push('/recherche')
}

function logout() {
  localStorage.removeItem('auth')
  localStorage.removeItem('utilisateur')
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  localStorage.removeItem('authToken')
  localStorage.removeItem('formateurToken')
  localStorage.removeItem('authFormateur')
  localStorage.removeItem('adminToken')

  sessionStorage.clear()

  try {
    if (authStore.user) {
      authStore.user = null
    }

    if (authStore.token) {
      authStore.token = null
    }

    if (authStore.$reset) {
      authStore.$reset()
    }
  } catch (error) {
    console.log('Store déjà vidé')
  }

  window.location.href = '/connexion'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.apprenant-space {
  min-height: 100vh;
  background: #f4f4f4;
}

.hero {
  background: linear-gradient(135deg, #1f1f1f, #2b2b2b);
  color: #ffffff;
  padding: 64px 8%;
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  border: 4px solid #777;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  background: #444;
  font-size: 22px;
}

.hero h1 {
  font-size: 30px;
  margin: 0 0 8px 0;
}

.hero p {
  margin: 0 0 16px 0;
}

.hero-actions {
  display: flex;
  gap: 8px;
}

.hero-actions button {
  border: 1px solid #ffffff;
  border-radius: 20px;
  background: transparent;
  color: #ffffff;
  padding: 8px 16px;
  cursor: pointer;
}

.space-body {
  max-width: 1120px;
  margin: 48px auto;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
  padding: 0 16px;
}

.sidebar {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
  height: fit-content;
}

.menu-item {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 16px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 8px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.menu-item.active {
  background: #161616;
  color: #ffffff;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  border: 1px solid #ff3333;
  color: #ff3333;
  background: #ffffff;
  border-radius: 8px;
  cursor: pointer;
}

.content-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 26px;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: #f3f3f3;
  border-radius: 14px;
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.stat-card h3 {
  font-size: 26px;
  margin: 0;
}

.stat-card p {
  margin: 4px 0 0 0;
}

.stat-icon {
  width: 58px;
  height: 58px;
  border-radius: 12px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.stat-icon.black {
  background: #111111;
}

.stat-icon.green {
  background: #23833a;
}

.stat-icon.gold {
  background: #c99d4f;
}

.stat-icon.gray {
  background: #777777;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  background: #f4f4f4;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.activity-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: #111111;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-content {
  flex: 1;
}

.activity-content h4 {
  margin: 0 0 6px 0;
}

.activity-content p {
  margin: 0;
  color: #666;
}

.activity-progress {
  min-width: 130px;
  text-align: right;
}

.progress {
  height: 6px;
  background: #e5e5e5;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 6px;
}

.progress-bar {
  height: 100%;
  background: #111111;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f1f1f1;
  padding: 14px;
  text-align: left;
}

.data-table td {
  border-bottom: 1px solid #ddd;
  padding: 14px;
}

.badge {
  display: inline-block;
  border-radius: 8px;
  color: #ffffff;
  padding: 5px 9px;
  font-size: 12px;
  font-weight: 700;
}

.bg-primary {
  background: #111111;
}

.bg-warning {
  background: #f59e0b;
}

.bg-success {
  background: #208638;
}

.bg-danger {
  background: #e3342f;
}

.bg-secondary {
  background: #777777;
}

.small-btn {
  border: 1px solid #111111;
  background: #ffffff;
  padding: 8px 14px;
  margin-right: 6px;
  cursor: pointer;
}

.small-btn.success,
.download-btn {
  border: none;
  background: #2e7d32;
  color: #ffffff;
  padding: 10px 14px;
  border-radius: 6px;
  cursor: pointer;
}

.reference {
  color: #e91e63;
  font-weight: 700;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.course-card {
  border: 1px solid #ddd;
  border-radius: 14px;
  overflow: hidden;
}

.course-image {
  height: 150px;
  background: linear-gradient(135deg, #1f1f1f, #3b3b3b);
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-image span {
  background: #ffffff;
  border-radius: 20px;
  padding: 10px 24px;
  font-weight: 800;
}

.course-content {
  padding: 24px;
}

.course-content h3 {
  margin-top: 0;
}

.course-meta {
  display: flex;
  gap: 18px;
  color: #666;
  margin: 16px 0;
}

.continue-btn {
  width: 100%;
  background: #111111;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 48px;
  background: #f5f5f5;
  border-radius: 14px;
}

.profile-box,
.settings-box {
  background: #f5f5f5;
  border-radius: 14px;
  padding: 24px;
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ddd;
  padding: 20px 0;
}

.error-box {
  color: #b00020;
}

@media (max-width: 900px) {
  .space-body {
    grid-template-columns: 1fr;
  }

  .stats-grid,
  .courses-grid {
    grid-template-columns: 1fr;
  }
}
</style>