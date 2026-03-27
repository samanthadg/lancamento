# Lancamento (Despesas e Receitas)

Aplicação web simples para **listar lançamentos** (despesas/receitas) armazenados em **PostgreSQL**.

## Requisitos

- **Sem Docker**: Java 21 + Maven + PostgreSQL 18

## Subir sem Docker (Java + Postgres já instalados)

1) Crie o banco/usuário (ajuste senha se quiser):

```sql
CREATE DATABASE lancamento_db;
CREATE USER lancamento_user WITH PASSWORD 'lancamento_pass';
GRANT ALL PRIVILEGES ON DATABASE lancamento_db TO lancamento_user;
```

2) Rode a aplicação:

```bash
cd app
mvn spring-boot:run
```

## VM Ubuntu 24 (publicação)

Na VM (como root):

```bash
sudo bash scripts/provision-ubuntu24.sh
```

Depois copie o projeto para `/opt/lancamento`, compile e habilite o serviço:

```bash
cd /opt/lancamento/app
mvn -DskipTests package
sudo systemctl enable --now lancamento
```

A forma mais simples (um comando, sem Docker) é:

```bash
sudo bash scripts/install-build-run-ubuntu24.sh
```

Acesse:
- `http://IP_PUBLICO_DA_VM:8080/lancamentos`

## Dados iniciais (INSERT)

No startup, o Spring Boot executa `schema.sql` e `data.sql` e popula automaticamente:
- `usuario`: 1 usuário (`financeiro` / `fin2026`)
- `lancamento`: 10 registros

## Autenticação

Para acessar a listagem (`/lancamentos`), é necessário autenticar em `/login` usando um usuário da tabela `usuario`.

Se você precisar executar “direto no banco” via script SQL:
- `scripts/sql/01_create_tables.sql`
- `scripts/sql/02_seed.sql`

