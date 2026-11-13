<script setup>
    import { useRouter } from 'vue-router'
    import { useAuthStore } from '@/stores/auth'

    const auth = useAuthStore()
    const router = useRouter()

    function sair() {
        auth.logout()
        router.push('/login')
    }

    const link = "text-center text-white rounded-lg px-3 py-2 hover:bg-slate-700"
    const ativo = "bg-linear-to-br from-sky-800 via-slate-800 to-blue-800 hover:bg-sky-700!"
</script>
<template>
    <aside class="flex flex-col w-60 shrink-0 min-h-screen bg-linear-to-br from-blue-800 via-slate-800 to-sky-800 p-4">
        <div>
            <img src="/cti-logo.png" alt="CTI" class="h-20 block mx-auto mb-8">
            <nav class="flex flex-col gap-2">
                <RouterLink to="/dashboard" :active-class="ativo" :class="link">Dashboard</RouterLink>
                <RouterLink to="/upload" :active-class="ativo" :class="link">Upload</RouterLink>
                <RouterLink v-if="auth.isAdmin" to="/usuarios" :active-class="ativo" :class="link">Usuários</RouterLink>
                <RouterLink to="/userPage" :active-class="ativo" :class="link">Minha conta</RouterLink>
            </nav>
        </div>
        <div class="mt-auto flex flex-col gap-3">
            <p class="text-center text-sm text-gray-300 truncate" :title="auth.usuario?.email">
                {{ auth.usuario?.nome }}
                <span v-if="auth.isAdmin" class="block text-xs text-sky-300">administrador</span>
            </p>
            <button @click="sair" class="bg-red-600 rounded-xl text-center px-3 py-1 cursor-pointer hover:bg-red-500 transition duration-300">Sair</button>
        </div>
    </aside>
</template>
