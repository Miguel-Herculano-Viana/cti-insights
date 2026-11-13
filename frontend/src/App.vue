<script setup>
    import { computed } from 'vue'
    import { useRoute } from 'vue-router'
    import Sidebar from './components/Sidebar.vue'

    const route = useRoute()
    const mostrarSidebar = computed(() => route.meta.requiresAuth)
</script>
<template>
  <div v-if="mostrarSidebar" class="flex min-h-screen bg-gray-900 text-white">
    <Sidebar />
    <main class="flex-1 min-w-0 p-6 md:p-10 bg-linear-to-br from-slate-900 via-gray-900 to-blue-950">
      <RouterView />
    </main>
  </div>
  <RouterView v-else />
</template>
