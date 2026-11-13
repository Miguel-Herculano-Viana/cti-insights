# Contrato entre as partes

Se mudar algo aqui, mude no front (`utils/planilha.js`), no Java (`UploadService`) e no Python (`limpeza.py`).

## Planilha (aba `upload_clientes`)
| Coluna | Obrigatória | Regra |
|---|---|---|
| codigo_cliente | sim | letras, números, `-` ou `_`; não repete (a 1ª ocorrência fica) |
| nome_cliente | sim | texto |
| consultor | sim | texto (vira "Nome Sobrenome") |
| segmento | sim | Indústria, Comércio, Serviços, Saúde, Educação, Tecnologia (aceita IND., industria, COMÉRCIO...) |
| nivel_cliente | sim | A, B ou C (minúscula é aceita) |
| faturamento_anual | sim | número ≥ 0 (`1850000`, `1.850.000,50`, `R$ 1.850.000,50`) |
| servicos_contratados | sim | separados por `;` |
| data_contratacao | sim | `dd/mm/aaaa`, `aaaa-mm-dd` ou data do Excel; a partir de 1990 |
| cidade | não | texto |
| uf | não | 2 letras |

## Rotas do Java (`/api`)
| Rota | Quem | Para quê |
|---|---|---|
| `POST /auth/register` | todos | `{nome, email, senha}` → cria como PENDENTE |
| `POST /auth/login` | todos | `{email, senha}` → `{token, usuario}` |
| `GET /auth/me` | logado | dados do usuário |
| `GET /admin/usuarios?status=PENDENTE` | ADMIN | lista (sem `status` = todos) |
| `PATCH /admin/usuarios/{id}/status` | ADMIN | `{"status": "APROVADO" \| "BLOQUEADO"}` |
| `POST /uploads/validadas` | logado | envia as linhas válidas → `{loteId, rejeitadas, resumo, qualidade}` |
| `GET /dashboard?lote=ID` | logado | `{lote, geradoEm, estatisticas}` (sem `lote` = último) |
| `GET /dashboard/lotes` | logado | histórico de envios |

Cabeçalho: `Authorization: Bearer <token>`. Erros: `{"erro": "mensagem"}`.

## Corpo do upload
```json
{ "arquivoNome": "dados.xlsx", "descartadas": 6,
  "linhas": [ { "numeroLinha": 2, "codigoCliente": "CTI001", "nomeCliente": "Metalúrgica Horizonte",
               "consultor": "Ana Souza", "segmento": "Indústria", "nivelCliente": "A",
               "faturamentoAnual": "1850000.00", "servicosContratados": "Internet Dedicada;Firewall",
               "dataContratacao": "2025-01-15", "cidade": "Campinas", "uf": "SP" } ] }
```
O Java repassa ao Python em `POST /processar` (com `usuarioId` e o cabeçalho `X-Chave-Interna`).

## JSON das estatísticas (`estatistica_lote.resultado`)
`resumo` (totalClientes, faturamentoTotal, media, mediana, desvioPadrao, minimo, maximo, q1, q3),
`porSegmento`, `porNivel`, `porConsultor`, `porServico`, `porMes` (meses sem cliente = 0),
`correlacao` (Pearson faturamento × nº de serviços), `topClientes` (5), `qualidade`, `insights` (frases).
NaN e infinito viram `null`.
