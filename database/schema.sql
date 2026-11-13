-- CTI Insights — schema do banco (PostgreSQL / Neon)
-- Pode rodar quantas vezes quiser: tudo usa IF NOT EXISTS.
-- O Spring Boot executa este mesmo arquivo ao iniciar (spring.sql.init).

-- ---------- Usuários (Java cuida) ----------
CREATE TABLE IF NOT EXISTS usuario (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',   -- ADMIN | USUARIO
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',  -- PENDENTE | APROVADO | BLOQUEADO
    criado_em   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('ADMIN', 'USUARIO')),
    CONSTRAINT ck_usuario_status CHECK (status IN ('PENDENTE', 'APROVADO', 'BLOQUEADO'))
);

-- ---------- Dados da planilha (Python grava) ----------
CREATE TABLE IF NOT EXISTS upload_lote (
    id                BIGSERIAL PRIMARY KEY,
    arquivo_nome      VARCHAR(255) NOT NULL,
    usuario_id        BIGINT REFERENCES usuario(id),
    linhas_recebidas  INT NOT NULL,
    linhas_validas    INT NOT NULL,
    linhas_invalidas  INT NOT NULL,
    criado_em         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS segmento (
    id    SERIAL PRIMARY KEY,
    nome  VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS consultor (
    id    SERIAL PRIMARY KEY,
    nome  VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS servico (
    id    SERIAL PRIMARY KEY,
    nome  VARCHAR(120) NOT NULL UNIQUE
);

-- Um cliente pertence a um lote. Cada upload gera um lote novo,
-- então o mesmo código pode aparecer em lotes diferentes.
CREATE TABLE IF NOT EXISTS cliente (
    id                 BIGSERIAL PRIMARY KEY,
    lote_id            BIGINT NOT NULL REFERENCES upload_lote(id) ON DELETE CASCADE,
    codigo             VARCHAR(40)  NOT NULL,
    nome               VARCHAR(200) NOT NULL,
    consultor_id       INT NOT NULL REFERENCES consultor(id),
    segmento_id        INT NOT NULL REFERENCES segmento(id),
    nivel              CHAR(1) NOT NULL CHECK (nivel IN ('A', 'B', 'C')),
    faturamento_anual  NUMERIC(15, 2) NOT NULL CHECK (faturamento_anual >= 0),
    data_contratacao   DATE NOT NULL,
    cidade             VARCHAR(120),
    uf                 CHAR(2),
    UNIQUE (lote_id, codigo)
);

-- Relação N:N cliente x serviço (um "contrato" por serviço contratado)
CREATE TABLE IF NOT EXISTS contrato (
    cliente_id  BIGINT NOT NULL REFERENCES cliente(id) ON DELETE CASCADE,
    servico_id  INT NOT NULL REFERENCES servico(id),
    PRIMARY KEY (cliente_id, servico_id)
);

-- Resultado das contas do Python, pronto para o dashboard
CREATE TABLE IF NOT EXISTS estatistica_lote (
    lote_id    BIGINT PRIMARY KEY REFERENCES upload_lote(id) ON DELETE CASCADE,
    resultado  JSONB NOT NULL,
    gerado_em  TIMESTAMP NOT NULL DEFAULT now()
);
