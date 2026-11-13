"""Contas do dashboard. Entrada: DataFrame limpo (saída de limpeza.limpar)."""
import math

import pandas as pd


def numero(valor, casas: int = 2):
    """Converte para float arredondado; NaN/infinito viram None (JSON não aceita NaN)."""
    if valor is None:
        return None
    try:
        v = float(valor)
    except (TypeError, ValueError):
        return None
    if math.isnan(v) or math.isinf(v):
        return None
    return round(v, casas)


def moeda(valor: float) -> str:
    """1850000.5 -> 'R$ 1.850.000,50' (para as frases de insight)."""
    texto = f"{valor:,.2f}".replace(",", "X").replace(".", ",").replace("X", ".")
    return f"R$ {texto}"


def calcular(df: pd.DataFrame, recebidas: int, invalidas: int) -> dict:
    total = len(df)
    qualidade = {"linhasRecebidas": recebidas, "linhasValidas": total, "linhasInvalidas": invalidas}

    if total == 0:
        return {"resumo": {"totalClientes": 0}, "porSegmento": [], "porNivel": [],
                "porConsultor": [], "porServico": [], "porMes": [],
                "correlacao": {"pearson": None, "interpretacao": "sem dados"},
                "topClientes": [], "qualidade": qualidade,
                "insights": ["Nenhuma linha válida neste envio."]}

    fat = df["faturamento"]

    resumo = {
        "totalClientes": total,
        "faturamentoTotal": numero(fat.sum()),
        "media": numero(fat.mean()),
        "mediana": numero(fat.median()),
        "desvioPadrao": numero(fat.std()),  # amostral (n-1), padrão do Pandas
        "minimo": numero(fat.min()),
        "maximo": numero(fat.max()),
        "q1": numero(fat.quantile(0.25)),
        "q3": numero(fat.quantile(0.75)),
    }

    seg = (df.groupby("segmento")["faturamento"]
             .agg(clientes="count", media="mean", mediana="median")
             .sort_values("clientes", ascending=False))
    por_segmento = [
        {"segmento": nome, "clientes": int(l.clientes),
         "percentual": numero(l.clientes / total * 100, 1),
         "media": numero(l.media), "mediana": numero(l.mediana)}
        for nome, l in seg.iterrows()
    ]

    niveis = df["nivel"].value_counts()
    por_nivel = [{"nivel": n, "clientes": int(niveis.get(n, 0))} for n in ("A", "B", "C")]

    cons = (df.groupby("consultor")["faturamento"]
              .agg(clientes="count", faturamentoTotal="sum")
              .sort_values("faturamentoTotal", ascending=False))
    por_consultor = [
        {"consultor": nome, "clientes": int(l.clientes), "faturamentoTotal": numero(l.faturamentoTotal)}
        for nome, l in cons.iterrows()
    ]

    # explode: uma linha por serviço contratado
    servicos = df.explode("servicos")["servicos"].value_counts()
    total_contratos = int(servicos.sum())
    por_servico = [
        {"servico": nome, "contratos": int(qtd), "percentual": numero(qtd / total_contratos * 100, 1)}
        for nome, qtd in servicos.items()
    ]

    meses = pd.to_datetime(df["data_contratacao"]).dt.to_period("M").value_counts().sort_index()
    # meses sem contratação entram com 0 (senão o gráfico de linha "pula" o mês)
    meses = meses.reindex(pd.period_range(meses.index.min(), meses.index.max(), freq="M"), fill_value=0)
    por_mes = [{"mes": str(m), "clientes": int(q)} for m, q in meses.items()]

    qtd_servicos = df["servicos"].apply(len)
    pearson = numero(fat.corr(qtd_servicos), 3) if total > 2 else None
    correlacao = {"pearson": pearson, "interpretacao": interpretar(pearson)}

    top = df.nlargest(5, "faturamento")
    top_clientes = [{"codigo": l.codigo, "nome": l.nome, "faturamento": numero(l.faturamento)}
                    for l in top.itertuples()]

    resultado = {
        "resumo": resumo,
        "porSegmento": por_segmento,
        "porNivel": por_nivel,
        "porConsultor": por_consultor,
        "porServico": por_servico,
        "porMes": por_mes,
        "correlacao": correlacao,
        "topClientes": top_clientes,
        "qualidade": qualidade,
    }
    resultado["insights"] = gerar_insights(resultado, total_contratos)
    return resultado


def interpretar(r) -> str:
    if r is None:
        return "sem dados suficientes"
    forca = abs(r)
    if forca < 0.3:
        intensidade = "fraca"
    elif forca < 0.7:
        intensidade = "moderada"
    else:
        intensidade = "forte"
    sentido = "positiva" if r >= 0 else "negativa"
    return f"correlação {intensidade} {sentido} entre faturamento e quantidade de serviços"


def gerar_insights(r: dict, total_contratos: int) -> list[str]:
    frases = []
    seg = r["porSegmento"][0]
    frases.append(f"{seg['segmento']} é o segmento com mais clientes: {seg['clientes']} ({seg['percentual']}%).")

    cons = r["porConsultor"][0]
    frases.append(f"{cons['consultor']} lidera a carteira em faturamento, com {moeda(cons['faturamentoTotal'])}.")

    serv = r["porServico"][0]
    frases.append(f"{serv['servico']} é o serviço mais contratado ({serv['contratos']} de {total_contratos} contratos).")

    res = r["resumo"]
    if res["media"] and res["mediana"] and res["media"] > res["mediana"] * 1.15:
        frases.append(f"A média ({moeda(res['media'])}) está bem acima da mediana ({moeda(res['mediana'])}): "
                      f"poucos clientes grandes puxam o faturamento para cima.")

    nivel_a = next(n for n in r["porNivel"] if n["nivel"] == "A")
    frases.append(f"{nivel_a['clientes']} de {res['totalClientes']} clientes são nível A.")

    if r["correlacao"]["pearson"] is not None:
        frases.append(f"Pearson = {r['correlacao']['pearson']}: {r['correlacao']['interpretacao']}.")

    q = r["qualidade"]
    if q["linhasInvalidas"]:
        frases.append(f"{q['linhasInvalidas']} de {q['linhasRecebidas']} linhas da planilha tinham erro e ficaram de fora.")
    return frases
