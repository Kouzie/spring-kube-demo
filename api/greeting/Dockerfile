FROM openjdk:11
# gradle
ARG JAR_FILE=build/libs/*.jar
# maven
# ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar", "--sprig.active.profile=${SPRING_PROFILES_ACTIVE}"]