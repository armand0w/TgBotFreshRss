
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/amd64,linux/arm64 --tag armand0w/tgbotfreshrss:beta --push
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/arm64 --tag armand0w/tgbotfreshrss:beta --push
FROM azul/zulu-openjdk-alpine:21-jre-headless-latest

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

COPY target/TgBotFreshRss.jar TgBotFreshRss.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotFreshRss.jar"]
