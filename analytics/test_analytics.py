"""Testes da limpeza e das estatísticas (não precisam de banco).
Rodar:  cd analytics  e  python -m pytest -q   (ou: python test_analytics.py)
"""
import estatisticas
import limpeza


def linha(n, **campos):
    base = dict(numeroLinha=n, codigoCliente=f"C{n}", nomeCliente="Empresa", consultor="ana souza",
                segmento="IND.", nivelCliente="a", faturamentoAnual="1000000.00",
                servicosContratados="Cloud; Backup", dataContratacao="2025-01-10", cidade="campinas", uf="sp")
    base.update(campos)
    return base


def test_padroniza():
    df, rej = limpeza.limpar([linha(2)])
    assert rej == []
    l = df.iloc[0]
    assert (l.segmento, l.consultor, l.nivel, l.uf, l.cidade) == ("Indústria", "Ana Souza", "A", "SP", "Campinas")
    assert l.servicos == ["Cloud", "Backup"]


def test_formatos_de_faturamento_e_data():
    assert limpeza.converter_faturamento("R$ 1.850.000,50") == 1850000.50
    assert limpeza.converter_faturamento("-5") is None
    assert str(limpeza.converter_data("15/03/2024")) == "2024-03-15"
    assert limpeza.converter_data("31/02/2024") is None
    assert limpeza.converter_data("01/01/1985") is None


def test_rejeita_invalidas():
    linhas = [linha(2), linha(3, codigoCliente="C2"), linha(4, nivelCliente="E"),
              linha(5, segmento="Agro"), linha(6, consultor="  ")]
    df, rej = limpeza.limpar(linhas)
    assert len(df) == 1
    assert [r["numeroLinha"] for r in rej] == [3, 4, 5, 6]
    assert "código repetido" in rej[0]["motivos"]


def test_estatisticas_e_meses_vazios():
    linhas = [linha(2, faturamentoAnual="100"), linha(3, faturamentoAnual="300", dataContratacao="2025-03-01")]
    df, _ = limpeza.limpar(linhas)
    r = estatisticas.calcular(df, recebidas=3, invalidas=1)
    assert r["resumo"]["media"] == 200 and r["resumo"]["faturamentoTotal"] == 400
    assert [m["clientes"] for m in r["porMes"]] == [1, 0, 1]  # fevereiro entra com 0
    assert r["qualidade"] == {"linhasRecebidas": 3, "linhasValidas": 2, "linhasInvalidas": 1}


if __name__ == "__main__":
    for nome, f in list(globals().items()):
        if nome.startswith("test_"):
            f()
            print("ok", nome)
