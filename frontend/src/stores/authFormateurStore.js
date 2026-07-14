import { defineStore } from "pinia";

const API_BASE_URL = "http://localhost:8081";

export const useAuthFormateurStore = defineStore("authFormateur", {
  state: () => ({
    token: localStorage.getItem("tokenFormateur") || null,
    formateur: JSON.parse(localStorage.getItem("formateur") || "null"),
    utilisateur: JSON.parse(localStorage.getItem("formateur") || "null"),
    loading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    estConnecte: (state) => !!state.token,
    isLoggedIn: (state) => !!state.token,
  },

  actions: {
    loadFromStorage() {
      const token = localStorage.getItem("tokenFormateur");
      const formateur = localStorage.getItem("formateur");

      this.token = token || null;

      try {
        this.formateur = formateur ? JSON.parse(formateur) : null;
        this.utilisateur = this.formateur;
      } catch (error) {
        this.formateur = null;
        this.utilisateur = null;
      }

      return !!this.token;
    },

    chargerDepuisStorage() {
      return this.loadFromStorage();
    },

    async login(email, motDePasse) {
      this.loading = true;
      this.error = null;

      try {
        const response = await fetch(`${API_BASE_URL}/api/v1/auth-formateur/connexion`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: email,
            motDePasse: motDePasse,
            password: motDePasse,
          }),
        });

        const data = await response.json().catch(() => null);

        if (!response.ok) {
          throw new Error(
            data?.message ||
              data?.error ||
              "Email ou mot de passe incorrect."
          );
        }

        const token = data?.token || data?.accessToken || data?.jwt;

        if (!token) {
          throw new Error("Connexion réussie mais aucun token reçu depuis le backend.");
        }

        const formateurData = data?.utilisateur || data?.user || data?.formateur || data;

        this.token = token;
        this.formateur = formateurData;
        this.utilisateur = formateurData;

        localStorage.setItem("tokenFormateur", token);
        localStorage.setItem("formateur", JSON.stringify(formateurData));

        return data;
      } catch (error) {
        this.error = error.message || "Erreur de connexion formateur.";
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async connexion(email, motDePasse) {
      return this.login(email, motDePasse);
    },

    logout() {
      this.token = null;
      this.formateur = null;
      this.utilisateur = null;
      this.error = null;

      localStorage.removeItem("tokenFormateur");
      localStorage.removeItem("formateur");
    },

    deconnexion() {
      this.logout();
    },
  },
});