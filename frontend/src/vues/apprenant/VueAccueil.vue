<template>
  <div class="container py-4">
    <section class="text-center mb-5">
      <h1 class="fw-bold">Trouvez la formation qu'il vous faut</h1>

      <form class="input-group mt-3 mx-auto search-form" @submit.prevent="rechercher">
        <input
          v-model="search"
          type="text"
          class="form-control"
          placeholder="Rechercher une formation..."
          aria-label="Rechercher une formation"
        />

        <button class="btn btn-primary" type="submit">
          Rechercher
        </button>
      </form>

      <div class="d-flex flex-wrap justify-content-center gap-2 mt-4">
        <button
          v-for="categorie in publicCategories"
          :key="categorie"
          type="button"
          class="category-chip"
          @click="search = categorie"
        >
          <i class="bi bi-tag me-2"></i>
          {{ categorie }}
        </button>
      </div>
    </section>

    <section class="stats-section mb-5">
      <div class="row g-3 text-center">
        <div class="col-6 col-md-3">
          <div class="stat-card">
            <h3>{{ formatStat(publicStats.formations) }}+</h3>
            <p>Formations</p>
          </div>
        </div>

        <div class="col-6 col-md-3">
          <div class="stat-card">
            <h3>{{ formatStat(publicStats.centres) }}+</h3>
            <p>Centres partenaires</p>
          </div>
        </div>

        <div class="col-6 col-md-3">
          <div class="stat-card">
            <h3>{{ formatStat(publicStats.apprenants) }}+</h3>
            <p>Apprenants</p>
          </div>
        </div>

        <div class="col-6 col-md-3">
          <div class="stat-card">
            <h3>{{ formatStat(publicStats.regions) }}</h3>
            <p>Régions couvertes</p>
          </div>
        </div>
      </div>
    </section>

    <section>
      <div class="d-flex align-items-center justify-content-between mb-3">
        <h2 class="h3 fw-bold mb-0">Formations suggérées</h2>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
      </div>

      <div v-else-if="erreur" class="alert alert-danger" role="alert">
        {{ erreur }}
      </div>

      <div v-else class="row g-4">
        <div
          v-for="formation in formationsFiltrees"
          :key="formation.id || formation._id"
          class="col-12 col-md-6 col-lg-4"
        >
          <CarteFormation
            :formation="formation"
            @selection="voirDetail"
          />
        </div>

        <div
          v-if="formationsFiltrees.length === 0"
          class="col-12 text-center text-muted py-5"
        >
          Aucun résultat trouvé
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import CarteFormation from "../../composants/CarteFormation.vue";
import { obtenirFormations } from "../../api/formations.js";
import { getPublicStats, getPublicCategories } from "../../api/publicApi";

const router = useRouter();

const formations = ref([]);
const search = ref("");
const loading = ref(false);
const erreur = ref("");

const publicStats = ref({
  formations: 0,
  centres: 0,
  apprenants: 0,
  regions: 0
});

const publicCategories = ref([]);

function formatStat(value) {
  const number = Number(value || 0);

  if (number >= 1000) {
    return number.toLocaleString("fr-FR");
  }

  return number;
}

async function loadPublicData() {
  try {
    const stats = await getPublicStats();

    publicStats.value = {
      formations: stats.formations || 0,
      centres: stats.centres || 0,
      apprenants: stats.apprenants || 0,
      regions: stats.regions || 0
    };
  } catch (error) {
    console.error("Erreur chargement statistiques accueil", error);
  }

  try {
    const categories = await getPublicCategories();
    publicCategories.value = Array.isArray(categories) ? categories : [];
  } catch (error) {
    console.error("Erreur chargement catégories accueil", error);
  }
}

async function chargerFormations() {
  loading.value = true;
  erreur.value = "";

  try {
    formations.value = await obtenirFormations();
    console.log("Toutes les formations depuis la base :", formations.value);
  } catch (e) {
    console.error("Erreur chargement formations", e);
    erreur.value = "Impossible de charger les formations pour le moment.";
  } finally {
    loading.value = false;
  }
}

const formationsFiltrees = computed(() => {
  const terme = search.value.trim().toLowerCase();

  if (!terme) {
    return formations.value;
  }

  return formations.value.filter((formation) =>
    [
      formation.titre,
      formation.nom,
      formation.lieu,
      formation.ville,
      formation.categorie
    ].some((valeur) => String(valeur || "").toLowerCase().includes(terme))
  );
});

function rechercher() {
  search.value = search.value.trim();
}

function voirDetail(formation) {
  const id = formation.idFormation || formation.id || formation._id;

  if (!id) {
    console.error('ID formation introuvable', formation);
    return;
  }

  router.push(`/formations/${id}`);
}

onMounted(() => {
  loadPublicData();
  chargerFormations();
});
</script>

<style scoped>
.search-form {
  max-width: 760px;
}

.stats-section {
  background: #c9a15b;
  border-radius: 16px;
  padding: 24px;
}

.stat-card {
  padding: 16px;
  border-right: 1px solid rgba(0, 0, 0, 0.15);
}

.stat-card h3 {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 4px;
  color: #111111;
}

.stat-card p {
  margin: 0;
  color: #111111;
}

.category-chip {
  background: #2b2b2b;
  color: #ffffff;
  border: 1px solid #555555;
  border-radius: 999px;
  padding: 8px 20px;
  font-weight: 500;
  cursor: pointer;
}

.category-chip:hover {
  background: #c9a15b;
  color: #111111;
  border-color: #c9a15b;
}
</style>