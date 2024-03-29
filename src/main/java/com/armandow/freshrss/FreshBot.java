package com.armandow.freshrss;

import com.armandow.freshrss.task.RssRead;
import com.armandow.freshrss.utils.RssUtils;
import com.armandow.telegrambotapi.TelegramBot;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class FreshBot {
    public static void main( String[] args ) {
        log.info(">> Initializing FreshBot <<");

        try { // load config
            RssUtils.loadConfig();
        } catch (Exception e) {
            log.error("load config", e);
            System.exit(-1);
        }

        try { // Sentry config
            Sentry.init(options -> {
                options.setDsn(RssUtils.config.getSentry().dsn());
                options.setEnvironment(RssUtils.config.getSentry().environment());
                options.setRelease(RssUtils.getRelease());
            });
            Sentry.captureMessage("Init FreshBot");
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        try {
            var scheduler = Executors.newSingleThreadScheduledExecutor();
            var eService = Executors.newVirtualThreadPerTaskExecutor();
            scheduler.scheduleWithFixedDelay(() -> eService.execute(new RssRead()),
                    RssUtils.getInitialStart(),
                    RssUtils.config.getBot().refresh(),
                    SECONDS);
        } catch (Exception e) {
            log.error("Start scheduler RSSBot", e);
            Sentry.captureException(e);
            System.exit(-1);
        }

        var bot = new TelegramBot(RssUtils.config.getBot().token());
        // bot.registerCommand(new AddUrlCommand());
        // bot.run();
    }
}
