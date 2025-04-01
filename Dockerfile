FROM maven:3-eclipse-temurin-17 AS builder
WORKDIR /opt/builder

RUN git clone -b develop https://github.com/armand0w/TgBotApi
RUN mvn -f TgBotApi/pom.xml install -DskipTests -e -B

COPY ./pom.xml ./TgBotFreshRss/pom.xml
COPY ./src ./TgBotFreshRss/src
RUN mvn -f ./TgBotFreshRss/pom.xml clean compile package -DskipTests


FROM azul/zulu-openjdk-alpine:17-jre-latest
LABEL maintainer="Armando Castillo" \
      version="0.0.2" \
      description="Telegram Notify FreshRss"

ENV APP_USER=tgbot \
    APP_UID=1000 \
    APP_HOME=/opt/app \
    JAVA_OPTS="-Xms64m -Xmx128m"

RUN addgroup -g $APP_UID $APP_USER && \
    adduser -D -u $APP_UID -G $APP_USER $APP_USER && \
    mkdir -p $APP_HOME && chown -R ${APP_USER}:${APP_USER} $APP_HOME && \
    mkdir -p /home/$APP_USER

WORKDIR $APP_HOME
USER $APP_USER

COPY --from=builder --chown=$APP_USER:$APP_USER /opt/builder/TgBotFreshRss/target/TgBotFreshRss.jar ./

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD ps aux | grep java | grep TgBotFreshRss.jar || exit 1

ENTRYPOINT ["sh", "-c"]
CMD ["java $JAVA_OPTS -jar TgBotFreshRss.jar"]

# docker build . -t armand0w/tgfreshbot
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/amd64,linux/arm64 --tag armand0w/tgbotfreshrss:beta --push
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/arm64,linux/arm/v6 --tag armand0w/tgbotfreshrss:beta --push
