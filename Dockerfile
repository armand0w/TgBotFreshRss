
FROM maven:3-eclipse-temurin-17 AS builder
WORKDIR /opt/builder

RUN git clone -b develop https://github.com/armand0w/TgBotApi
RUN mvn -f TgBotApi/pom.xml install -DskipTests -e -B

COPY ./pom.xml ./TgBotFreshRss/pom.xml
COPY ./src ./TgBotFreshRss/src
RUN mvn -f ./TgBotFreshRss/pom.xml clean compile package -DskipTests


FROM armand0w/java:openjdk-17-jre-headless
LABEL author="Armando Castillo"

ARG USERNAME=tgbot
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} -m ${USERNAME}
#    && apt-get update \
#    && apt-get install -y sudo \
#    && echo "$USERNAME" ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/"$USERNAME" \
#    && chmod 0440 /etc/sudoers.d/"$USERNAME" \
#    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

USER ${USERNAME}

WORKDIR /opt/app
RUN chown -R ${USERNAME}:${USERNAME} /opt/app

USER ${USERNAME}

COPY --from=builder /opt/builder/TgBotFreshRss/target/TgBotFreshRss.jar TgBotFreshRss.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotFreshRss.jar"]

# docker build . -t armand0w/tgfreshbot
