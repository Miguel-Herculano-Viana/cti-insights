<script setup>
    import { computed } from 'vue'
    import { useRoute } from 'vue-router'
    import Sidebar from './components/Sidebar.vue'

    const route = useRoute()
    const mostrarSidebar = computed(() => route.meta.requiresAuth)
</script>
<template>
  <div class="">
    <Sidebar v-if="mostrarSidebar" />
    <main class="">
      <RouterView />
    </main>
  </div>
</template>