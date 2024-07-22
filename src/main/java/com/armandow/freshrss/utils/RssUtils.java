package com.armandow.freshrss.utils;

import com.armandow.freshrss.FreshBot;
import com.armandow.freshrss.model.Bot;
import com.armandow.freshrss.model.Config;
import com.armandow.freshrss.model.FreshRSS;
import com.armandow.freshrss.model.Sentry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class RssUtils {
    private RssUtils() {
        // Prevent instance
    }

    public static final Config config = new Config();

    public static String getRelease() {
        return FreshBot.class.getPackage().getImplementationVersion();
    }

    public static void loadConfig() throws Exception {
        config.setSentry(
                new Sentry(
                        System.getenv("SENTRY_DSN"),
                        InetAddress.getLocalHost().getHostName()
                )
        );

        config.setBot(
                new Bot(
                        System.getenv("BOT_TOKEN"),
                        Integer.parseInt(System.getenv("BOT_REFRESH")),
                        Long.parseLong(System.getenv("USER_ID")),
                        Long.parseLong(System.getenv("CHANNEL_ID"))
                )
        );

        var list = System.getenv("TOPICS").split(",");
        var topics = new HashSet<>(Arrays.asList(list));

        config.setFreshRSS(
                new FreshRSS(
                        System.getenv("URL_BASE"),
                        System.getenv("FRESH_AUTH"),
                        topics
                )
        );

        log.trace(config.toString());
    }

    public static String formatMexDate(Long published) {
        var format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        var date = LocalDateTime.ofEpochSecond(published, 0, ZoneOffset.of("-06:00"));
        return date.format(format);
    }
}
