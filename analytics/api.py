"""API Python do CTI Insights. Só o backend Java chama esta API.

Rodar:  uvicorn api:app --port 8000 --reload
"""
import os

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel

load_dotenv()

import banco  # noqa: E402  (depois do load_dotenv, para ler DATABASE_URL)
import estatisticas  # noqa: E402
import limpeza  # noqa: E402

app = FastAPI(title="CTI Insights — Analytics")

# Chave simples compartilhada com o Java, para ninguém chamar esta API direto pela internet
CHAVE_INTERNA = os.environ.get("CHAVE_INTERNA", "")


class Linha(BaseModel):
    numeroLinha: int | None = None
    codigoCliente: str | None = None
    nomeCliente: str | None = None
    consultor: str | None = None
    segmento: str | None = None
    nivelCliente: str | None = None
    faturamentoAnual: str | float | None = None
    servicosContratados: str | None = None
    dataContratacao: str | None = None
    cidade: str | None = None
    uf: str | None = None


class Pedido(BaseModel):
    arquivoNome: str
    usuarioId: int | None = None
    descartadas: int = 0  # linhas com erro que o front já tirou
    linhas: list[Linha]


@app.get("/saude")
def saude():
    return {"status": "ok"}


@app.post("/processar")
def processar(pedido: Pedido, x_chave_interna: str = Header(default="")):
    if CHAVE_INTERNA and x_chave_interna != CHAVE_INTERNA:
        raise HTTPException(status_code=401, detail="chave interna inválida")
    if not pedido.linhas:
        raise HTTPException(status_code=400, detail="nenhuma linha enviada")

    df, rejeitadas = limpeza.limpar([l.model_dump() for l in pedido.linhas])
    if df.empty:
        raise HTTPException(status_code=422, detail="nenhuma linha válida após a limpeza")

    recebidas = len(pedido.linhas) + pedido.descartadas
    invalidas = pedido.descartadas + len(rejeitadas)
    resultado = estatisticas.calcular(df, recebidas, invalidas)
    lote_id = banco.gravar_lote(pedido.arquivoNome, pedido.usuarioId, df, resultado)

    return {"loteId": lote_id, "rejeitadas": rejeitadas, "resumo": resultado["resumo"],
            "qualidade": resultado["qualidade"]}
