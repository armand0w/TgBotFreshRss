package com.armandow.freshrss.utils;

import com.armandow.freshrss.FreshBot;
import com.armandow.freshrss.model.Bot;
import com.armandow.freshrss.model.Config;
import com.armandow.freshrss.model.FreshRSS;
import com.armandow.freshrss.model.Sentry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Calendar;
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

    public static int getInitialStart() {
        var cs = Calendar.getInstance().get(Calendar.SECOND);
        var rf = config.getBot().refresh();
        log.trace("minute {}:{}", Calendar.getInstance().get(Calendar.MINUTE), cs);

        if ( rf < 60 ) {
            return ( 60 - cs );
        }

        rf = ( rf / 60 );
        log.debug("every '{}' minutes", rf);

        if ( rf < 60 ) {
            var cm = Calendar.getInstance().get(Calendar.MINUTE);
            var w = ((( (((cm / rf) + 1 )) * rf ) - cm) * 60) - cs;
            log.debug("w: '{}'s", w);
            return w;
        }

        return 0;
    }
}
