#!/bin/bash
set -e

# =========================
# VARIÁVEIS (AJUSTADAS)
# =========================
RESOURCE_GROUP_NAME="rg-cloudsprint3-calazans"
WEBAPP_NAME="cloudsprint3-rm556620"
APP_SERVICE_PLAN="planCloudSprint3"
LOCATION="northcentralus"
RUNTIME="JAVA:17-java17"

RG_DB_NAME="rg-cloudsprint3-calazans-db"
DB_USERNAME="admsql"
DB_NAME="dimdimdb"
DB_PASSWORD="Fiap@2tdsvms"
SERVER_NAME="sqlserver-rm556620"

APP_INSIGHTS_NAME="ai-cloudsprint3"
GITHUB_REPO_NAME="calazans-99/CloudSprint3"
BRANCH="main"

# =========================
# PROVIDERS & EXTENSIONS
# =========================
az provider register --namespace Microsoft.Web
az provider register --namespace Microsoft.Insights
az provider register --namespace Microsoft.OperationalInsights
az provider register --namespace Microsoft.ServiceLinker
az provider register --namespace Microsoft.Sql
az extension add --name application-insights || true

# =========================
# RESOURCE GROUPS
# =========================
az group create --name "$RG_DB_NAME" --location "$LOCATION"
az group create --name "$RESOURCE_GROUP_NAME" --location "$LOCATION"

# =========================
# AZURE SQL (SERVER + DB)
# =========================
az sql server create \
  --name "$SERVER_NAME" \
  --resource-group "$RG_DB_NAME" \
  --location "$LOCATION" \
  --admin-user "$DB_USERNAME" \
  --admin-password "$DB_PASSWORD" \
  --enable-public-network true

az sql db create \
  --resource-group "$RG_DB_NAME" \
  --server "$SERVER_NAME" \
  --name "$DB_NAME" \
  --service-objective Basic \
  --backup-storage-redundancy Local

# Firewall amplo (DEV APENAS)
az sql server firewall-rule create \
  --resource-group "$RG_DB_NAME" \
  --server "$SERVER_NAME" \
  --name AllowAllDevTEMP \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 255.255.255.255
echo "⚠️  Firewall 0.0.0.0/255.255.255.255 habilitado (somente DEV)."

# =========================
# SEED OPCIONAL (sqlcmd)
# =========================
if command -v sqlcmd >/dev/null 2>&1; then
  echo "🌱 Criando tabela 'motos' e inserindo dados iniciais..."
  sqlcmd -S "$SERVER_NAME.database.windows.net" -d "$DB_NAME" -U "$DB_USERNAME" -P "$DB_PASSWORD" -l 60 -N -b <<'EOF'
IF OBJECT_ID('dbo.motos','U') IS NULL
BEGIN
  CREATE TABLE dbo.motos (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    modelo VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
  );
END;

IF NOT EXISTS (SELECT 1 FROM dbo.motos WHERE placa='ABC1D23')
  INSERT INTO dbo.motos (placa, modelo, status) VALUES ('ABC1D23','Honda CG 160','ATIVA');

IF NOT EXISTS (SELECT 1 FROM dbo.motos WHERE placa='XYZ4E56')
  INSERT INTO dbo.motos (placa, modelo, status) VALUES ('XYZ4E56','Yamaha Fazer 250','EM_MANUTENCAO');
GO
EOF
else
  echo "ℹ️  sqlcmd não encontrado — pulando seed. O Hibernate criará a tabela no primeiro start."
fi

# =========================
# APPLICATION INSIGHTS
# =========================
az monitor app-insights component create \
  --app "$APP_INSIGHTS_NAME" \
  --location "$LOCATION" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --application-type web

CONNECTION_STRING=$(az monitor app-insights component show \
  --app "$APP_INSIGHTS_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --query connectionString -o tsv)

# =========================
# APP SERVICE (PLAN + WEBAPP)
# =========================
az appservice plan create \
  --name "$APP_SERVICE_PLAN" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --location "$LOCATION" \
  --sku B1 \
  --is-linux

az webapp create \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --plan "$APP_SERVICE_PLAN" \
  --runtime "$RUNTIME"

# =========================
# APP SETTINGS
# =========================
SPRING_DATASOURCE_URL="jdbc:sqlserver://$SERVER_NAME.database.windows.net:1433;database=$DB_NAME;user=$DB_USERNAME@$SERVER_NAME;password=$DB_PASSWORD;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

az webapp config appsettings set \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --settings \
    APPLICATIONINSIGHTS_CONNECTION_STRING="$CONNECTION_STRING" \
    ApplicationInsightsAgent_EXTENSION_VERSION="~3" \
    XDT_MicrosoftApplicationInsights_Mode="Recommended" \
    XDT_MicrosoftApplicationInsights_PreemptSdk="1" \
    SPRING_DATASOURCE_USERNAME="$DB_USERNAME" \
    SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD" \
    SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL"

# Reiniciar Web App
az webapp restart --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP_NAME"

# =========================
# GITHUB ACTIONS (CI/CD)
# =========================
az webapp deployment github-actions add \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --repo "$GITHUB_REPO_NAME" \
  --branch "$BRANCH" \
  --login-with-github || echo "(Aviso) Não foi possível configurar GitHub Actions automaticamente."

echo "✅ Deploy concluído!"
echo "🌐 URL: https://$WEBAPP_NAME.azurewebsites.net"
echo "📊 App Insights: $APP_INSIGHTS_NAME"
echo "🗄  Banco: $DB_NAME @ $SERVER_NAME"
echo "🔐 Lembre de restringir o firewall em produção."
