import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'batch', name: 'Batch', component: () => import('../views/Batch.vue') },
      { path: 'batch/:batchNo', name: 'BatchDetail', component: () => import('../views/BatchDetail.vue') },
      { path: 'farmers', name: 'Farmers', component: () => import('../views/Farmers.vue') },
      { path: 'carbon', name: 'Carbon', component: () => import('../views/Carbon.vue') },
      { path: 'analysis', name: 'Analysis', component: () => import('../views/Analysis.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('trace_token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/'
  }
})

export default router
