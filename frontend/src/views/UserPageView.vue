<script setup>
    import { onMounted } from 'vue'
    import { useAuthStore } from '@/stores/auth'
    import { dataHora } from '@/utils/formato'

    const auth = useAuthStore()
    onMounted(() => auth.recarregar().catch(() => {})) // se falhar, mostra o que já está salvo
</script>
<template>
    <section class="max-w-xl mx-auto flex flex-col gap-6">
        <h1 class="font-bold text-3xl">Minha conta</h1>
        <div v-if="auth.usuario" class="bg-white/10 border border-white/20 rounded-xl p-6">
            <dl class="grid grid-cols-[auto_1fr] gap-x-6 gap-y-3">
                <dt class="text-gray-300">Nome</dt><dd>{{ auth.usuario.nome }}</dd>
                <dt class="text-gray-300">E-mail</dt><dd>{{ auth.usuario.email }}</dd>
                <dt class="text-gray-300">Perfil</dt><dd>{{ auth.isAdmin ? 'Administrador' : 'Usuário' }}</dd>
                <dt class="text-gray-300">Conta criada em</dt><dd>{{ dataHora(auth.usuario.criadoEm) }}</dd>
            </dl>
        </div>
    </section>
</template>
