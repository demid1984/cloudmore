FROM openjdk:11-jdk-slim

ADD app.jar /home/
RUN mkdir /home/config

WORKDIR /home
EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "exec java $JAVA_OPTS -Djava.net.preferIPv4Stack=true -Djava.security.egd=file:/dev/./urandom -Duser.timezone=UTC -Duser.timezone=UTC -Dspring.config.location=classpath:/,file:/home/config/ -Dspring.profiles.active=$PROFILE_OPTS -jar app.jar"]
