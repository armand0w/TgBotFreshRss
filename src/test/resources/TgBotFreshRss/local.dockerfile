
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --platform linux/arm/v6 --tag armand0w/tgbotfreshrss --push --no-cache

FROM armand0w/java:openjdk-17-jre-headless
LABEL author="Armando Castillo"

ARG USERNAME=tgbot
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} -m ${USERNAME} \
#    && apt-get update \
#    && apt-get install -y sudo \
#    && echo "$USERNAME" ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/"$USERNAME" \
#    && chmod 0440 /etc/sudoers.d/"$USERNAME" \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

WORKDIR /opt/app
RUN chown -R ${USERNAME}:${USERNAME} /opt/app
USER ${USERNAME}

COPY target/TgBotFreshRss.jar TgBotFreshRss.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotFreshRss.jar"]
