FROM openjdk:17.0.1-oracle

COPY target/builtin-support-service*.jar builtin-support-service.jar
CMD java ${JAVA_OPTS} -jar builtin-support-service.jar