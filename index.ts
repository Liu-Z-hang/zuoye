import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import AreaManagement from '../views/AreaManagement.vue'
import SeatManagement from '../views/SeatManagement.vue'
import OrderManagement from '../views/OrderManagement.vue'
import PricingManagement from '../views/PricingManagement.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
    },
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: Dashboard,
      meta: { requiresAuth: true },
    },
    {
      path: '/areas',
      name: 'areas',
      component: AreaManagement,
      meta: { requiresAuth: true },
    },
    {
      path: '/seats',
      name: 'seats',
      component: SeatManagement,
      meta: { requiresAuth: true },
    },
    {
      path: '/orders',
      name: 'orders',
      component: OrderManagement,
      meta: { requiresAuth: true },
    },
    {
      path: '/pricing',
      name: 'pricing',
      component: PricingManagement,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn()) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
