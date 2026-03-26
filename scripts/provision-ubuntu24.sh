#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/lancamento"
DB_NAME="lancamento_db"
DB_USER="lancamento_user"
DB_PASS="lancamento_pass"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Rode como root: sudo $0"
  exit 1
fi

echo "[1/8] Atualizando sistema e instalando dependências base..."
apt-get update -y
apt-get install -y ca-certificates curl gnupg lsb-release unzip git ufw maven

echo "[2/8] Instalando Java (Temurin 21)..."
mkdir -p /etc/apt/keyrings
curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public \
  | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" \
  > /etc/apt/sources.list.d/adoptium.list
apt-get update -y
apt-get install -y temurin-21-jdk

echo "[3/8] Instalando PostgreSQL 18 (PGDG)..."
curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
  | gpg --dearmor -o /etc/apt/keyrings/postgresql.gpg
echo "deb [signed-by=/etc/apt/keyrings/postgresql.gpg] http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
  > /etc/apt/sources.list.d/pgdg.list
apt-get update -y
apt-get install -y postgresql-18 postgresql-client-18

echo "[4/8] Criando banco e usuário..."
sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}') THEN
    CREATE DATABASE ${DB_NAME};
  END IF;
END
\$\$;

DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USER}') THEN
    CREATE ROLE ${DB_USER} LOGIN PASSWORD '${DB_PASS}';
  END IF;
END
\$\$;

GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
SQL

echo "[5/8] Preparando diretório da aplicação..."
mkdir -p "${APP_DIR}"
chown -R ubuntu:ubuntu "${APP_DIR}" || true

echo "[6/8] Criando serviço systemd (opcional)..."
cat >/etc/lancamento.env <<EOF
DB_URL=jdbc:postgresql://localhost:5432/${DB_NAME}
DB_USER=${DB_USER}
DB_PASSWORD=${DB_PASS}
PORT=8080
EOF

cat >/etc/systemd/system/lancamento.service <<'EOF'
[Unit]
Description=Aplicação Lancamento (Spring Boot)
After=network.target postgresql.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/lancamento/app
EnvironmentFile=/etc/lancamento.env
ExecStart=/usr/bin/java -jar /opt/lancamento/app/target/lancamento-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload

echo "[7/8] Abrindo porta 8080 no firewall (ufw)..."
ufw allow 8080/tcp || true
ufw --force enable || true

echo "[8/8] Concluído."

cat <<EOF

Provisionamento concluído.

Próximos passos:
1) Copie o projeto para a VM (ex.: scp -r . ubuntu@SEU_IP:${APP_DIR})
2) Compile (como ubuntu):
   cd ${APP_DIR}/app
   mvn -DskipTests package

3) Inicie como serviço (recomendado):
   sudo systemctl enable --now lancamento
   sudo systemctl status lancamento --no-pager

Ou rode via Docker (se preferir):
   cd ${APP_DIR}
   docker compose up -d --build

Depois acesse:
  http://SEU_IP_PUBLICO:8080/lancamentos

EOF

