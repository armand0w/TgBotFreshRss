FROM maven:3-eclipse-temurin-17 AS builder
WORKDIR /opt/builder

RUN git clone -b develop https://github.com/armand0w/TgBotApi
RUN mvn -f TgBotApi/pom.xml install -DskipTests -e -B

COPY ./pom.xml ./TgBotFreshRss/pom.xml
COPY ./src ./TgBotFreshRss/src
RUN mvn -f ./TgBotFreshRss/pom.xml clean compile package -DskipTests


FROM azul/zulu-openjdk-alpine:17-jre-latest
LABEL author="Armando Castillo"

ARG USERNAME=tgbot
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN addgroup -g ${USER_GID} ${USERNAME} \
    && adduser -D -u ${USER_UID} -G ${USERNAME} ${USERNAME}

WORKDIR /opt/app
RUN chown -R ${USERNAME}:${USERNAME} /opt/app

USER ${USERNAME}

COPY --from=builder /opt/builder/TgBotFreshRss/target/TgBotFreshRss.jar TgBotFreshRss.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotFreshRss.jar"]

# docker build . -t armand0w/tgfreshbot
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/amd64,linux/arm64 --tag armand0w/tgbotfreshrss:beta --push
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/arm64,linux/arm/v6 --tag armand0w/tgbotfreshrss:beta --push
