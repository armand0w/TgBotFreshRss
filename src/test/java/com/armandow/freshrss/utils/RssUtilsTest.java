package com.armandow.freshrss.utils;

import com.armandow.freshrss.task.RssRead;
import com.armandow.telegrambotapi.TelegramBot;
import com.armandow.telegrambotapi.utils.TelegramApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class RssUtilsTest {

    @BeforeAll
    static void setUp() throws Exception {
        RssUtils.loadConfig();
    }

    @Test
    void testGetInitialStart() {
        log.info("refresh value: {}", RssUtils.config.getBot().refresh());
        log.info("current HOUR: {}", Calendar.getInstance().get(Calendar.HOUR));
        log.info("current MINUTE: {}", Calendar.getInstance().get(Calendar.MINUTE));
        log.info("current SECOND: {}\n", Calendar.getInstance().get(Calendar.SECOND));

        assertNotEquals(0, TelegramApiUtils.getInitialStart(RssUtils.config.getBot().refresh()));
        log.info("waiting for '{}' seconds", TelegramApiUtils.getInitialStart(RssUtils.config.getBot().refresh()));
    }

    @Test
    void testRssRead() {
        var bot = new TelegramBot(RssUtils.config.getBot().token());
        assertNotNull(bot);

        var runnable = new RssRead();
        assertNotNull(runnable);

        runnable.run();
    }
}
