const fmtMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 })
const fmtCompacto = new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 })

export const moeda = (v) => (v == null || isNaN(v) ? '—' : fmtMoeda.format(v))
export const compacto = (v) => (v == null || isNaN(v) ? '—' : 'R$ ' + fmtCompacto.format(v))

export function dataHora(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

/** '2025-03' -> 'mar/25' */
export function mesCurto(aaaaMm) {
  const [a, m] = aaaaMm.split('-')
  const nome = new Date(Number(a), Number(m) - 1, 1).toLocaleString('pt-BR', { month: 'short' }).replace('.', '')
  return `${nome}/${a.slice(2)}`
}
