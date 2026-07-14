import { defineStore } from "pinia"
import { connexionFormateur, deconnexionFormateur } from "../api/formateurApi"

export const useAuthFormateurStore = defineStore("authFormateur", {
  state: () => ({
    token: null,
    formateur: null,
    loading: false,
    error: ""
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    chargerDepuisStorage() {
      const raw = localStorage.getItem("authFormateur")

      if (!raw) {
        return
      }

      try {
        const data = JSON.parse(raw)
        this.token = data.token || null
        this.formateur = data.formateur || null
      } catch (error) {
        this.token = null
        this.formateur = null
        localStorage.removeItem("authFormateur")
      }
    },

    loadFromStorage() {
      this.chargerDepuisStorage()
    },

    async login(email, motDePasse) {
      this.loading = true
      this.error = ""

      try {
        const data = await connexionFormateur(email, motDePasse)

        this.token = data.token || data.jwt || data.accessToken || ""

        this.formateur = {
          idUser: data.idUser || data.id || data.userId,
          nom: data.nom || data.lastName || "",
          prenom: data.prenom || data.firstName || "",
          email: data.email || email,
          telephone: data.telephone || "",
          typeUtilisateur: data.typeUtilisateur || data.role || "FORMATEUR"
        }

        const authFormateur = {
          token: this.token,
          formateur: this.formateur
        }

        localStorage.setItem("authFormateur", JSON.stringify(authFormateur))
        localStorage.setItem("tokenFormateur", this.token)
        localStorage.setItem("formateurToken", this.token)

        return true
      } catch (error) {
        this.error = error.message || "Connexion impossible"
        return false
      } finally {
        this.loading = false
      }
    },

    async logout() {
      try {
        await deconnexionFormateur()
      } catch (error) {
        console.log("Déconnexion backend ignorée")
      }

      this.token = null
      this.formateur = null
      this.error = ""

      localStorage.removeItem("authFormateur")
      localStorage.removeItem("tokenFormateur")
      localStorage.removeItem("formateurToken")
    }
  }
})