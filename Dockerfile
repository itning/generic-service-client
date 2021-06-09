FROM openjdk:8u292-jre

MAINTAINER itning itning@itning.top

ADD generic-service-run/target/generic-service-client-*RELEASE.jar /home/generic-service-client.jar
# 端口暴露
EXPOSE 8868

ENTRYPOINT ["java","-jar","/home/generic-service-client.jar"]