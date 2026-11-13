"""Gravação no PostgreSQL (Neon). Tudo numa transação: ou grava o lote inteiro, ou nada."""
import os

import psycopg
from psycopg.types.json import Jsonb


def conectar():
    url = os.environ.get("DATABASE_URL")
    if not url:
        raise RuntimeError("Defina DATABASE_URL no .env (string de conexão do Neon)")
    return psycopg.connect(url)


def _id_por_nome(cur, tabela: str, nome: str, cache: dict) -> int:
    """Busca o id de segmento/consultor/servico pelo nome; cria se não existir."""
    if nome in cache:
        return cache[nome]
    # tabela vem só do nosso código (nunca do usuário), então o f-string é seguro aqui
    cur.execute(
        f"INSERT INTO {tabela} (nome) VALUES (%s) "
        f"ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome RETURNING id",
        (nome,),
    )
    cache[nome] = cur.fetchone()[0]
    return cache[nome]


def gravar_lote(arquivo_nome: str, usuario_id: int | None, df, resultado: dict) -> int:
    q = resultado["qualidade"]
    with conectar() as conn, conn.cursor() as cur:
        cur.execute(
            "INSERT INTO upload_lote (arquivo_nome, usuario_id, linhas_recebidas, linhas_validas, linhas_invalidas) "
            "VALUES (%s, %s, %s, %s, %s) RETURNING id",
            (arquivo_nome, usuario_id, q["linhasRecebidas"], q["linhasValidas"], q["linhasInvalidas"]),
        )
        lote_id = cur.fetchone()[0]

        cache_seg, cache_cons, cache_serv = {}, {}, {}
        for l in df.itertuples():
            cur.execute(
                "INSERT INTO cliente (lote_id, codigo, nome, consultor_id, segmento_id, nivel, "
                "faturamento_anual, data_contratacao, cidade, uf) "
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) RETURNING id",
                (lote_id, l.codigo, l.nome,
                 _id_por_nome(cur, "consultor", l.consultor, cache_cons),
                 _id_por_nome(cur, "segmento", l.segmento, cache_seg),
                 l.nivel, l.faturamento, l.data_contratacao, l.cidade, l.uf),
            )
            cliente_id = cur.fetchone()[0]
            for servico in l.servicos:
                cur.execute(
                    "INSERT INTO contrato (cliente_id, servico_id) VALUES (%s, %s)",
                    (cliente_id, _id_por_nome(cur, "servico", servico, cache_serv)),
                )

        cur.execute(
            "INSERT INTO estatistica_lote (lote_id, resultado) VALUES (%s, %s)",
            (lote_id, Jsonb(resultado)),
        )
    return lote_id
