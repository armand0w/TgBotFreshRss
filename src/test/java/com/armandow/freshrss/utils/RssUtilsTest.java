package com.armandow.freshrss.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RssUtilsTest {

    @BeforeEach
    void setUp() throws Exception {
        RssUtils.loadConfig();
    }

    @Test
    void testGetInitialStart() {
        log.info("refresh value: {}", RssUtils.config.getBot().refresh());
        log.info("current HOUR: {}", Calendar.getInstance().get(Calendar.HOUR));
        log.info("current MINUTE: {}", Calendar.getInstance().get(Calendar.MINUTE));
        log.info("current SECOND: {}\n", Calendar.getInstance().get(Calendar.SECOND));

        log.info("waiting for '{}' seconds", RssUtils.getInitialStart());
    }
}
