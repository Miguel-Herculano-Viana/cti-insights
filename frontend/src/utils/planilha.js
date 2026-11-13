// Leitura e validação da planilha NO NAVEGADOR (antes de enviar).
// Regras iguais às do Java e do Python (docs/CONTRATO.md).
import readExcelFile from 'read-excel-file/browser'

export const ABA = 'upload_clientes'

export const COLUNAS = [
  'codigo_cliente', 'nome_cliente', 'consultor', 'segmento', 'nivel_cliente',
  'faturamento_anual', 'servicos_contratados', 'data_contratacao', 'cidade', 'uf',
]
const OBRIGATORIAS = COLUNAS.slice(0, 8)

const SEGMENTOS = {
  industria: 'Indústria', ind: 'Indústria',
  comercio: 'Comércio', com: 'Comércio',
  servicos: 'Serviços', servico: 'Serviços', serv: 'Serviços',
  saude: 'Saúde',
  educacao: 'Educação', edu: 'Educação',
  tecnologia: 'Tecnologia', tec: 'Tecnologia', ti: 'Tecnologia',
}

const semAcento = (t) => t.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()

function texto(valor) {
  if (valor === null || valor === undefined) return ''
  return String(valor).replace(/\s+/g, ' ').trim()
}

export function segmentoPadrao(valor) {
  const chave = semAcento(texto(valor)).replace(/\.$/, '').trim()
  return SEGMENTOS[chave] || null
}

/** 1850000 | '1.850.000,50' | 'R$ 1.850.000,50' -> '1850000.50' (ou null) */
export function faturamentoPadrao(valor) {
  if (typeof valor === 'number') return valor >= 0 ? valor.toFixed(2) : null
  let t = texto(valor).replace('R$', '').replace(/\s/g, '')
  if (!t) return null
  if (t.includes(',')) t = t.replace(/\./g, '').replace(',', '.')
  const n = Number(t)
  return Number.isFinite(n) && n >= 0 ? n.toFixed(2) : null
}

/** Date | 'dd/mm/aaaa' | 'aaaa-mm-dd' -> 'aaaa-mm-dd' (ou null). A partir de 1990. */
export function dataPadrao(valor) {
  let iso = null
  if (valor instanceof Date && !isNaN(valor)) {
    iso = valor.toISOString().slice(0, 10) // a lib entrega datas em UTC
  } else {
    const t = texto(valor)
    let m
    if ((m = t.match(/^(\d{2})\/(\d{2})\/(\d{4})$/))) iso = `${m[3]}-${m[2]}-${m[1]}`
    else if ((m = t.match(/^(\d{4})-(\d{2})-(\d{2})/))) iso = `${m[1]}-${m[2]}-${m[3]}`
  }
  if (!iso) return null
  const d = new Date(iso + 'T00:00:00Z')
  // confere se a data existe (ex.: 31/02 vira março e é rejeitada)
  if (isNaN(d) || d.toISOString().slice(0, 10) !== iso || d.getUTCFullYear() < 1990) return null
  return iso
}

/** Lê o arquivo e devolve as linhas como objetos { coluna: valor, numeroLinha }. */
export async function lerPlanilha(arquivo) {
  if (!/\.xlsx$/i.test(arquivo.name)) {
    throw new Error('Envie um arquivo .xlsx')
  }
  let abas
try {
  abas = await readExcelFile(arquivo)
} catch (e) {
  if (e.code === 'XLS_FILE_NOT_SUPPORTED') throw new Error('Arquivo .xls antigo: abra no Excel e salve como .xlsx')
  throw new Error('O arquivo não é um .xlsx válido (pode estar corrompido ou ser outro formato renomeado)')
}
  const aba = abas.find((a) => a.sheet === ABA) || abas[0]
  const [cabecalho = [], ...linhas] = aba.data

  const nomes = cabecalho.map((c) => semAcento(texto(c)))
  const faltando = COLUNAS.filter((c) => !nomes.includes(c))
  if (faltando.length) {
    throw new Error(`Colunas faltando na aba "${aba.sheet}": ${faltando.join(', ')}`)
  }

  return linhas
    .map((valores, i) => {
      const obj = { numeroLinha: i + 2 } // +2: linha 1 é o cabeçalho
      COLUNAS.forEach((col) => (obj[col] = valores[nomes.indexOf(col)]))
      return obj
    })
    .filter((l) => COLUNAS.some((c) => texto(l[c]) !== '')) // ignora linhas totalmente vazias
}

/** Separa válidas e inválidas. Válidas já saem no formato do backend (camelCase). */
export function validar(linhas) {
  const validas = []
  const invalidas = []
  const codigos = new Set()

  for (const l of linhas) {
    const motivos = []
    for (const col of OBRIGATORIAS) {
      if (texto(l[col]) === '') motivos.push(`${col} vazio`)
    }

    const codigo = texto(l.codigo_cliente).toUpperCase()
    if (codigo && !/^[A-Z0-9_-]+$/.test(codigo)) motivos.push('código com caracteres inválidos')
    else if (codigo && codigos.has(codigo)) motivos.push('código repetido')

    const segmento = segmentoPadrao(l.segmento)
    if (texto(l.segmento) && !segmento) motivos.push(`segmento desconhecido "${texto(l.segmento)}"`)

    const nivel = texto(l.nivel_cliente).toUpperCase()
    if (nivel && !['A', 'B', 'C'].includes(nivel)) motivos.push(`nível "${nivel}" (use A, B ou C)`)

    const faturamento = faturamentoPadrao(l.faturamento_anual)
    if (texto(l.faturamento_anual) && faturamento === null) motivos.push('faturamento inválido')

    const data = dataPadrao(l.data_contratacao)
    if (texto(l.data_contratacao) && !data) motivos.push('data inválida')

    const uf = texto(l.uf).toUpperCase()
    if (uf && !/^[A-Z]{2}$/.test(uf)) motivos.push('UF inválida')

    if (motivos.length) {
      invalidas.push({ numeroLinha: l.numeroLinha, codigo: texto(l.codigo_cliente), nome: texto(l.nome_cliente), motivos })
      continue
    }

    codigos.add(codigo)
    validas.push({
      numeroLinha: l.numeroLinha,
      codigoCliente: codigo,
      nomeCliente: texto(l.nome_cliente),
      consultor: texto(l.consultor),
      segmento,
      nivelCliente: nivel,
      faturamentoAnual: faturamento,
      servicosContratados: texto(l.servicos_contratados),
      dataContratacao: data,
      cidade: texto(l.cidade) || null,
      uf: uf || null,
    })
  }
  return { validas, invalidas }
}
