# Documentação — Aplicação de Registro de Despesas e Receitas

## Aplicação

### Número de classes da aplicação

Classes Java criadas (8):

- `br.com.lancamento.LancamentoApplication`
- `br.com.lancamento.domain.Lancamento`
- `br.com.lancamento.domain.Usuario`
- `br.com.lancamento.domain.TipoLancamento` (enum)
- `br.com.lancamento.domain.Situacao` (enum)
- `br.com.lancamento.repo.LancamentoRepository`
- `br.com.lancamento.web.HomeController`
- `br.com.lancamento.web.LancamentoController`

Observação: enums e controllers também são classes Java; se você precisar reportar **somente** classes “de domínio”, use apenas `Lancamento` e `Usuario`.

### Modelagem do banco de dados

**Tabela `lancamento`**

- `id` (BIGSERIAL, PK)
- `descricao` (VARCHAR(200), NOT NULL)
- `data_lancamento` (DATE, NOT NULL)
- `valor` (NUMERIC(14,2), NOT NULL)
- `tipo_lancamento` (VARCHAR(20), NOT NULL) — valores: `RECEITA` | `DESPESA`
- `situacao` (VARCHAR(20), NOT NULL) — valores usados: `PENDENTE` | `EFETIVADO` | `CANCELADO` (e também `ATIVO`/`INATIVO` existe no enum para reutilização)

**Tabela `usuario`**

- `id` (BIGSERIAL, PK)
- `nome` (VARCHAR(120), NOT NULL)
- `login` (VARCHAR(60), NOT NULL, UNIQUE)
- `senha` (VARCHAR(255), NOT NULL)
- `situacao` (VARCHAR(20), NOT NULL)

As tabelas são criadas automaticamente no startup via scripts SQL do Spring Boot:
- `app/src/main/resources/schema.sql`

Os inserts iniciais (10 lançamentos + 1 usuário) são executados no startup:
- `app/src/main/resources/data.sql`

### Interface desenvolvida

Tela web (Thymeleaf) que lista os lançamentos:

- **URL**: `/lancamentos`
- **Template**: `app/src/main/resources/templates/lancamentos/lista.html`

A rota `/` redireciona para `/lancamentos`.

## Publicação (VM Ubuntu 24 com IP público)

### Como acessar a VM

- Via SSH:
  - `ssh ubuntu@IP_PUBLICO_DA_VM`

### Instalação de cada ferramenta

O script `scripts/provision-ubuntu24.sh` instala:

- **Java 21** (Temurin)
- **Maven**
- **PostgreSQL 18** (repositório PGDG)
- utilitários: `curl`, `gnupg`, `ufw`, etc.

Execute:

```bash
sudo bash scripts/provision-ubuntu24.sh
```

### Implantação da aplicação

1) Copie o projeto para a VM (exemplo usando `scp` a partir do seu PC):

```bash
scp -r . ubuntu@IP_PUBLICO_DA_VM:/opt/lancamento
```

2) Compile o projeto:

```bash
cd /opt/lancamento/app
mvn -DskipTests package
```

3) Suba como serviço:

```bash
sudo systemctl enable --now lancamento
sudo systemctl status lancamento --no-pager
```

4) Libere a porta no firewall (o script já faz isso, mas caso precise):

```bash
sudo ufw allow 8080/tcp
```

### URL de acesso

- `http://IP_PUBLICO_DA_VM:8080/lancamentos`

## Execução sem Docker (recomendado)

Para deixar o processo mais simples possível, use o script “um comando”:

```bash
sudo bash scripts/install-build-run-ubuntu24.sh
```

URL:
- `http://IP_PUBLICO_DA_VM:8080/lancamentos`

