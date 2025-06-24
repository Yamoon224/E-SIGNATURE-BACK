# Étape 1 : Utilise une image Java officielle
FROM eclipse-temurin:17-jdk-alpine

# Crée un répertoire de travail dans le conteneur
WORKDIR /app

# Copie le fichier JAR dans l'image
COPY target/*.jar app.jar

# Expose le port sur lequel ton app écoute (changer si besoin)
EXPOSE 8080

# Commande pour lancer l’application
ENTRYPOINT ["java", "-jar", "app.jar"]
