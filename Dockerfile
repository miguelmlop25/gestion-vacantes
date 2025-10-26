# Usar una imagen base de Maven con Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar los archivos de configuración de Maven primero (para aprovechar cache)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar las dependencias (esto se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación (sin ejecutar tests)
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de runtime más ligera
FROM eclipse-temurin:21-jre-alpine

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el JAR construido desde la etapa de build
COPY --from=build /app/target/gestion-vacantes-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto (Render lo asigna dinámicamente, pero lo definimos por si acaso)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
