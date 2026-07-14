import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/authStore'
import { useAuthFormateurStore } from './stores/authFormateurStore'
import router from './router'
import App from './App.vue'
import OptimizedImage from './components/OptimizedImage.vue'
import './assets/scss/main.scss'
import './styles/variables.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.component('OptimizedImage', OptimizedImage)

const auth = useAuthStore()
auth.loadFromStorage()

const authFormateur = useAuthFormateurStore()

if (typeof authFormateur.loadFromStorage === "function") {
  authFormateur.loadFromStorage()
} else if (typeof authFormateur.chargerDepuisStorage === "function") {
  authFormateur.chargerDepuisStorage()
}
app.mount('#app')
