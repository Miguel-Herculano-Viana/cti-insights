# CTI Insights

Sistema web que recebe a planilha de clientes, aponta os erros **antes** de enviar, grava só os dados válidos e mostra um dashboard com indicadores e gráficos.

```
Vue (navegador)  ──►  Spring Boot (Java)  ──►  FastAPI (Python + Pandas)  ──►  PostgreSQL (Neon)
 lê e valida o .xlsx    login JWT, admin,        limpa, padroniza, calcula       ▲
                        valida de novo           e grava tudo numa transação     │
                        lê o dashboard ◄──────────────────────────────────────────┘
```

| Pasta | O que tem |
|---|---|
| `frontend/` | Vue 3 + Vite + Tailwind v4 + Pinia + Chart.js |
| `backend/` | Spring Boot 3.5 (Java 21): auth JWT, aprovação de usuários, upload, dashboard |
| `analytics/` | FastAPI + Pandas: limpeza, estatísticas, insights, gravação |
| `database/schema.sql` | Tabelas (o Spring roda este arquivo sozinho ao subir) |
| `docs/CONTRATO.md` | O que cada parte envia e recebe |

## Rodar no computador (3 terminais)

Pré-requisitos: Node 22+, JDK 21, Maven, Python 3.11+ e um banco no [Neon](https://neon.tech) (grátis).

**1. Python** (porta 8000)
```bash
cd analytics
python -m venv .venv && .venv\Scripts\activate      # Linux/mac: source .venv/bin/activate
pip install -r requirements.txt
copy .env.example .env                                # preencha DATABASE_URL e CHAVE_INTERNA
uvicorn api:app --port 8000 --reload
```

**2. Backend Java** (porta 8080). Suba depois do Python, mas antes de enviar planilhas.
```bash
cd backend
copy .env.example .env      # preencha DB_URL, DB_USER, DB_PASSWORD, JWT_SEGREDO e PYTHON_CHAVE (= CHAVE_INTERNA)
mvn spring-boot:run
```
Na primeira subida ele cria as tabelas e o admin do `.env` (padrão `admin@cti.local` / `Admin@12345`).

**3. Front** (porta 5173)
```bash
cd frontend
npm install
npm run dev
```

Abra http://localhost:5173, entre com o admin, envie a planilha em **Upload** e veja o **Dashboard**.
Com a `dados.xlsx` da aula o resultado esperado é: 32 válidas, 6 com erro, 61 contratos e média de R$ 2.261.250.

**Testes do Python:** `cd analytics` e depois `python test_analytics.py`

## Fluxo de usuários
1. A pessoa se cadastra e a conta fica **PENDENTE**.
2. O admin aprova em **Usuários**. Essa tela só aparece para o perfil ADMIN.
3. Só usuários **APROVADO** fazem login. Bloquear alguém corta o acesso na hora, mesmo com token válido.

## Deploy (grátis)
| Parte | Serviço | Configuração |
|---|---|---|
| Banco | Neon | Já é o mesmo das variáveis acima |
| Python | Render (Web Service, Python) | Root: `analytics` · Build: `pip install -r requirements.txt` · Start: `uvicorn api:app --host 0.0.0.0 --port $PORT` · Env: `DATABASE_URL`, `CHAVE_INTERNA` |
| Java | Render (Web Service, Docker) | Root: *(vazio)* · Dockerfile: `backend/Dockerfile` · Env: tudo do `backend/.env`, com `PYTHON_URL=https://<python>.onrender.com` e `CORS_ORIGENS=https://<seu-front>.vercel.app` |
| Front | Vercel | Root: `frontend` · Env: `VITE_API_URL=https://<java>.onrender.com/api` |

> O plano grátis do Render "dorme" depois de 15 min sem uso, e a primeira requisição pode levar cerca de 1 min. **Antes da apresentação, abra as duas URLs do Render** pra acordar os serviços.
