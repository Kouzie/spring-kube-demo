FROM openjdk:11
ENV APP_FILE greeting-1.0-SNAPSHOT.jar
ENV APP_HOME /usr/apps
EXPOSE 8080
COPY build/libs/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]