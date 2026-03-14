# Etapa 1: build da aplicação
FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app

# Copia os arquivos de pom e código
COPY pom.xml .
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Etapa 2: container final mais leve
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia o JAR gerado pelo build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta do Spring Boot
EXPOSE 8080

# Define variáveis de ambiente padrão (podem ser sobrescritas pelo docker-compose)
ENV DB_URL=jdbc:postgresql://db:5432/bank
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=vidapaz0720
ENV SERVER_PORT=8080
ENV JWT_PRIVATE_KEY=/keys/app.key.pem
ENV JWT_PUBLIC_KEY=/keys/app.pub
ENV SPRING_PROFILES_ACTIVE=prod

# Comando para rodar a aplicação
ENTRYPOINT ["java","-jar","app.jar"]