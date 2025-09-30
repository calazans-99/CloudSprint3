# CloudSprint3 · Spring Boot + Azure SQL · DevOps Sprint

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white">
  <img alt="Build" src="https://img.shields.io/github/actions/workflow/status/calazans-99/CloudSprint3/main_cloudsprint3-rm556620.yml?label=CI%2FCD">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue">
</p>

## ⚙️ Opção Escolhida
A equipe optou pela **Opção 2 – Serviço de Aplicativo (App Service + Azure SQL)**, modelo **PaaS**.  
A aplicação foi publicada em um **Azure App Service (Linux)** conectado a um **Azure SQL Database**, atendendo aos requisitos da disciplina.

---

## 📌 Descrição
API simples para gestão de **motos** (CRUD) construída com **Spring Boot** e banco **Azure SQL**.  
O deploy é realizado no **Azure App Service** via **GitHub Actions** e a observabilidade usa **Application Insights**.

> **URL pública** (exemplo): `https://cloudsprint3-rm556620.azurewebsites.net`  
> **Swagger UI**: `/swagger-ui/index.html` · **OpenAPI**: `/v3/api-docs`

---

## 📚 Sumário
- [Arquitetura](#-arquitetura)  
- [Tecnologias](#-tecnologias)  
- [Endpoints](#-endpoints)  
- [Como executar localmente](#-como-executar-localmente)  
- [Configuração no Azure](#-configuração-no-azure)  
- [Fluxo de CI/CD](#-fluxo-de-cicd)  
- [Modelo de dados](#-modelo-de-dados)  
- [Coleção Postman](#-coleção-postman)  
- [Exemplos de requisição](#-exemplos-de-requisição)  
- [Scripts](#-scripts)  
- [Evidências em vídeo](#-evidências-em-vídeo)  
- [Conformidade com requisitos](#-conformidade-com-requisitos)  
- [Resolução de problemas](#-resolução-de-problemas)  
- [Licença](#-licença)  

---

## 🏗 Arquitetura

### Diagrama lógico (Mermaid)
```mermaid
flowchart LR
  subgraph GitHub["GitHub"]
    GH[CI/CD & Actions Workflow]
  end

  subgraph Azure["Azure"]
    subgraph RG["Resource Group"]
      ASP[App Service Plan]
      WEBAPP[Web App<br/>cloudsprint3-rm556620]
      AI[Application Insights]
      SQL[Azure SQL Database<br/>dimdimdb]
    end
  end

  USER[Cliente/Browser]

  USER -->|HTTP 80/443| WEBAPP
  GH -->|Publish Profile secret| WEBAPP
  WEBAPP -->|JDBC SQL| SQL
  WEBAPP --> AI
🧰 Tecnologias
Java 17 · Spring Boot 3.3.5

Spring Web, Validation, Spring Data JPA

Driver com.microsoft.sqlserver:mssql-jdbc

Swagger/OpenAPI via springdoc-openapi

H2 para testes locais (profile h2)

Azure: App Service (Linux), Azure SQL, Application Insights

CI/CD: GitHub Actions (deploy por Publish Profile)

🔗 Endpoints
Base path: /api/v1

Método	Caminho	Descrição
GET	/motos	Lista todas
GET	/motos/{id}	Busca por ID
POST	/motos	Cria uma moto
PUT	/motos/{id}	Atualiza uma moto
DELETE	/motos/{id}	Remove uma moto

▶️ Como executar localmente
Pré-requisitos
JDK 17+

Maven 3.9+

Usando H2 (memória)
bash
Copiar código
mvn spring-boot:run -Dspring-boot.run.profiles=h2
# Swagger: http://localhost:8080/swagger-ui/index.html
# H2 Console: http://localhost:8080/h2-console  (JDBC URL: jdbc:h2:mem:testdb)
Usando SQL Server (local/Azure)
Configurar em src/main/resources/application.properties ou via variáveis de ambiente:

ini
Copiar código
spring.datasource.url=jdbc:sqlserver://<server>.database.windows.net:1433;database=<db>;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
spring.datasource.username=<usuario>
spring.datasource.password=<senha>
spring.jpa.hibernate.ddl-auto=update
Executar:

bash
Copiar código
mvn clean package -DskipTests
java -jar target/sprint3-sqlserver-0.0.1-SNAPSHOT.jar
☁️ Configuração no Azure
Criar Resource Group, App Service Plan (Linux), Azure SQL Database e Application Insights.

Definir App Settings no App Service:

SPRING_DATASOURCE_URL

SPRING_DATASOURCE_USERNAME

SPRING_DATASOURCE_PASSWORD

(Opcional) APPLICATIONINSIGHTS_CONNECTION_STRING

Configurar firewall do Azure SQL para liberar acesso do App Service.

Deploy automatizado com o script deploy-cloud-*.sh.

🚀 Fluxo de CI/CD
Build: mvn -B -DskipTests package

Artefato: target/*.jar

Deploy: azure/webapps-deploy@v2 usando secret AZURE_WEBAPP_PUBLISH_PROFILE

🗃 Modelo de dados
Tabela MOTOS:

id (PK), placa (UK), modelo, status, created_at

🧪 Coleção Postman
Arquivo CloudSprint3.postman_collection.json no repositório.

Variável host: http://localhost:8080 ou https://cloudsprint3-rm556620.azurewebsites.net

📬 Exemplos de requisição
http
Copiar código
POST {{host}}/api/v1/motos
Content-Type: application/json
{
  "placa": "ABC1D23",
  "modelo": "Honda CG 160",
  "status": "ATIVA"
}
(mais exemplos no repositório e no vídeo)

📂 Scripts
script_bd.sql → DDL da tabela motos e inserts de exemplo

deploy-cloud-marcus.sh → cria RG, App Service, Azure SQL e Insights via CLI

deploycomandos.txt → comandos de execução do script

🎥 Evidências em vídeo
O vídeo da entrega mostra:

Clone do repositório

Deploy via script/CI

Criação e configuração do App Service + Azure SQL

CRUD completo no sistema e conferência no banco

✅ Conformidade com requisitos
Banco em nuvem (Azure SQL) ✅

CRUD completo com registros reais ✅

Deploy em App Service via CLI/GitHub Actions ✅

Repositório GitHub com código e documentação ✅

Vídeo com evidência de todas as operações CRUD ✅

PDF de entrega com nomes, RMs, links e arquitetura ✅

🛠 Resolução de problemas
Swagger não abre em “/” → acessar /swagger-ui/index.html

Erro “no main manifest attribute” → garantir build via spring-boot-maven-plugin

Conexão com banco falhando → revisar App Settings e firewall do Azure SQL

Incompatibilidade H2/SQL Server → usar -Dspring-boot.run.profiles=h2
