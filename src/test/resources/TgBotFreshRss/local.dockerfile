
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/arm/v6 --tag armand0w/tgbotfreshrss --push
FROM armand0w/java:openjdk-17-jre-headless

WORKDIR /opt/app

COPY target/TgBotFreshRss.jar TgBotFreshRss.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotFreshRss.jar"]
