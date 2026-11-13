"""Limpeza e padronização das linhas recebidas do Java.

O front e o Java já barram linhas inválidas, mas o Python valida de novo:
quem grava no banco é ele, então ele não confia cegamente em quem chamou.
"""
import re
import unicodedata
from datetime import date, datetime

import pandas as pd

SEGMENTOS = {
    "industria": "Indústria", "ind": "Indústria",
    "comercio": "Comércio", "com": "Comércio",
    "servicos": "Serviços", "servico": "Serviços", "serv": "Serviços",
    "saude": "Saúde",
    "educacao": "Educação", "edu": "Educação",
    "tecnologia": "Tecnologia", "tec": "Tecnologia", "ti": "Tecnologia",
}

# Palavras que ficam minúsculas no meio de nomes ("Sabor & Cia", "Ana de Souza")
MINUSCULAS = {"de", "da", "do", "das", "dos", "e"}


def sem_acento(texto: str) -> str:
    """'Indústria' -> 'industria' (para comparar sem se preocupar com acento/caixa)."""
    nfkd = unicodedata.normalize("NFKD", texto)
    return "".join(c for c in nfkd if not unicodedata.combining(c)).lower()


def texto_limpo(valor) -> str | None:
    """Tira espaços extras. Vazio vira None."""
    if valor is None or (isinstance(valor, float) and pd.isna(valor)):
        return None
    texto = re.sub(r"\s+", " ", str(valor)).strip()
    return texto or None


def capitalizar_nome(texto: str) -> str:
    """'ANA SOUZA' -> 'Ana Souza'; mantém 'de', 'da'... minúsculos no meio."""
    palavras = texto.lower().split(" ")
    saida = []
    for i, p in enumerate(palavras):
        if i > 0 and p in MINUSCULAS:
            saida.append(p)
        else:
            saida.append(p[:1].upper() + p[1:])
    return " ".join(saida)


def padronizar_segmento(valor) -> str | None:
    texto = texto_limpo(valor)
    if not texto:
        return None
    chave = sem_acento(texto).rstrip(".").strip()
    return SEGMENTOS.get(chave)


def converter_faturamento(valor) -> float | None:
    """Aceita 1850000, '1.850.000,50', 'R$ 1.850.000,50', '1850000.00'."""
    if valor is None or (isinstance(valor, float) and pd.isna(valor)):
        return None
    if isinstance(valor, (int, float)):
        return float(valor) if valor >= 0 else None
    texto = str(valor).replace("R$", "").replace(" ", "").strip()
    if not texto:
        return None
    if "," in texto:  # formato brasileiro: ponto = milhar, vírgula = decimal
        texto = texto.replace(".", "").replace(",", ".")
    try:
        numero = float(texto)
    except ValueError:
        return None
    return numero if numero >= 0 else None


def converter_data(valor) -> date | None:
    """Aceita 'dd/mm/aaaa', 'aaaa-mm-dd' e 'aaaa-mm-dd hh:mm:ss'. Só a partir de 1990."""
    texto = texto_limpo(valor)
    if not texto:
        return None
    for formato in ("%Y-%m-%d", "%d/%m/%Y", "%Y-%m-%d %H:%M:%S"):
        try:
            d = datetime.strptime(texto, formato).date()
            return d if d.year >= 1990 else None
        except ValueError:
            continue
    return None


def separar_servicos(valor) -> list[str]:
    texto = texto_limpo(valor)
    if not texto:
        return []
    vistos, servicos = set(), []
    for s in texto.split(";"):
        s = texto_limpo(s)
        if s and sem_acento(s) not in vistos:
            vistos.add(sem_acento(s))
            servicos.append(s)
    return servicos


def limpar(linhas: list[dict]) -> tuple[pd.DataFrame, list[dict]]:
    """Recebe as linhas (camelCase, como vêm do Java) e devolve:
    - DataFrame só com as linhas válidas e padronizadas
    - lista de rejeitadas: [{numeroLinha, motivos}]
    """
    validas, rejeitadas = [], []
    codigos_vistos = set()

    for linha in linhas:
        motivos = []
        numero = linha.get("numeroLinha")

        codigo = texto_limpo(linha.get("codigoCliente"))
        if not codigo or not re.fullmatch(r"[A-Za-z0-9_-]+", codigo):
            motivos.append("código inválido")
        elif codigo.upper() in codigos_vistos:
            motivos.append("código repetido")

        nome = texto_limpo(linha.get("nomeCliente"))
        if not nome:
            motivos.append("nome vazio")

        consultor = texto_limpo(linha.get("consultor"))
        if not consultor:
            motivos.append("consultor vazio")

        segmento = padronizar_segmento(linha.get("segmento"))
        if not segmento:
            motivos.append("segmento desconhecido")

        nivel = (texto_limpo(linha.get("nivelCliente")) or "").upper()
        if nivel not in ("A", "B", "C"):
            motivos.append("nível deve ser A, B ou C")

        faturamento = converter_faturamento(linha.get("faturamentoAnual"))
        if faturamento is None:
            motivos.append("faturamento inválido")

        servicos = separar_servicos(linha.get("servicosContratados"))
        if not servicos:
            motivos.append("sem serviços")

        data = converter_data(linha.get("dataContratacao"))
        if not data:
            motivos.append("data inválida")

        uf = (texto_limpo(linha.get("uf")) or "").upper() or None
        if uf and not re.fullmatch(r"[A-Z]{2}", uf):
            motivos.append("UF inválida")

        if motivos:
            rejeitadas.append({"numeroLinha": numero, "motivos": motivos})
            continue

        codigos_vistos.add(codigo.upper())
        cidade = texto_limpo(linha.get("cidade"))
        validas.append({
            "codigo": codigo.upper(),
            "nome": nome,
            "consultor": capitalizar_nome(consultor),
            "segmento": segmento,
            "nivel": nivel,
            "faturamento": faturamento,
            "servicos": servicos,
            "data_contratacao": data,
            "cidade": capitalizar_nome(cidade) if cidade else None,
            "uf": uf,
        })

    colunas = ["codigo", "nome", "consultor", "segmento", "nivel", "faturamento",
               "servicos", "data_contratacao", "cidade", "uf"]
    return pd.DataFrame(validas, columns=colunas), rejeitadas
