package com.armandow.freshrss.task;

import com.armandow.freshrss.http.RestClient;
import com.armandow.telegrambotapi.enums.ParseMode;
import com.armandow.telegrambotapi.exceptions.TelegramApiValidationException;
import com.armandow.telegrambotapi.methods.SendMessage;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import static com.armandow.freshrss.utils.RssUtils.config;
import static com.armandow.telegrambotapi.utils.TelegramApiUtils.scapeTelegramString;
import static com.armandow.telegrambotapi.utils.TelegramApiUtils.scapeUrl;

@Slf4j
public class RssRead implements Runnable {

    @Override
    public void run() {
        log.debug("-- RssRead run --");

        try {
            for ( var topic: config.getFreshRSS().topics() ) {
                var response = new RestClient().get(topic).getBody();
                var items = response.optJSONArray("items");

                if ( !items.isEmpty() ) {
                    for ( var it: items ) {
                        sendNotify((JSONObject) it);
                    }
                }
            }

        } catch (Exception e) {
            log.error("RssRead", e);
            Sentry.captureException(e);
        } finally {
            log.debug("RssRead run finally");
        }
    }

    protected void sendNotify(JSONObject item) {
        log.trace(item.toString(2));
        var message = new SendMessage();
        var resp = new JSONObject();

        try {
            message.setText(buildMessage(item));
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

    protected String buildMessage(JSONObject item) {
        // Channel title
        return "*" + scapeTelegramString(item.optQuery("/origin/title").toString()) + "*\n\n" +

                // title
                scapeTelegramString(item.optQuery("/title").toString()) + "\n\n" +

                // link
                "[Leer mas](" + scapeUrl(item.optQuery("/canonical/0/href").toString()) + ")" + "\n";
    }
}
