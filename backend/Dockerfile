# ==========================================================================
#  Dockerfile - Backend Spring Boot (Gestion de Biblioteca)
#  Usado por Railway, Fly.io, Koyeb y cualquier hosting con contenedores.
#  Construir:  docker build -t gestion-biblioteca .
#  Ejecutar:   docker run -p 8080:8080 gestion-biblioteca
# ==========================================================================

# ---- Etapa 1: compilar el JAR con Maven -----------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Descargar dependencias primero (aprovecha la cache de Docker)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el codigo y compilar
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Etapa 2: imagen final minima -----------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# El jar generado por la etapa de compilacion
COPY --from=build /build/target/gestion-biblioteca-*.jar app.jar

# Puerto del contenedor (el hosting lo asigna via la variable PORT)
EXPOSE 8080

# Variables de entorno que se configuran en el hosting:
#   DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET
ENTRYPOINT ["java", "-jar", "app.jar"]
