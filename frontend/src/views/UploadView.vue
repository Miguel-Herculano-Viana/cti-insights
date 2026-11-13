<script setup>
    import { onMounted, ref } from 'vue'
    import { useUploadStore } from '@/stores/upload'
    import { api, mensagemDeErro } from '@/services/api'
    import { moeda, dataHora } from '@/utils/formato'

    const up = useUploadStore()
    const arrastando = ref(false)
    const lotes = ref([])
    const erroLotes = ref('')

    async function carregarLotes() {
        try {
            lotes.value = (await api.get('/dashboard/lotes')).data
        } catch (e) {
            erroLotes.value = mensagemDeErro(e)
        }
    }
    onMounted(carregarLotes)

    function escolher(evento) {
        const arquivo = evento.target.files?.[0]
        if (arquivo) up.carregar(arquivo)
        evento.target.value = '' // permite escolher o mesmo arquivo de novo
    }

    function soltar(evento) {
        arrastando.value = false
        const arquivo = evento.dataTransfer.files?.[0]
        if (arquivo) up.carregar(arquivo)
    }

    async function confirmar() {
        await up.enviar()
        if (up.etapa === 'concluido') carregarLotes()
    }
</script>
<template>
    <section class="flex flex-col gap-8 max-w-6xl mx-auto">
        <header>
            <h1 class="font-bold text-3xl">Upload de planilha</h1>
            <p class="text-gray-300">Envie o .xlsx com a aba <code class="text-sky-300">upload_clientes</code>. O sistema confere cada linha antes de enviar.</p>
        </header>

        <!-- 1. Escolher arquivo -->
        <label v-if="['vazio', 'erro', 'lendo'].includes(up.etapa)"
            @dragover.prevent="arrastando = true" @dragleave="arrastando = false" @drop.prevent="soltar"
            :class="['flex flex-col items-center justify-center gap-3 border-2 border-dashed rounded-xl p-12 cursor-pointer transition duration-300 bg-white/5',
                     arrastando ? 'border-sky-400 bg-sky-400/10' : 'border-white/30 hover:border-sky-400']">
            <span class="text-xl font-bold">{{ up.etapa === 'lendo' ? 'Lendo planilha...' : 'Arraste o arquivo aqui ou clique para escolher' }}</span>
            <span class="text-sm text-gray-400">Somente .xlsx</span>
            <input type="file" accept=".xlsx" class="hidden" @change="escolher">
        </label>
        <p v-if="up.etapa === 'erro'" class="text-red-300" role="alert">{{ up.erro }}</p>

        <!-- 2. Prévia -->
        <template v-if="up.etapa === 'previa' || up.etapa === 'enviando'">
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <p class="text-gray-300 text-sm">Arquivo</p>
                    <p class="font-bold truncate" :title="up.arquivoNome">{{ up.arquivoNome }}</p>
                    <p class="text-sm text-gray-400">{{ up.total }} linhas</p>
                </div>
                <div class="bg-cyan-400/10 border border-cyan-300/30 rounded-xl p-5">
                    <p class="text-cyan-200 text-sm">Válidas</p>
                    <p class="font-bold text-3xl">{{ up.validas.length }}</p>
                </div>
                <div class="bg-red-400/10 border border-red-300/30 rounded-xl p-5">
                    <p class="text-red-200 text-sm">Com erro (não serão enviadas)</p>
                    <p class="font-bold text-3xl">{{ up.invalidas.length }}</p>
                </div>
            </div>

            <div v-if="up.invalidas.length" class="bg-red-400/10 border border-red-300/30 rounded-xl p-5">
                <h2 class="font-bold text-xl text-red-200 mb-3">Linhas com erro</h2>
                <ul class="flex flex-col gap-1 text-sm">
                    <li v-for="l in up.invalidas" :key="l.numeroLinha">
                        <span class="font-bold">Linha {{ l.numeroLinha }}</span>
                        <span class="text-gray-300"> ({{ l.codigo || 'sem código' }} — {{ l.nome || 'sem nome' }}):</span>
                        {{ l.motivos.join(', ') }}
                    </li>
                </ul>
            </div>

            <div class="bg-white/10 border border-white/20 rounded-xl p-5 overflow-x-auto">
                <h2 class="font-bold text-xl mb-3">Prévia das linhas válidas (já padronizadas)</h2>
                <table class="w-full text-sm text-left">
                    <thead class="text-gray-300 border-b border-white/20">
                        <tr><th class="py-2 pr-3">Linha</th><th class="pr-3">Código</th><th class="pr-3">Cliente</th><th class="pr-3">Segmento</th><th class="pr-3">Nível</th><th class="pr-3 text-right">Faturamento</th><th>Contratação</th></tr>
                    </thead>
                    <tbody>
                        <tr v-for="l in up.validas.slice(0, 10)" :key="l.numeroLinha" class="border-b border-white/5">
                            <td class="py-1 pr-3">{{ l.numeroLinha }}</td>
                            <td class="pr-3">{{ l.codigoCliente }}</td>
                            <td class="pr-3">{{ l.nomeCliente }}</td>
                            <td class="pr-3">{{ l.segmento }}</td>
                            <td class="pr-3">{{ l.nivelCliente }}</td>
                            <td class="pr-3 text-right">{{ moeda(Number(l.faturamentoAnual)) }}</td>
                            <td>{{ l.dataContratacao.split('-').reverse().join('/') }}</td>
                        </tr>
                    </tbody>
                </table>
                <p v-if="up.validas.length > 10" class="text-sm text-gray-400 mt-2">... e mais {{ up.validas.length - 10 }} linhas</p>
            </div>

            <p v-if="up.erro" class="text-red-300" role="alert">{{ up.erro }}</p>
            <div class="flex flex-wrap gap-3 justify-end">
                <button @click="up.limpar()" :disabled="up.etapa === 'enviando'" class="rounded-xl px-4 py-2 border border-white/30 hover:bg-white/10 cursor-pointer transition duration-300">Cancelar</button>
                <button @click="confirmar" :disabled="!up.validas.length || up.etapa === 'enviando'"
                    class="bg-sky-700 rounded-xl px-4 py-2 hover:bg-white hover:text-sky-700 cursor-pointer transition duration-300 disabled:opacity-50 disabled:cursor-not-allowed">
                    <template v-if="up.etapa === 'enviando'">Enviando...</template>
                    <template v-else-if="up.invalidas.length">Descartar {{ up.invalidas.length }} com erro e enviar {{ up.validas.length }}</template>
                    <template v-else>Enviar {{ up.validas.length }} linhas</template>
                </button>
            </div>
        </template>

        <!-- 3. Concluído -->
        <div v-if="up.etapa === 'concluido'" class="bg-cyan-400/10 border border-cyan-300/30 rounded-xl p-8 text-center flex flex-col gap-3 items-center">
            <h2 class="font-bold text-2xl text-cyan-200">Planilha processada!</h2>
            <p>{{ up.resultado.qualidade.linhasValidas }} clientes gravados de {{ up.resultado.qualidade.linhasRecebidas }} linhas (lote #{{ up.resultado.loteId }}).</p>
            <div class="flex gap-3">
                <RouterLink :to="{ path: '/dashboard', query: { lote: up.resultado.loteId } }" class="bg-sky-700 rounded-xl px-4 py-2 hover:bg-white hover:text-sky-700 transition duration-300">Ver dashboard</RouterLink>
                <button @click="up.limpar()" class="rounded-xl px-4 py-2 border border-white/30 hover:bg-white/10 cursor-pointer">Enviar outra</button>
            </div>
        </div>

        <!-- Relatório: histórico de envios -->
        <div class="bg-white/10 border border-white/20 rounded-xl p-5 overflow-x-auto">
            <h2 class="font-bold text-xl mb-3">Relatório de envios</h2>
            <p v-if="erroLotes" class="text-red-300">{{ erroLotes }}</p>
            <p v-else-if="!lotes.length" class="text-gray-400">Nenhuma planilha enviada ainda.</p>
            <table v-else class="w-full text-sm text-left">
                <thead class="text-gray-300 border-b border-white/20">
                    <tr><th class="py-2 pr-3">Lote</th><th class="pr-3">Arquivo</th><th class="pr-3">Enviado por</th><th class="pr-3">Data</th><th class="pr-3 text-right">Válidas</th><th class="pr-3 text-right">Com erro</th><th></th></tr>
                </thead>
                <tbody>
                    <tr v-for="l in lotes" :key="l.id" class="border-b border-white/5">
                        <td class="py-1 pr-3">#{{ l.id }}</td>
                        <td class="pr-3">{{ l.arquivoNome }}</td>
                        <td class="pr-3">{{ l.enviadoPor || '—' }}</td>
                        <td class="pr-3">{{ dataHora(l.criadoEm) }}</td>
                        <td class="pr-3 text-right text-cyan-200">{{ l.linhasValidas }}</td>
                        <td class="pr-3 text-right text-red-200">{{ l.linhasInvalidas }}</td>
                        <td><RouterLink :to="{ path: '/dashboard', query: { lote: l.id } }" class="text-sky-300 hover:underline">ver</RouterLink></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>
</template>
