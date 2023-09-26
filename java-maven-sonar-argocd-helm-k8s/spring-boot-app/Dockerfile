FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir /opt/app 

COPY target/spring-boot-web.jar /opt/app/app.jar

CMD [ "java","-jar","/opt/app/app.jar" ]


