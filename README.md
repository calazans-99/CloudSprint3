# CloudSprint3 — Spring Boot + Azure SQL (DevOps Sprint 3)

Projeto **Java 17 / Spring Boot 3** com API REST de **motos**, preparado para:
- rodar **localmente** com H2 (perfil `h2`);
- rodar **com SQL Server** (local ou Azure SQL);
- **deploy no Azure App Service** com CI/CD via **GitHub Actions**;
- documentação automática no **Swagger**.

> Repositório: `calazans-99/CloudSprint3`  
> App Service: `cloudsprint3-rm556620`  
> Swagger (produção): `https://cloudsprint3-rm556620.azurewebsites.net/swagger-ui/index.html`

---

## Sumário
- [Arquitetura (diagramas)](#arquitetura-diagramas)
- [Tecnologias](#tecnologias)
- [Como rodar local (H2)](#como-rodar-local-h2)
- [Como rodar com SQL Server](#como-rodar-com-sql-server)
- [Build e empacotamento JAR](#build-e-empacotamento-jar)
- [Executar o JAR](#executar-o-jar)
- [Swagger / OpenAPI](#swagger--openapi)
- [Endpoints](#endpoints)
- [Exemplos Postman (prontos)](#exemplos-postman-prontos)
- [Variáveis de ambiente (perfil default)](#variáveis-de-ambiente-perfil-default)
- [Deploy no Azure App Service](#deploy-no-azure-app-service)
  - [Script de provisionamento (opcional)](#script-de-provisionamento-opcional)
  - [CI/CD com GitHub Actions](#cicd-com-github-actions)
- [Modelagem / JPA](#modelagem--jpa)
- [Dicas e troubleshooting](#dicas-e-troubleshooting)
- [Licença](#licença)

---

## Arquitetura (diagramas)

### Diagrama lógico (Mermaid)
> Visualização da app no Azure com Azure SQL e GitHub Actions.

```mermaid
flowchart LR
    subgraph GitHub["GitHub"]
      GH[Code & Actions Workflow]
    end

    subgraph Azure["Azure"]
      subgraph RG["Resource Group"]
        ASP[App Service Plan]
        WA[Web App\n(cloudsprint3-rm556620)]
        AI[Application Insights]
      end
      SQL[Azure SQL Database\n(dimdimdb)]
    end

    User[Cliente/Browser] -->|HTTP 80/443| WA
    GH -->|CI/CD Publish Profile| WA
    WA -->|JDBC| SQL
    WA --> AI
```

### Diagrama de camadas (ASCII)
```
┌─────────────────────────────────┐
│           Controller            │  → REST (JSON)
│  - MotoController               │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│             Service             │  (regra de negócio simples/validações)
│  - (neste projeto a lógica é    │
│     direta no Controller)       │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│           Repository            │  → Spring Data JPA
│  - MotoRepository               │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│             JPA/Hibernate       │  → DDL auto (cria/atualiza tabela)
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│        Banco de Dados           │  → H2 (dev) | SQL Server/Azure SQL (prod)
└─────────────────────────────────┘
```

---

## Tecnologias

- Java 17
- Spring Boot 3 (web, validation, data-jpa)
- H2 (dev)
- SQL Server JDBC (`com.microsoft.sqlserver:mssql-jdbc`)
- springdoc-openapi (Swagger)
- Maven
- Azure App Service + Azure SQL
- GitHub Actions

---

## Como rodar local (H2)

1. **Pré-requisitos**: JDK 17, Maven 3.9+.
2. **Perfil H2** com _in-memory database_.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

- Console H2: `http://localhost:8080/h2-console`  
  (JDBC URL padrão do H2 em memória costuma ser `jdbc:h2:mem:testdb`)

---

## Como rodar com SQL Server

1. Ajuste `src/main/resources/application.properties` (perfil **default**) para ler as variáveis de ambiente:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`

2. Exemplo local (Windows PowerShell):
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:sqlserver://localhost:1433;database=dimdimdb;encrypt=false"
$env:SPRING_DATASOURCE_USERNAME="sa"
$env:SPRING_DATASOURCE_PASSWORD="SUA_SENHA"
mvn spring-boot:run
```

3. **Azure SQL** (App Service):
   - Essas variáveis são configuradas no App Service → **Configuration**.
   - No script de deploy elas já são populadas automaticamente.

> **Observação**: O projeto usa criação de schema via **Hibernate** (DDL auto) ao subir, sem Flyway.

---

## Build e empacotamento JAR

O projeto gera um **fat JAR** (com `Main-Class`) via `spring-boot-maven-plugin` com o goal `repackage`.

```bash
mvn clean package -DskipTests
```

Verifique o MANIFEST (opcional):
```bash
# Windows PowerShell (usa jar.exe do JDK)
$jar = Get-ChildItem target\*.jar | Where-Object { $_.Name -notmatch 'original' } | Select-Object -First 1
& "$env:JAVA_HOME\bin\jar.exe" tf $jar.FullName | Select-String META-INF/MANIFEST.MF
& "$env:JAVA_HOME\bin\jar.exe" xf $jar.FullName META-INF/MANIFEST.MF
type META-INF\MANIFEST.MF
```

Você deve ver algo como:
```
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: com.fiap.sprint3sqlserver.Application
```

---

## Executar o JAR

```bash
java -jar target/sprint3-sqlserver-0.0.1-SNAPSHOT.jar
# ou definindo perfil H2:
java -Dspring.profiles.active=h2 -jar target/sprint3-sqlserver-0.0.1-SNAPSHOT.jar
```

---

## Swagger / OpenAPI

- Local: `http://localhost:8080/swagger-ui/index.html`
- Produção (Azure): `https://cloudsprint3-rm556620.azurewebsites.net/swagger-ui/index.html`

Opcional: redirecionar `/` → Swagger adicionando um `HomeController` que faz `return "redirect:/swagger-ui/index.html"`.

---

## Endpoints

Base path: `/api/v1/motos`

| Método | Rota                 | Descrição            |
|-------:|----------------------|----------------------|
| GET    | `/api/v1/motos`      | Lista todas          |
| GET    | `/api/v1/motos/{id}` | Busca por id         |
| POST   | `/api/v1/motos`      | Cria registro        |
| PUT    | `/api/v1/motos/{id}` | Atualiza por id      |
| DELETE | `/api/v1/motos/{id}` | Remove por id        |

### Exemplo corpo (POST/PUT)
```json
{
  "placa": "AAA1B23",
  "modelo": "Honda CG 160",
  "status": "ATIVA"
}
```
> **Não** enviar `id` no POST (é gerado pelo banco).  
> `placa` é **única**.

---

## Exemplos Postman (prontos)

### 0) Coleção pronta
Importe no Postman o arquivo **`CloudSprint3.postman_collection.json`** deste repo (ou baixe abaixo).  
A coleção usa a variável `{{baseUrl}}`:
- Local (H2): `http://localhost:8080`
- Produção (Azure): `https://cloudsprint3-rm556620.azurewebsites.net`

> Para baixar direto: veja os links no fim deste README.

### 1) POST — Criar moto
- **URL**: `{{baseUrl}}/api/v1/motos`
- **Body (raw / application/json)**:
```json
{
  "placa": "AAA1B23",
  "modelo": "Honda CG 160",
  "status": "ATIVA"
}
```
- **Resposta 201/200 (exemplo)**:
```json
{
  "id": 1,
  "placa": "AAA1B23",
  "modelo": "Honda CG 160",
  "status": "ATIVA",
  "created_at": "2025-09-27T18:56:40.736Z"
}
```

### 2) GET — Listar todas
- **URL**: `{{baseUrl}}/api/v1/motos`
- **Resposta 200 (exemplo)**:
```json
[
  { "id": 1, "placa": "AAA1B23", "modelo": "Honda CG 160", "status": "ATIVA" },
  { "id": 2, "placa": "BBB2C34", "modelo": "Yamaha Fazer 250", "status": "EM_MANUTENCAO" }
]
```

### 3) GET — Buscar por id
- **URL**: `{{baseUrl}}/api/v1/motos/1`
- **Resposta 200 (exemplo)**:
```json
{ "id": 1, "placa": "AAA1B23", "modelo": "Honda CG 160", "status": "ATIVA" }
```

### 4) PUT — Atualizar por id
- **URL**: `{{baseUrl}}/api/v1/motos/1`
- **Body**:
```json
{
  "placa": "AAA1B23",
  "modelo": "Honda CG 160 Start",
  "status": "EM_MANUTENCAO"
}
```
- **Resposta 200 (exemplo)**:
```json
{ "id": 1, "placa": "AAA1B23", "modelo": "Honda CG 160 Start", "status": "EM_MANUTENCAO" }
```

### 5) DELETE — Remover por id
- **URL**: `{{baseUrl}}/api/v1/motos/1`
- **Resposta 204** (sem corpo) ou 200 com mensagem (dependendo de como o controller trata).

> **Observações**  
> - 400: validações de request falhando (`@Valid`).  
> - 404: id não encontrado.  
> - 409/500: `placa` duplicada (coluna `UNIQUE`).

---

## Variáveis de ambiente (perfil default)

- `SPRING_DATASOURCE_URL`  
  Ex.: `jdbc:sqlserver://<server>.database.windows.net:1433;database=<db>;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;`
- `SPRING_DATASOURCE_USERNAME`  
  Ex.: `admsql`
- `SPRING_DATASOURCE_PASSWORD`

No Azure, configure no **App Service → Configuration → Application settings**.

---

## Deploy no Azure App Service

### Script de provisionamento (opcional)

Há um script `deploy-cloud-*.sh` que:
- cria Resource Groups (app e db);
- provisiona **Azure SQL Server + Database**;
- abre firewall **(apenas DEV)**;
- cria **App Insights**, **App Service Plan**, **Web App**;
- seta `SPRING_DATASOURCE_*` no App Service;
- opcionalmente configura **GitHub Actions** para CI/CD.

> No **Azure Cloud Shell** (Bash), suba o script e rode `chmod +x ./deploy.sh && ./deploy.sh`.

### CI/CD com GitHub Actions

Workflow (`.github/workflows/main_cloudsprint3-rm556620.yml`) faz:
- build Maven com JDK 17;
- seleciona o **fat JAR** (exclui `*-original.jar`);
- deploy para o Web App usando **Publish Profile** salvo no secret `AZURE_WEBAPP_PUBLISH_PROFILE`.

Gerar/atualizar Publish Profile:
```bash
az webapp deployment list-publishing-profiles -g rg-cloudsprint3-calazans -n cloudsprint3-rm556620 --xml
```
Crie o secret no GitHub `AZURE_WEBAPP_PUBLISH_PROFILE` com **todo o XML**.

---

## Modelagem / JPA

Entidade `Moto` (resumo):
- `id` (BIGINT, identity, PK)
- `placa` (VARCHAR(10), **UNIQUE**, NOT NULL)
- `modelo` (VARCHAR(80), NOT NULL)
- `status` (VARCHAR(30), NOT NULL)
- `created_at` (DateTimeOffset, default `SYSDATETIMEOFFSET()` no SQL Server / `timestamp with time zone` no H2)

> No ambiente H2 o Hibernate ajusta os tipos automaticamente.

---

## Dicas e troubleshooting

- **Whitelabel 404 na raiz**: a API não define `/`. Acesse o **Swagger**.
- **Erro “no main manifest attribute”**: garanta `spring-boot-maven-plugin` com `repackage` e que o Actions está enviando o JAR **sem** `original` no nome.
- **Erro ao conectar no banco**: confira `SPRING_DATASOURCE_*` no App Service e se o firewall do Azure SQL permite acesso do App Service.
- **Placa duplicada**: a coluna `placa` é `UNIQUE`. Troque o valor ao criar.
- **Latency alta no primeiro boot**: o App Service “acorda” o container (cold start). Depois estabiliza.

---

## Licença

Este projeto é de uso educacional para a Sprint 3 (DevOps). Adapte conforme sua necessidade.
