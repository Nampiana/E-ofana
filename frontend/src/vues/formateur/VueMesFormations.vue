<template>
  <div class="mes-formations-page">
    <div class="page-header">
      <h1>Mes formations</h1>

      <button class="btn-publier" @click="publierFormation">
        + Publier une formation
      </button>
    </div>

    <div class="filters">
      <button :class="{ active: filtre === 'toutes' }" @click="filtre = 'toutes'">
        Toutes
      </button>
      <button :class="{ active: filtre === 'enAttente' }" @click="filtre = 'enAttente'">
        En attente
      </button>
      <button :class="{ active: filtre === 'approuve' }" @click="filtre = 'approuve'">
        Approuvées
      </button>
      <button :class="{ active: filtre === 'rejete' }" @click="filtre = 'rejete'">
        Rejetées
      </button>
    </div>

    <div v-if="erreur" class="alert-error">
      {{ erreur }}
    </div>

    <div v-if="chargement" class="loading">
      Chargement des formations...
    </div>

    <table v-else class="formations-table">
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
        <tr v-if="formationsFiltrees.length === 0">
          <td colspan="6" class="empty">
            Aucune formation trouvée.
          </td>
        </tr>

        <tr v-for="formation in formationsFiltrees" :key="formation.idFormation">
          <td class="titre">
            {{ formation.titre }}
          </td>

          <td>
            <span class="badge-statut">
              {{ afficherStatut(formation.statut) }}
            </span>
          </td>

          <td>
            {{ formatPrix(formation.prixRemise || formation.prix) }}
          </td>

          <td>
            {{ formation.nombreInscrits || formation.inscrits || formation.nbInscrits || 0 }}
          </td>

          <td>
            {{ formation.placesRestantes || 0 }}
          </td>

          <td class="actions">
            <button class="btn-action" @click="ouvrirStats(formation)">
              Stats
            </button>

            <button class="btn-action" @click="ouvrirInscrits(formation)">
              Inscrits
            </button>

            <button class="btn-danger-outline" @click="supprimerFormation(formation)">
              Supprimer
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- MODAL STATS -->
    <div v-if="modalStatsOuverte" class="modal-backdrop">
      <div class="modal-card">
        <div class="modal-header">
          <h2>Statistiques</h2>
          <button @click="fermerModals">×</button>
        </div>

        <h3>{{ formationSelectionnee?.titre }}</h3>

        <div class="stats-grid">
          <div class="stat-card">
            <span>Inscrits</span>
            <strong>{{ stats.nombreInscrits || 0 }}</strong>
          </div>

          <div class="stat-card">
            <span>Réservations</span>
            <strong>{{ stats.nombreReservations || 0 }}</strong>
          </div>

          <div class="stat-card">
            <span>Places restantes</span>
            <strong>{{ stats.placesRestantes || 0 }}</strong>
          </div>

          <div class="stat-card">
            <span>Revenus générés</span>
            <strong>{{ formatPrix(stats.revenusGeneres || 0) }}</strong>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL INSCRITS -->
    <div v-if="modalInscritsOuverte" class="modal-backdrop">
      <div class="modal-card modal-large">
        <div class="modal-header">
          <h2>Liste des inscrits</h2>
          <button @click="fermerModals">×</button>
        </div>

        <h3>{{ formationSelectionnee?.titre }}</h3>

        <table class="formations-table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Email</th>
              <th>Type</th>
              <th>Statut</th>
              <th>Montant</th>
            </tr>
          </thead>

          <tbody>
            <tr v-if="inscrits.length === 0">
              <td colspan="5" class="empty">
                Aucun inscrit pour cette formation.
              </td>
            </tr>

            <tr v-for="inscrit in inscrits" :key="inscrit.idInscription">
              <td>{{ inscrit.nom }} {{ inscrit.prenom }}</td>
              <td>{{ inscrit.email }}</td>
              <td>{{ inscrit.typeInsc }}</td>
              <td>{{ inscrit.statut }}</td>
              <td>{{ formatPrix(inscrit.montantPaye || inscrit.montantFormateur || 0) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const API_BASE_URL = "http://localhost:8081";

const formations = ref([]);
const filtre = ref("toutes");
const chargement = ref(false);
const erreur = ref("");

const modalStatsOuverte = ref(false);
const modalInscritsOuverte = ref(false);
const formationSelectionnee = ref(null);
const stats = ref({});
const inscrits = ref([]);

const tokenFormateur =
  localStorage.getItem("tokenFormateur") ||
  localStorage.getItem("formateurToken");

const headers = {
  "Content-Type": "application/json",
  Authorization: tokenFormateur ? `Bearer ${tokenFormateur}` : "",
};

const formationsFiltrees = computed(() => {
  if (filtre.value === "toutes") {
    return formations.value;
  }

  return formations.value.filter((formation) => {
    const statut = String(formation.statut || "").toLowerCase();

    if (filtre.value === "enAttente") {
      return statut === "enattente" || statut === "en_attente";
    }

    if (filtre.value === "approuve") {
      return statut === "approuve" || statut === "approuvée" || statut === "approuvee";
    }

    if (filtre.value === "rejete") {
      return statut === "rejete" || statut === "rejetée" || statut === "rejetee";
    }

    return true;
  });
});

onMounted(() => {
  chargerFormations();
});

async function chargerFormations() {
  chargement.value = true;
  erreur.value = "";

  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/formations/mes-formations`, {
      method: "GET",
      headers,
    });

    const data = await response.json().catch(() => []);

    if (!response.ok) {
      throw new Error(data?.message || data?.error || "Impossible de charger les formations.");
    }

    formations.value = Array.isArray(data) ? data : [];
  } catch (error) {
    erreur.value = error.message || "Erreur lors du chargement des formations.";
  } finally {
    chargement.value = false;
  }
}

function publierFormation() {
  router.push("/formateur/creation");
}

async function ouvrirStats(formation) {
  formationSelectionnee.value = formation;
  stats.value = {};
  modalStatsOuverte.value = true;

  try {
    const response = await fetch(
      `${API_BASE_URL}/api/v1/formateurs/tableau-de-bord/formations/${formation.idFormation}`,
      {
        method: "GET",
        headers,
      }
    );

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(data?.message || data?.error || "Impossible de charger les statistiques.");
    }

    stats.value = data;
  } catch (error) {
    stats.value = {
      nombreInscrits: formation.nombreInscrits || 0,
      nombreReservations: formation.nombreReservations || 0,
      placesRestantes: formation.placesRestantes || 0,
      revenusGeneres: 0,
    };
  }
}

async function ouvrirInscrits(formation) {
  formationSelectionnee.value = formation;
  inscrits.value = [];
  modalInscritsOuverte.value = true;

  try {
    const response = await fetch(
      `${API_BASE_URL}/api/v1/formateurs/inscrits?formationId=${formation.idFormation}`,
      {
        method: "GET",
        headers,
      }
    );

    const data = await response.json().catch(() => []);

    if (!response.ok) {
      throw new Error(data?.message || data?.error || "Impossible de charger les inscrits.");
    }

    inscrits.value = Array.isArray(data) ? data : [];
  } catch (error) {
    inscrits.value = [];
  }
}

async function supprimerFormation(formation) {
  const confirmation = confirm(
    `Voulez-vous vraiment supprimer la formation : ${formation.titre} ?`
  );

  if (!confirmation) {
    return;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/formations/${formation.idFormation}`, {
      method: "DELETE",
      headers,
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(data?.message || data?.error || "Suppression impossible.");
    }

    formations.value = formations.value.filter(
      (item) => item.idFormation !== formation.idFormation
    );
  } catch (error) {
    alert(error.message || "Erreur lors de la suppression.");
  }
}

function fermerModals() {
  modalStatsOuverte.value = false;
  modalInscritsOuverte.value = false;
  formationSelectionnee.value = null;
  stats.value = {};
  inscrits.value = [];
}

function afficherStatut(statut) {
  if (!statut) return "inconnu";

  const value = String(statut).toLowerCase();

  if (value === "approuve" || value === "approuvee") return "approuvé";
  if (value === "enattente" || value === "en_attente") return "en attente";
  if (value === "rejete" || value === "rejetee") return "rejeté";

  return statut;
}

function formatPrix(value) {
  const montant = Number(value || 0);

  return new Intl.NumberFormat("fr-FR").format(montant) + " Ar";
}
</script>

<style scoped>
.mes-formations-page {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

h1 {
  font-size: 32px;
  font-weight: 800;
}

.btn-publier {
  background: #1f2933;
  color: white;
  border: none;
  padding: 12px 18px;
  border-radius: 6px;
  font-weight: 700;
  cursor: pointer;
}

.filters {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.filters button {
  background: white;
  border: 1px solid #222;
  padding: 7px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.filters button.active {
  background: #d4aa5f;
  color: #111;
  border-color: #d4aa5f;
}

.alert-error {
  background: #fee2e2;
  border: 1px solid #fca5a5;
  color: #991b1b;
  padding: 14px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.loading {
  padding: 24px;
}

.formations-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.formations-table th,
.formations-table td {
  border-bottom: 1px solid #ddd;
  padding: 12px;
  text-align: left;
}

.formations-table th {
  font-weight: 800;
}

.titre {
  font-weight: 800;
}

.badge-statut {
  background: #d4aa5f;
  color: white;
  border-radius: 6px;
  padding: 5px 9px;
  font-size: 13px;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-action {
  background: white;
  border: 1px solid #111;
  padding: 7px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-danger-outline {
  background: white;
  color: red;
  border: 1px solid red;
  padding: 7px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.empty {
  text-align: center;
  color: #666;
  padding: 28px;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-card {
  width: 520px;
  max-width: calc(100vw - 32px);
  background: white;
  border-radius: 10px;
  padding: 24px;
}

.modal-large {
  width: 850px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header button {
  border: none;
  background: #111;
  color: white;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 20px;
}

.stat-card {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 18px;
}

.stat-card span {
  display: block;
  color: #666;
  margin-bottom: 8px;
}

.stat-card strong {
  font-size: 24px;
}
</style>