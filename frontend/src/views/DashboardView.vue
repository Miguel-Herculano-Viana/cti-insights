<script setup>
    import { computed, ref, watch } from 'vue'
    import { useRoute, useRouter } from 'vue-router'
    import { api, mensagemDeErro } from '@/services/api'
    import BaseChart from '@/components/BaseChart.vue'
    import { moeda, compacto, dataHora, mesCurto } from '@/utils/formato'

    const route = useRoute()
    const router = useRouter()

    const dados = ref(null)
    const lotes = ref([])
    const erro = ref('')
    const carregando = ref(true)
    const verTabela = ref(false)

    const est = computed(() => dados.value?.estatisticas)
    const loteAtual = computed(() => dados.value?.lote.id)

    async function carregar() {
        carregando.value = true
        erro.value = ''
        try {
            const params = route.query.lote ? { lote: route.query.lote } : {}
            const [d, l] = await Promise.all([api.get('/dashboard', { params }), api.get('/dashboard/lotes')])
            dados.value = d.data
            lotes.value = l.data
        } catch (e) {
            dados.value = null
            erro.value = mensagemDeErro(e)
        } finally {
            carregando.value = false
        }
    }
    watch(() => route.query.lote, carregar, { immediate: true })

    function trocarLote(evento) {
        router.push({ query: { lote: evento.target.value } })
    }

    const inteiro = (v) => Math.round(v).toString()
</script>
<template>
    <section class="flex flex-col gap-6 max-w-7xl mx-auto">
        <header class="flex flex-wrap gap-4 items-end justify-between">
            <div>
                <h1 class="font-bold text-3xl">Dashboard</h1>
                <p v-if="dados" class="text-gray-300 text-sm">
                    {{ dados.lote.arquivoNome }} · gerado em {{ dataHora(dados.geradoEm) }}
                </p>
            </div>
            <label v-if="lotes.length > 1" class="flex items-center gap-2 text-sm">
                Envio:
                <select :value="loteAtual" @change="trocarLote" class="bg-slate-800 border border-white/20 rounded-lg px-2 py-1">
                    <option v-for="l in lotes" :key="l.id" :value="l.id">#{{ l.id }} — {{ l.arquivoNome }} ({{ dataHora(l.criadoEm) }})</option>
                </select>
            </label>
        </header>

        <p v-if="carregando" class="text-gray-300">Carregando...</p>

        <div v-else-if="erro" class="bg-white/10 border border-white/20 rounded-xl p-8 text-center flex flex-col gap-3 items-center">
            <p>{{ erro }}</p>
            <RouterLink to="/upload" class="bg-sky-700 rounded-xl px-4 py-2 hover:bg-white hover:text-sky-700 transition duration-300">Enviar uma planilha</RouterLink>
        </div>

        <template v-else-if="est">
            <!-- Indicadores -->
            <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <div class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <p class="text-gray-300 text-sm">Clientes</p>
                    <p class="font-bold text-3xl">{{ est.resumo.totalClientes }}</p>
                </div>
                <div class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <p class="text-gray-300 text-sm">Faturamento total</p>
                    <p class="font-bold text-3xl">{{ compacto(est.resumo.faturamentoTotal) }}</p>
                </div>
                <div class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <p class="text-gray-300 text-sm">Média por cliente</p>
                    <p class="font-bold text-3xl">{{ compacto(est.resumo.media) }}</p>
                    <p class="text-gray-400 text-xs">mediana {{ compacto(est.resumo.mediana) }}</p>
                </div>
                <div class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <p class="text-gray-300 text-sm">Linhas aproveitadas</p>
                    <p class="font-bold text-3xl">{{ est.qualidade.linhasValidas }}<span class="text-lg text-gray-400">/{{ est.qualidade.linhasRecebidas }}</span></p>
                    <p class="text-gray-400 text-xs">{{ est.qualidade.linhasInvalidas }} com erro</p>
                </div>
            </div>

            <!-- Gráficos -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <article class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <h2 class="font-bold mb-3">Clientes por segmento</h2>
                    <BaseChart nome-serie="Clientes" :formatar="inteiro" inteiro
                        :rotulos="est.porSegmento.map(s => s.segmento)" :valores="est.porSegmento.map(s => s.clientes)" />
                </article>
                <article class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <h2 class="font-bold mb-3">Faturamento por consultor</h2>
                    <BaseChart nome-serie="Faturamento" :formatar="compacto" horizontal
                        :rotulos="est.porConsultor.map(c => c.consultor)" :valores="est.porConsultor.map(c => c.faturamentoTotal)" />
                </article>
                <article class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <h2 class="font-bold mb-3">Serviços mais contratados</h2>
                    <BaseChart nome-serie="Contratos" :formatar="inteiro" inteiro horizontal
                        :rotulos="est.porServico.map(s => s.servico)" :valores="est.porServico.map(s => s.contratos)" />
                </article>
                <article class="bg-white/10 border border-white/20 rounded-xl p-5">
                    <h2 class="font-bold mb-3">Novos clientes por mês</h2>
                    <BaseChart tipo="line" nome-serie="Clientes" :formatar="inteiro" inteiro
                        :rotulos="est.porMes.map(m => mesCurto(m.mes))" :valores="est.porMes.map(m => m.clientes)" />
                </article>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <!-- Insights -->
                <article class="bg-cyan-400/10 border border-cyan-300/30 rounded-xl p-5">
                    <h2 class="font-bold text-cyan-200 mb-3">Insights</h2>
                    <ul class="list-disc pl-5 flex flex-col gap-2 text-gray-200">
                        <li v-for="(frase, i) in est.insights" :key="i">{{ frase }}</li>
                    </ul>
                </article>
                <!-- Maiores clientes + níveis -->
                <article class="bg-white/10 border border-white/20 rounded-xl p-5 flex flex-col gap-4">
                    <div>
                        <h2 class="font-bold mb-3">Maiores clientes</h2>
                        <table class="w-full text-sm">
                            <tbody>
                                <tr v-for="c in est.topClientes" :key="c.codigo" class="border-b border-white/5">
                                    <td class="py-1 pr-3 text-gray-400">{{ c.codigo }}</td>
                                    <td class="pr-3">{{ c.nome }}</td>
                                    <td class="text-right">{{ moeda(c.faturamento) }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div>
                        <h2 class="font-bold mb-2">Clientes por nível</h2>
                        <div class="flex gap-3">
                            <div v-for="n in est.porNivel" :key="n.nivel" class="flex-1 bg-white/5 rounded-lg p-3 text-center">
                                <p class="text-gray-300 text-xs">Nível {{ n.nivel }}</p>
                                <p class="font-bold text-2xl">{{ n.clientes }}</p>
                            </div>
                        </div>
                    </div>
                </article>
            </div>

            <!-- Estatística descritiva (tabela) -->
            <article class="bg-white/10 border border-white/20 rounded-xl p-5">
                <button @click="verTabela = !verTabela" class="font-bold cursor-pointer hover:text-sky-300">
                    {{ verTabela ? '▾' : '▸' }} Estatística descritiva do faturamento
                </button>
                <table v-if="verTabela" class="mt-3 text-sm">
                    <tbody>
                        <tr><td class="pr-6 text-gray-300">Mínimo</td><td class="text-right">{{ moeda(est.resumo.minimo) }}</td></tr>
                        <tr><td class="pr-6 text-gray-300">1º quartil</td><td class="text-right">{{ moeda(est.resumo.q1) }}</td></tr>
                        <tr><td class="pr-6 text-gray-300">Mediana</td><td class="text-right">{{ moeda(est.resumo.mediana) }}</td></tr>
                        <tr><td class="pr-6 text-gray-300">Média</td><td class="text-right">{{ moeda(est.resumo.media) }}</td></tr>
                        <tr><td class="pr-6 text-gray-300">3º quartil</td><td class="text-right">{{ moeda(est.resumo.q3) }}</td></tr>
                        <tr><td class="pr-6 text-gray-300">Máximo</td><td class="text-right">{{ moeda(est.resumo.maximo) }}</td></tr>
                        <tr><td class="pr-6 text-gray-300">Desvio padrão</td><td class="text-right">{{ moeda(est.resumo.desvioPadrao) }}</td></tr>
                        <tr><td class="pr-6 text-gray-300">Correlação (Pearson)</td><td class="text-right">{{ est.correlacao.pearson ?? '—' }}</td></tr>
                    </tbody>
                </table>
            </article>
        </template>
    </section>
</template>
