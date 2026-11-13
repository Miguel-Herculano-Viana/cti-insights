import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api, mensagemDeErro } from '@/services/api'
import { lerPlanilha, validar } from '@/utils/planilha'

// etapa: 'vazio' -> 'lendo' -> 'previa' -> 'enviando' -> 'concluido'  (ou 'erro')
export const useUploadStore = defineStore('upload', () => {
  const etapa = ref('vazio')
  const arquivoNome = ref('')
  const validas = ref([])
  const invalidas = ref([])
  const resultado = ref(null)
  const erro = ref('')

  const total = computed(() => validas.value.length + invalidas.value.length)

  function limpar() {
    etapa.value = 'vazio'
    arquivoNome.value = ''
    validas.value = []
    invalidas.value = []
    resultado.value = null
    erro.value = ''
  }

  async function carregar(arquivo) {
    limpar()
    etapa.value = 'lendo'
    arquivoNome.value = arquivo.name
    try {
      const linhas = await lerPlanilha(arquivo)
      if (!linhas.length) throw new Error('A planilha não tem linhas de dados.')
      const r = validar(linhas)
      validas.value = r.validas
      invalidas.value = r.invalidas
      etapa.value = 'previa'
    } catch (e) {
      erro.value = e.message || 'Não foi possível ler o arquivo.'
      etapa.value = 'erro'
    }
  }

  /** Só é chamado depois que o usuário confirma descartar as inválidas. */
  async function enviar() {
    if (!validas.value.length) return
    etapa.value = 'enviando'
    erro.value = ''
    try {
      const { data } = await api.post('/uploads/validadas', {
        arquivoNome: arquivoNome.value,
        descartadas: invalidas.value.length,
        linhas: validas.value,
      })
      resultado.value = data
      etapa.value = 'concluido'
    } catch (e) {
      erro.value = mensagemDeErro(e)
      etapa.value = 'previa' // volta para a prévia para tentar de novo
    }
  }

  return { etapa, arquivoNome, validas, invalidas, resultado, erro, total, limpar, carregar, enviar }
})
