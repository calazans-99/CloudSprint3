# Sprint 3 – DevOps Tools & Cloud Computing (App Service + Azure SQL)

Projeto Java Spring Boot (Java 17) com CRUD de **motos** usando **Azure SQL** e **Flyway**.

## 👀 Endpoints (Swagger)
- Swagger UI: `/swagger`
- API base: `/api/v1/motos`

Exemplos:
```bash
# Listar
curl -s http://localhost:8080/api/v1/motos

# Criar
curl -s -X POST http://localhost:8080/api/v1/motos -H "Content-Type: application/json" -d '{"placa":"DEF7G89","modelo":"Honda Biz 125","status":"ATIVA"}'

# Atualizar
curl -s -X PUT http://localhost:8080/api/v1/motos/1 -H "Content-Type: application/json" -d '{"placa":"ABC1D23","modelo":"Honda CG 160 Start","status":"ATIVA"}'

# Deletar
curl -s -X DELETE http://localhost:8080/api/v1/motos/1 -i
```

## 🧱 Stack
- Spring Boot 3.3.x (Web, Validation, Data JPA)
- Flyway
- SQL Server (Azure SQL)
- OpenAPI (springdoc)

## 🚀 Rodando local (H2 modo *apenas* para desenvolvimento)
> **Atenção:** Para a correção da Sprint 3, use **Azure SQL**. O perfil H2 é apenas apoio local.
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## 🔗 Conexão (Azure SQL via variáveis de ambiente)
No App Service, configure:
- `SPRING_DATASOURCE_URL` (ex.: `jdbc:sqlserver://sqlserver-SEU-RM.database.windows.net:1433;database=dimdimdb;user=admsql@sqlserver-SEU-RM;password=SUA_SENHA;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;`)
- `SPRING_DATASOURCE_USERNAME` (ex.: `admsql`)
- `SPRING_DATASOURCE_PASSWORD` (ex.: `SUA_SENHA_FORTE`)

Locally (docker/sqlcmd) você pode ajustar `src/main/resources/application.properties`.

## 🗄️ Script DDL
Arquivo **script_bd.sql** com a DDL e comentários (extended properties).

## 🧪 Teste de CRUD (o que precisa aparecer no vídeo)
1. Criar 2 registros reais (POST).
2. Listar/Buscar (GET).
3. Atualizar (PUT).
4. Deletar (DELETE).
5. Mostrar no **banco na nuvem** as operações realizadas.

## ☁️ Deploy no Azure (App Service)
1. Registre os providers e crie Azure SQL conforme instruções do professor.
2. Execute os scripts (ajuste RM, região e repositório):
   - `create-sql-server.ps1`
   - `deploy-movtodimdim.sh`
3. O App Service vai apontar para seu repositório GitHub e fazer o deploy.

## 👤 Integrantes
- Nome 1 – RMXXXXX  
- Nome 2 – RMYYYYY

## 📝 Benefícios para o negócio (exemplo)
Gerir frota de motos (inventário, status, manutenção), reduzindo o tempo de parada e melhorando a disponibilidade operacional.

## 🎥 Vídeo
Inclua aqui o link do YouTube demonstrando:
- Deploy + Configuração
- CRUD completo via API
- Evidência no Azure SQL
