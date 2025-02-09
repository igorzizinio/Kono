FROM openjdk:21

COPY . /app

WORKDIR /app

RUN microdnf install findutils
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon build shadowJar

COPY config.properties? /app/build/libs/

WORKDIR /app/build/libs

CMD ["java", "-jar", "KonoBot.jar"]
