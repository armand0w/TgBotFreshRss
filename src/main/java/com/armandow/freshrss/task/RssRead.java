package com.armandow.freshrss.task;

import com.armandow.freshrss.http.RestClient;
import com.armandow.freshrss.utils.RssUtils;
import com.armandow.telegrambotapi.enums.ParseMode;
import com.armandow.telegrambotapi.exceptions.TelegramApiValidationException;
import com.armandow.telegrambotapi.methods.SendMessage;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.armandow.freshrss.utils.RssUtils.config;
import static com.armandow.telegrambotapi.utils.TelegramApiUtils.scapeTelegramString;

@Slf4j
public class RssRead implements Runnable {
    private Map<String, String> messages;

    @Override
    public void run() {
        log.debug("-- RssRead run --");

        try {
            messages = new HashMap<>();
            for ( var topic: config.getFreshRSS().topics() ) {
                var response = new RestClient().get(topic).getBody();
                var items = response.optJSONArray("items");

                if ( !items.isEmpty() ) {
                    log.trace("Found {} items", items.length());
                    separateFeed(items);
                }
            }

            messages.forEach((k, m) -> sendNotify(m));
        } catch (Exception e) {
            log.error("RssRead", e);
            Sentry.captureException(e);
        } finally {
            log.debug("RssRead run finally");
        }
    }

    protected void separateFeed(JSONArray array) {
        for ( var item: array ) {
            var jItem = (JSONObject) item;
            var streamId = jItem.optQuery("/origin/streamId").toString();
            if ( !messages.containsKey(streamId) ) {
                var m = "*" + scapeTelegramString(jItem.optQuery("/origin/title").toString()) +
                        "*\n\n" + scapeTelegramString("```") + "\n" + buildMsg(jItem);
                messages.put(streamId, m);
            } else {
                var mm = messages.get(streamId);
                mm += buildMsg(jItem);
                messages.replace(streamId, mm);
            }
        }
    }

    private String buildMsg(JSONObject item) {
        var author = scapeTelegramString(item.optQuery("/author").toString());
        var title = scapeTelegramString(item.optQuery("/title").toString());
        var published = RssUtils.formatMexDate(item.getLong("published"));

        if ( !title.startsWith(author) ) {
            return author + " [" + published +"]:\t" + title + "\n\n";
        } else return "[" + published +"]:\t" + title + "\n\n";
    }

    protected void sendNotify(String feeds) {
        feeds += scapeTelegramString("```");
        log.debug(feeds);
        var message = new SendMessage();
        var resp = new JSONObject();

        try {
            message.setText(feeds);
            message.setParseMode(ParseMode.MARKDOWNV2);
            message.setChatId(config.getBot().channelIdGit());
            log.trace("Message: {}", message.getText());

            resp = message.send();
            if ( !resp.optBoolean("ok") ) {
                Sentry.captureMessage("Payload", scope -> {
                    scope.setLevel(SentryLevel.WARNING);
                    scope.setExtra("text", message.getText());
                });
                throw new TelegramApiValidationException(resp.getString("description"));
            }
            log.trace("______________________________________________________________________________________________");
        } catch (Exception e) {
            log.error("RSS Reader", e);
            log.error(resp.toString(2));
            log.error(message.getText());
            Sentry.captureException(e);
        }
    }
}
