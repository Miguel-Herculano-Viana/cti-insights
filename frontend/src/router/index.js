import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LandingView from '@/views/LandingView.vue'
import LoginView from '@/views/LoginView.vue'
import CadastroView from '@/views/CadastroView.vue'
import UserPageView from '@/views/UserPageView.vue'
import DashboardView from '@/views/DashboardView.vue'
import UploadView from '@/views/UploadView.vue'
import UsuariosView from '@/views/UsuariosView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'Landing-Page', component: LandingView },
    { path: '/login', name: 'Login', component: LoginView, meta: { soVisitante: true } },
    { path: '/cadastro', name: 'Cadastro', component: CadastroView, meta: { soVisitante: true } },
    { path: '/userPage', name: 'UserPage', component: UserPageView, meta: { requiresAuth: true } },
    { path: '/dashboard', name: 'Dashboard', component: DashboardView, meta: { requiresAuth: true } },
    { path: '/upload', name: 'Upload', component: UploadView, meta: { requiresAuth: true } },
    { path: '/usuarios', name: 'Usuarios', component: UsuariosView, meta: { requiresAuth: true, requiresAdmin: true } },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView },
  ],
})

// Guarda de rotas: roda antes de cada troca de página
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.logado) return { name: 'Login', query: { voltar: to.fullPath } }
  if (to.meta.requiresAdmin && !auth.isAdmin) return { name: 'Dashboard' }
  if (to.meta.soVisitante && auth.logado) return { name: 'Dashboard' }
})

export default router
