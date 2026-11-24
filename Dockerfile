
# Etapa 1: compilar Foodly IAM
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar Maven Wrapper y pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

# Copiar el código fuente
COPY src/ src/

# Compilar y generar el JAR ejecutable
RUN ./mvnw clean package -DskipTests -B \
    -Dspring-boot.repackage.mainClass=com.foodly.iam.IamServiceApplication

# Etapa 2: ejecutar Foodly IAM
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
