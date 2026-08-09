# Multi-stage build: to 1o stadio XTIZEI tin efarmogi, to 2o mono tin TREXEI.
#
# Giati dyo stadia: i eikona tou Maven einai ~800MB (exei olokliro JDK kai ergaleia).
# I teliki eikona xreiazetai mono ena JRE kai to .jar - ~250MB. Etsi den koubalame
# ergaleia metaglottisis se ena perivallon pou apla trexei tin efarmogi.

# ---------- 1o stadio: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Prota MONO to pom.xml: an den allaxoun oi vivliothikes, to Docker ksanaxrisimopoiei
# to cache kai den ta ksanakatevazei se kathe build.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src ./src

# -DskipTests: ta tests DEN trexoun edo. Trexoun ksexorista me ".\mvnw.cmd test".
# Logos: to build tis eikonas prepei na einai grigoro kai proplepsimo· o elegxos
# poiotitas einai diaforetiki douleia apo ti syskevasia.
RUN mvn -B clean package -DskipTests

# ---------- 2o stadio: runtime ----------
FROM eclipse-temurin:21-jre

# fontconfig: to Apache POI metraei to platos ton stilon me vasi tis grammatoseires.
# Se ena "gymno" container den yparxoun katholou, kai i eksagogi se Excel tha evgaine
# me stathero platos. curl: to xreiazetai to healthcheck parakato.
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# To /api/health einai dimosio (den thelei login), opote kanei gia healthcheck.
HEALTHCHECK --interval=10s --timeout=5s --retries=20 --start-period=30s \
    CMD curl -fsS http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
