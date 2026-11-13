<script setup>
    import { ref, watch } from 'vue'
    import { api, mensagemDeErro } from '@/services/api'
    import { useAuthStore } from '@/stores/auth'
    import { dataHora } from '@/utils/formato'

    const auth = useAuthStore()
    const filtro = ref('PENDENTE') // PENDENTE | '' (todos)
    const usuarios = ref([])
    const erro = ref('')
    const carregando = ref(false)
    const salvando = ref(null) // id do usuário sendo alterado

    async function carregar() {
        carregando.value = true
        erro.value = ''
        try {
            const params = filtro.value ? { status: filtro.value } : {}
            usuarios.value = (await api.get('/admin/usuarios', { params })).data
        } catch (e) {
            erro.value = mensagemDeErro(e)
        } finally {
            carregando.value = false
        }
    }
    watch(filtro, carregar, { immediate: true })

    async function mudarStatus(u, status) {
        salvando.value = u.id
        erro.value = ''
        try {
            await api.patch(`/admin/usuarios/${u.id}/status`, { status })
            await carregar()
        } catch (e) {
            erro.value = mensagemDeErro(e)
        } finally {
            salvando.value = null
        }
    }

    const corStatus = {
        PENDENTE: 'bg-yellow-400/20 text-yellow-200',
        APROVADO: 'bg-cyan-400/20 text-cyan-200',
        BLOQUEADO: 'bg-red-400/20 text-red-200',
    }
    const aba = (ativa) => ['px-3 py-1 rounded-lg cursor-pointer transition duration-300', ativa ? 'bg-sky-700' : 'hover:bg-white/10']
</script>
<template>
    <section class="flex flex-col gap-6 max-w-5xl mx-auto">
        <header>
            <h1 class="font-bold text-3xl">Usuários</h1>
            <p class="text-gray-300">Aprove ou bloqueie quem pediu acesso.</p>
        </header>

        <div class="flex gap-2">
            <button :class="aba(filtro === 'PENDENTE')" @click="filtro = 'PENDENTE'">Pendentes</button>
            <button :class="aba(filtro === '')" @click="filtro = ''">Todos</button>
        </div>

        <p v-if="erro" class="text-red-300" role="alert">{{ erro }}</p>

        <div class="bg-white/10 border border-white/20 rounded-xl p-5 overflow-x-auto">
            <p v-if="carregando" class="text-gray-300">Carregando...</p>
            <p v-else-if="!usuarios.length" class="text-gray-400">
                {{ filtro === 'PENDENTE' ? 'Nenhum pedido pendente.' : 'Nenhum usuário.' }}
            </p>
            <table v-else class="w-full text-sm text-left">
                <thead class="text-gray-300 border-b border-white/20">
                    <tr><th class="py-2 pr-3">Nome</th><th class="pr-3">E-mail</th><th class="pr-3">Perfil</th><th class="pr-3">Status</th><th class="pr-3">Pedido em</th><th class="text-right">Ações</th></tr>
                </thead>
                <tbody>
                    <tr v-for="u in usuarios" :key="u.id" class="border-b border-white/5">
                        <td class="py-2 pr-3">{{ u.nome }}</td>
                        <td class="pr-3">{{ u.email }}</td>
                        <td class="pr-3">{{ u.perfil === 'ADMIN' ? 'Admin' : 'Usuário' }}</td>
                        <td class="pr-3"><span :class="['px-2 py-0.5 rounded-full text-xs', corStatus[u.status]]">{{ u.status.toLowerCase() }}</span></td>
                        <td class="pr-3">{{ dataHora(u.criadoEm) }}</td>
                        <td class="text-right whitespace-nowrap">
                            <template v-if="u.id !== auth.usuario?.id">
                                <button v-if="u.status !== 'APROVADO'" @click="mudarStatus(u, 'APROVADO')" :disabled="salvando === u.id"
                                    class="bg-sky-700 rounded-lg px-3 py-1 mr-2 hover:bg-sky-600 cursor-pointer disabled:opacity-50">Aprovar</button>
                                <button v-if="u.status !== 'BLOQUEADO'" @click="mudarStatus(u, 'BLOQUEADO')" :disabled="salvando === u.id"
                                    class="bg-red-600 rounded-lg px-3 py-1 hover:bg-red-500 cursor-pointer disabled:opacity-50">Bloquear</button>
                            </template>
                            <span v-else class="text-gray-400 text-xs">você</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>
</template>
