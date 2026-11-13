<script setup>
    // Componente genérico: recebe tipo, rótulos e valores e desenha com Chart.js.
    import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
    import Chart from 'chart.js/auto'

    const props = defineProps({
        tipo: { type: String, default: 'bar' },           // 'bar' | 'line'
        rotulos: { type: Array, required: true },
        valores: { type: Array, required: true },
        nomeSerie: { type: String, default: '' },
        horizontal: { type: Boolean, default: false },
        formatar: { type: Function, default: (v) => v },  // formata tooltip e eixo de valor
        inteiro: { type: Boolean, default: false },       // contagens: eixo só com 0, 1, 2...
    })

    // Uma cor só por gráfico (uma série). Azul do tema escuro + texto/grade discretos.
    const COR = '#3987e5'
    const TEXTO = '#c3c2b7'
    const GRADE = 'rgba(255,255,255,0.08)'

    const canvas = ref(null)
    let grafico = null

    function desenhar() {
        grafico?.destroy()
        const eixoValor = props.horizontal ? 'x' : 'y'
        const eixoRotulo = props.horizontal ? 'y' : 'x'
        grafico = new Chart(canvas.value, {
            type: props.tipo,
            data: {
                labels: props.rotulos,
                datasets: [{
                    label: props.nomeSerie,
                    data: props.valores,
                    backgroundColor: props.tipo === 'line' ? 'rgba(57,135,229,0.15)' : COR,
                    borderColor: COR,
                    borderWidth: props.tipo === 'line' ? 2 : 0,
                    borderRadius: 4,
                    borderSkipped: 'start',   // arredonda só a ponta do dado
                    maxBarThickness: 36,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    fill: props.tipo === 'line',
                    tension: 0.25,
                }],
            },
            options: {
                indexAxis: props.horizontal ? 'y' : 'x',
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false }, // tooltip ao passar perto, não só em cima
                plugins: {
                    legend: { display: false }, // uma série: o título do card já diz o que é
                    tooltip: { callbacks: { label: (c) => ` ${props.formatar(c.raw)}` } },
                },
                scales: {
                    [eixoValor]: {
                        beginAtZero: true,
                        grid: { color: GRADE },
                        border: { display: false },
                        ticks: { color: TEXTO, callback: (v) => props.formatar(v), maxTicksLimit: 6, precision: props.inteiro ? 0 : undefined },
                    },
                    [eixoRotulo]: {
                        grid: { display: false },
                        ticks: { color: TEXTO },
                    },
                },
            },
        })
    }

    onMounted(desenhar)
    watch(() => [props.rotulos, props.valores], desenhar, { deep: true })
    onBeforeUnmount(() => grafico?.destroy())
</script>
<template>
    <div class="relative h-64">
        <canvas ref="canvas" role="img" :aria-label="nomeSerie"></canvas>
    </div>
</template>
