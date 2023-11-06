#FROM registry.access.redhat.com/ubi8/openjdk-17
FROM openjdk:17-alpine
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

ARG QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://172.28.98.233:5432/jbossts
ARG QUARKUS_DATASOURCE_USERNAME=admin
ARG QUARKUS_DATASOURCE_PASSWORD=admin

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENV QUARKUS_DATASOURCE_JDBC_URL=${QUARKUS_DATASOURCE_JDBC_URL}
ENV QUARKUS_DATASOURCE_USERNAME=${QUARKUS_DATASOURCE_USERNAME}
ENV QUARKUS_DATASOURCE_PASSWORD=${QUARKUS_DATASOURCE_PASSWORD}

ENTRYPOINT java -jar ./deployments/quarkus-run.jar
#CMD ["java", "-Dquarkus.datasource.jdbc.url=$QUARKUS_DATASOURCE_JDBC_URL","-Dquarkus.datasource.username=$QUARKUS_DATASOURCE_USERNAME","-Dquarkus.datasource.password=$QUARKUS_DATASOURCE_PASSWORD", "-jar", "./deployments/quarkus-run.jar"]
#ENV AB_JOLOKIA_OFF=""
#ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
#ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"