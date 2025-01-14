FROM eclipse-temurin:23.0.1_11-jdk
COPY target/libs/*.jar /usr/app/libs/
COPY target/pex-message-broker-1.0-SNAPSHOT.jar /usr/app/pex-message-broker-1.0-SNAPSHOT.jar
EXPOSE 8000
ENTRYPOINT ["java","-jar","/usr/app/pex-message-broker-1.0-SNAPSHOT.jar"]

#FROM alpine:3.4
#
#RUN apk update
#RUN apk add vim
#RUN apk add curl

# docker tag message-broker wasanka2000/message-broker:latest
# docker push wasanka2000/message-broker:latest