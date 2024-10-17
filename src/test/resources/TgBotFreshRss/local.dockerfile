
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/amd64,linux/arm64 --tag armand0w/tgbotfreshrss:beta --push
# docker buildx build -f src/test/resources/TgBotFreshRss/local.dockerfile . --no-cache --platform linux/arm64,linux/arm/v6 --tag armand0w/tgbotfreshrss:beta --push
FROM armand0w/java:openjdk-17-jre-headless

WORKDIR /opt/app

COPY target/TgBotFreshRss.jar TgBotFreshRss.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotFreshRss.jar"]
