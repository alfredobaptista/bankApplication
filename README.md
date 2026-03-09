# 🏦 Banking Application

Uma API bancária moderna, segura e escalável, construída com Spring Boot e PostgreSQL, pronta para ambientes de produção

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-%20Concluido-yellow?style=for-the-badge)

## 📖 Sobre o Projeto
Uma aplicação **backend** robusta que implementa as funcionalidades essenciais de um sistema bancário moderno, seguindo boas práticas de desenvolvimento com Spring Boot.

## ✨ Principais Funcionalidades

- **Autenticação e Autorização** via OAuth2 Resource Server + JWT (Spring Security)
- Perfis de usuário: **Cliente**, **Funcionário**, **Administrador**
- **CRUD** completo de usuários, contas e transações (com validação @Valid)
- Operações financeiras:
    - Depósitos
    - Levantamentos (Levantamentos cardless incluídos)
    - Transferências internas
    - Histórico paginado e filtrado de transações
- Controle rigoroso de **saldo** (não permite saldo negativo)
- **Limpeza automática** de Levantamentos cardless expirados (scheduler)
- Validação de dados com `@Valid` e tratamento global de erros
- **Detecção de fraude**:
    - Varredura a cada 5 minutos
    - Bloqueio preventivo de contas com múltiplas transações de alto valor
    - Logs de alerta e preparado para notificações
- Documentação interactiva com **OpenAPI 3 / Swagger UI**
- Tratamento global de exceções (@ControllerAdvice)
- Testes unitários e de integração (**JUnit 5 + Mockito + Testcontainers**)
- Migrações de banco automatizadas com **Flyway**
- Suporte a **Docker** + **Docker Compose** para dev e prod
- Perfis Spring (`dev`, `prod`) com configurações seguras

## 🛠️ Tecnologias Utilizadas

- **Java 17+**
- **Spring Boot 3.11+**
- **Spring Data JPA + Hibernate**
- **PostgreSQL** (suporte a outros bancos via profile)
- **Flyway** (migrações)
- **Spring Security + OAuth2 Resource Server**
- **Lombok**
- **MapStruct**
- **JUnit 5 + Mockito**
- **Maven**
- **OpenAPI / Swagger**
- **Docker + Docker Compose**

## 🚀 Como Começar (Quick Start)

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker (opcional, mas recomendado)
- PostgreSQL (ou container via Docker)

### 1. Clone o repositório

```bash
git clone https://github.com/alfredobaptista/banking-application.git
cd banking-application
```

### 2. Configure o ambiente (dev)
- Crie/ edite o arquivo src/main/resources/application-dev.yml:

```json
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/banking_db
    username: postgres
    password: sua_senha_forte
  flyway:
    enabled: true
    locations: classpath:db/migration
```
- Ou use Docker Compose (recomendado):

```bash
docker-compose up -d
```

### 3. Execute a aplicação

```bash
# Opção 1 - Maven
mvn spring-boot:run -Dspring.profiles.active=dev

# Opção 2 - Jar
mvn clean package
java -jar target/banking-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 4. Acesse a documentação Swagger

```json
http://localhost:8080/swagger-ui.html
# ou
http://localhost:8080/swagger-ui/index.html

```

## 🔐 Endpoints Principais

| Método | Endpoint                        | Descrição                            | Autenticação         |
|--------|---------------------------------|--------------------------------------|----------------------|
| POST   | `/api/auth/register`            | Registar novo utilizador             | Não                  |
| POST   | `/api/auth/login`               | Login → retorna JWT                  | Não                  |
| POST   | `/api/auth/refresh`             | RefreshToken → retorna JWT           | Não                  |
| GET    | `/api/accounts?page=0&size=2`   | Lista contas do utilizador           | JWT (Funcionario+)   |
| POST   | `/api/transactions/deposit`     | Efectuar depósito                    | JWT (Funcionario)    |
| POST   | `/api/transactions/withdraw`    | Fazer levantamento                   | JWT (Cliente)        |
| POST   | `/api/transactions/transfer`    | Transferência entre contas           | JWT (Cliente)        |
| GET    | `/api/transactions`             | Histórico de transações              | JWT (Cliente)        |
| GET    | `/api/transactions/bi`             | Histórico de transações           | JWT (Funcioanrio)    |
| POST   | `/api/admin/users`              | Criar utilizador (admin, funcionario)| JWT (ADMIN)          |

*(Consulta a interface Swagger para a lista completa, exemplos de request/response e schemas detalhados)*

## 🐳 Docker

```bash
# Build da imagem
docker build -t banking-api:latest .

# Executar
docker-compose up --build

```

## 🧪 Testes

### Executar todos os testes (unitários + integração)
mvn test

### Apenas testes de integração (se tiveres profile configurado)
mvn test -P integration

## 📸 Capturas de Ecrã
![Swagger UI](/docs/images/swegger.PNG)

![Exemplo de Transação](/docs//images/transaction.PNG)

## 📄 Licença

MIT License — veja o ficheiro [LICENSE](LICENSE) para mais detalhes.

## 👤 Autor

**Alfredo Fernando Baptista**

- GitHub: [@alfredobaptista](https://github.com/alfredobaptista)
- LinkedIn: [linkedin.com/in/alfredobaptista](https://www.linkedin.com/in/alfredobaptista)
- Email: baptistaalfredo81@gmail.com

## 🙌 Contribuições
Contribuições são super bem-vindas!  
Podes abrir *issues* para sugestões/bugs ou *pull requests* com melhorias.
Gostaste? Dá uma ⭐ no repositório para apoiar o projeto! 🚀