FROM maven:3.6.3-jdk-11

COPY ./pom.xml        ./pom.xml
COPY ./core/pom.xml   ./core/pom.xml
COPY ./webapp/pom.xml ./webapp/pom.xml
RUN mvn dependency:go-offline -B

COPY ./core ./core
COPY ./webapp ./webapp
RUN mvn -fn -Dmaven.test.failure.ignore=true clean install

FROM openjdk:11.0.10-jdk
LABEL maintainer="stefan.t42@gmx.de"
LABEL description="The Contagio Server"

COPY --from=0 webapp/target/contagio-server.jar /opt/contagio-server/contagio-server.jar
COPY ./docker-entrypoint.sh /opt/contagio-server/docker-entrypoint.sh
RUN mkdir /opt/contagio-server/logs && chmod +x /opt/contagio-server/docker-entrypoint.sh

WORKDIR /opt/contagio-server
EXPOSE 13013
ENV ACTIVE_PROFILE stt
ENTRYPOINT ["/opt/contagio-server/docker-entrypoint.sh"]
