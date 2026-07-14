<template>
  <div class="eo-form-card eo-card-wide mx-auto">

            <div class="text-center mb-4 eo-card-header">
              <div class="bg-eofana-dark d-inline-flex p-3 rounded-3 mb-3 shadow-sm eo-icon-bg">
                <svg style="color: #c69c50;" fill="currentColor" height="28" viewBox="0 0 16 16" width="28" xmlns="http://www.w3.org/2000/svg">
                  <path d="M14 1a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h12zM2 0a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2H2z"/>
                  <path d="M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4z"/>
                </svg>
              </div>
              <h1 class="h3 fw-bold text-dark mb-2 eo-card-title">
                {{ modeEdition ? 'Modifier la formation' : 'Nouvelle formation' }}
              </h1>
              <p class="text-muted small mb-0 eo-card-subtitle">
                {{ modeEdition ? 'Modifiez les informations de votre formation' : 'Créez une nouvelle formation à publier' }}
              </p>
            </div>

            <!-- Avertissement modification formation déjà approuvée -->
            <div v-if="modeEdition && formationOriginale.approuvee" class="alert alert-warning eo-alert d-flex align-items-start gap-2 mb-4" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" class="mt-0 flex-shrink-0">
                <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
              </svg>
              <div>
                <strong>Attention :</strong> Cette formation était déjà approuvée. Après modification, elle devra repasser en attente de validation par un modérateur.
              </div>
            </div>

            <!-- Erreur API -->
            <div v-if="apiError" class="alert alert-danger eo-alert d-flex align-items-center gap-2 mb-4" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16">
                <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
              </svg>
              <span>{{ apiError }}</span>
            </div>

            <!-- Succès -->
            <div v-if="successMessage" class="alert alert-success eo-alert d-flex align-items-center gap-2 mb-4" role="alert">
              <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16">
                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zm-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/>
              </svg>
              <span>{{ successMessage }}</span>
            </div>

            <form @submit.prevent="handleSubmit" novalidate>

              <!-- Image de présentation -->
              <div class="mb-4 eo-form-group">
                <label class="form-label eo-form-label">Image de présentation</label>
                <div
                  class="eo-image-upload"
                  :class="{ 'eo-image-has-preview': imagePreview || form.image }"
                  @click="triggerImageUpload"
                >
                  <input
                    ref="fileInput"
                    type="file"
                    accept="image/*"
                    class="d-none"
                    @change="onImageSelected"
                  />
                  <div v-if="imagePreview || form.image" class="eo-image-preview">
                    <img :src="imagePreview || form.image" alt="Aperçu" />
                    <button type="button" class="eo-image-remove" @click.stop="removeImage" aria-label="Supprimer l'image">
                      <svg fill="currentColor" height="16" width="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg"><path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/></svg>
                    </button>
                  </div>
                  <div v-else class="eo-image-placeholder">
                    <svg fill="none" height="32" stroke="#9ca3af" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" width="32" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
                    </svg>
                    <span class="text-muted small">Cliquez pour ajouter une image</span>
                  </div>
                </div>
                <div v-if="errors.image" class="eo-invalid-feedback d-block">{{ errors.image }}</div>
              </div>

              <div class="row g-3">
                <!-- Titre -->
                <div class="col-12">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="titre">Titre de la formation *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.titre }"
                      id="titre"
                      v-model="form.titre"
                      @blur="validateField('titre')"
                      placeholder="Ex: Vue.js 3 Masterclass"
                      type="text"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.titre">{{ errors.titre }}</div>
                  </div>
                </div>

                <!-- Description -->
                <div class="col-12">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="description">Description *</label>
                    <textarea
                      class="form-control eo-form-control eo-textarea"
                      :class="{ 'is-invalid': errors.description }"
                      id="description"
                      v-model="form.description"
                      @blur="validateField('description')"
                      placeholder="Décrivez le contenu et les objectifs de la formation..."
                      rows="5"
                      required
                    ></textarea>
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.description">{{ errors.description }}</div>
                  </div>
                </div>

                <!-- Catégorie -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="categorie">Catégorie *</label>
                    <select
                      class="form-select eo-form-control"
                      :class="{ 'is-invalid': errors.categorie }"
                      id="categorie"
                      v-model="form.categorie"
                      @blur="validateField('categorie')"
                      required
                    >
                      <option value="" disabled>Sélectionnez une catégorie</option>
                      <option value="Développement">Développement</option>
                      <option value="Design">Design</option>
                      <option value="Marketing">Marketing</option>
                      <option value="Gestion">Gestion</option>
                      <option value="Comptabilité">Comptabilité</option>
                      <option value="Agriculture">Agriculture</option>
                      <option value="Langues">Langues</option>
                      <option value="Autre">Autre</option>
                    </select>
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.categorie">{{ errors.categorie }}</div>
                  </div>
                </div>

                <!-- Durée -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="duree">Durée *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.duree }"
                      id="duree"
                      v-model="form.duree"
                      @blur="validateField('duree')"
                      placeholder="Ex: 20h"
                      type="text"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.duree">{{ errors.duree }}</div>
                  </div>
                </div>

                <!-- Lieu -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="lieu">Lieu *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.lieu }"
                      id="lieu"
                      v-model="form.lieu"
                      @blur="validateField('lieu')"
                      placeholder="Ex: Antananarivo"
                      type="text"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.lieu">{{ errors.lieu }}</div>
                  </div>
                </div>

                <!-- Nombre de places -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="nombrePlaces">Nombre de places disponibles *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.nombrePlaces }"
                      id="nombrePlaces"
                      v-model.number="form.nombrePlaces"
                      @blur="validateField('nombrePlaces')"
                      placeholder="Ex: 30"
                      type="number"
                      min="1"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.nombrePlaces">{{ errors.nombrePlaces }}</div>
                  </div>
                </div>

                <!-- Date de début -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="dateDebut">Date de début *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.dateDebut }"
                      id="dateDebut"
                      v-model="form.dateDebut"
                      @blur="validateField('dateDebut')"
                      type="date"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.dateDebut">{{ errors.dateDebut }}</div>
                  </div>
                </div>

                <!-- Date limite d'inscription -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="dateLimite">Date limite d'inscription *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.dateLimite }"
                      id="dateLimite"
                      v-model="form.dateLimite"
                      @blur="validateField('dateLimite')"
                      type="date"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.dateLimite">{{ errors.dateLimite }}</div>
                  </div>
                </div>

                <!-- Prix normal -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="prix">Prix normal (Ar) *</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.prix }"
                      id="prix"
                      v-model.number="form.prix"
                      @blur="validateField('prix')"
                      placeholder="Ex: 150000"
                      type="number"
                      min="0"
                      required
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.prix">{{ errors.prix }}</div>
                  </div>
                </div>

                <!-- Prix remisé (optionnel) -->
                <div class="col-12 col-md-6">
                  <div class="mb-4 eo-form-group">
                    <label class="form-label eo-form-label" for="prixRemise">Prix remisé (optionnel)</label>
                    <input
                      class="form-control eo-form-control"
                      :class="{ 'is-invalid': errors.prixRemise }"
                      id="prixRemise"
                      v-model.number="form.prixRemise"
                      @blur="validateField('prixRemise')"
                      placeholder="Ex: 120000"
                      type="number"
                      min="0"
                    />
                    <div class="invalid-feedback eo-invalid-feedback" v-if="errors.prixRemise">{{ errors.prixRemise }}</div>
                    <div class="form-text text-muted small">Laissez vide si aucun prix remisé.</div>
                  </div>
                </div>
              </div>

              <!-- Message d'information modération -->
              <div class="alert alert-info eo-alert d-flex align-items-start gap-2 mb-4 mt-2" role="alert">
                <svg fill="currentColor" height="18" width="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" class="mt-0 flex-shrink-0">
                  <path d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2z"/>
                </svg>
                <div>
                  <strong>Information :</strong> Votre formation sera visible après approbation du modérateur.
                </div>
              </div>

              <!-- Boutons -->
              <div class="d-flex gap-3">
                <button
                  type="button"
                  class="btn btn-outline-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                  @click="annuler"
                >
                  <span>Annuler</span>
                </button>
                <button
                  type="submit"
                  class="btn btn-eofana-dark flex-fill d-flex align-items-center justify-content-center gap-2"
                  :disabled="submitting"
                >
                  <span v-if="submitting" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  <span v-else>Soumettre pour validation</span>
                  <svg v-if="!submitting" fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"></polyline></svg>
                </button>
              </div>

            </form>
          </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const API_BASE_URL = "http://localhost:8081";

const chargement = ref(false);
const erreur = ref("");
const success = ref("");
const imagePreview = ref("");

const form = reactive({
  titre: "",
  description: "",
  image: "",
  duree: "",
  lieu: "",
  prix: 0,
  prixRemise: 0,
});

function choisirImage(event) {
  const file = event.target.files?.[0];

  if (!file) return;

  const reader = new FileReader();

  reader.onload = () => {
    imagePreview.value = reader.result;
    form.image = reader.result;
  };

  reader.readAsDataURL(file);
}

function retirerImage() {
  imagePreview.value = "";
  form.image = "";
}

async function enregistrerFormation() {
  erreur.value = "";
  success.value = "";
  chargement.value = true;

  try {
    const token =
      localStorage.getItem("tokenFormateur") ||
      localStorage.getItem("formateurToken") ||
      "";

    const response = await fetch(`${API_BASE_URL}/api/v1/formateurs/formations`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        titre: form.titre,
        description: form.description,
        image: form.image,
        duree: form.duree,
        lieu: form.lieu,
        prix: form.prix,
        prixRemise: form.prixRemise || form.prix,
      }),
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(data.message || "Impossible de créer la formation.");
    }

    success.value = data.message || "Formation créée avec succès.";

    setTimeout(() => {
      router.push("/formateur/formations");
    }, 800);
  } catch (error) {
    erreur.value = error.message || "Erreur lors de la création.";
  } finally {
    chargement.value = false;
  }
}

function retour() {
  router.push("/formateur/formations");
}
</script>


<style scoped>
.eo-form-card {
  background: #ffffff;
  border-radius: 1rem;
  border: 1px solid #e0e0e0;
  padding: 3rem;
  max-width: 512px;
  width: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.eo-card-wide {
  max-width: 720px;
}

.eo-card-title {
  color: #1a1a1a;
}

.eo-icon-bg {
  background-color: #1a1a1a !important;
}

.eo-form-group {
  margin-bottom: 0 !important;
}

.eo-form-control {
  height: 48px;
  border-radius: 8px;
  border-color: #dee2e6;
}

.eo-form-control:focus {
  border-color: #c69c50;
  box-shadow: 0 0 0 0.2rem rgba(198, 156, 80, 0.25);
}

.eo-textarea {
  height: auto;
  min-height: 120px;
  padding-top: 12px;
}

.eo-form-label {
  font-weight: 600;
  font-size: 0.875rem;
  color: #333333;
  margin-bottom: 0.5rem;
}

.btn-eofana-dark {
  background-color: #1a1a1a;
  color: #ffffff;
  font-weight: 600;
  border-radius: 8px;
  border: none;
  padding: 0.75rem 1.5rem;
  transition: background-color 0.2s;
}

.btn-eofana-dark:hover {
  background-color: #333333;
  color: #ffffff;
}

.btn-eofana-dark:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-outline-dark {
  border-radius: 8px;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-color: #dee2e6;
  color: #333333;
}

.btn-outline-dark:hover {
  background-color: #f5f5f5;
  border-color: #c69c50;
  color: #1a1a1a;
}

.eo-alert {
  border-radius: 8px;
  font-size: 0.9rem;
}

.eo-invalid-feedback {
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

/* Image upload */
.eo-image-upload {
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  position: relative;
}

.eo-image-upload:hover {
  border-color: #c69c50;
  background: rgba(198, 156, 80, 0.04);
}

.eo-image-has-preview {
  border-style: solid;
  padding: 0.5rem;
  cursor: default;
}

.eo-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.eo-image-preview {
  position: relative;
  display: inline-block;
  width: 100%;
  max-height: 250px;
  overflow: hidden;
  border-radius: 6px;
}

.eo-image-preview img {
  width: 100%;
  height: auto;
  max-height: 250px;
  object-fit: cover;
  display: block;
}

.eo-image-remove {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  cursor: pointer;
  transition: background 0.2s;
}

.eo-image-remove:hover {
  background: rgba(220, 53, 69, 0.8);
}

@media (max-width: 768px) {
  .eo-form-card {
    padding: 1.5rem;
  }
}
</style>
