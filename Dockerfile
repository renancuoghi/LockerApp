FROM maven:3.6.0-jdk-11-slim
RUN rm -Rf /home/app/target
COPY src /home/app/src
COPY pom.xml /home/app
WORKDIR /home/app
RUN mvn clean install
RUN mvn test
