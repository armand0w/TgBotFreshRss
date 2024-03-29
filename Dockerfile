
FROM maven:3-eclipse-temurin-21 AS builder
WORKDIR /opt/builder

RUN git clone -b develop https://github.com/armand0w/TgBotApi
RUN mvn -f TgBotApi/pom.xml install -DskipTests -e -B

COPY ./pom.xml ./TgBotFreshRss/pom.xml
COPY ./src ./TgBotFreshRss/src
RUN mvn -f ./TgBotFreshRss/pom.xml clean compile package -DskipTests


FROM azul/zulu-openjdk-alpine:21-jre-headless-latest
LABEL author="Armando Castillo"

RUN apk add --no-cache sudo
ARG USERNAME=tgbot
RUN adduser --gecos "$USERNAME" \
    --disabled-password \
    --shell /bin/sh \
    --uid 1000 \
    ${USERNAME} && \
    echo "$USERNAME:1234" | chpasswd && \
    echo "$USERNAME ALL=(ALL) ALL" > /etc/sudoers.d/$USERNAME && chmod 0440 /etc/sudoers.d/$USERNAME \
    && addgroup ${USERNAME} wheel \
    && addgroup ${USERNAME} ${USERNAME}

WORKDIR /opt/app
RUN chown -R ${USERNAME}:${USERNAME} /opt/app

USER ${USERNAME}

COPY --from=builder /opt/builder/TgBotFreshRss/target/TgBotFreshRss.jar TgBotFreshRss.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotFreshRss.jar"]

# docker build . -t armand0w/tgfreshbot
