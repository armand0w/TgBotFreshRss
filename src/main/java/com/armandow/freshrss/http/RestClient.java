package com.armandow.freshrss.http;

import com.armandow.freshrss.utils.RssUtils;
import com.armandow.telegrambotapi.exceptions.TooManyRequestExceptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

import static com.armandow.freshrss.utils.RssUtils.config;
import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
public class RestClient {
    private final HttpClient httpClient;

    @Getter
    private int statusCode;
    @Getter private JSONObject body;

    public RestClient() {
        this.httpClient = createHttpClient();
    }

    private HttpClient createHttpClient() {
        HttpClient client;

        try {
            var context = SSLContext.getInstance("TLSv1.3");
            context.init(null, null, null);

            client = HttpClient.newBuilder()
                    .sslContext(context)
                    .connectTimeout(Duration.ofSeconds(75))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }

        return client;
    }

    public RestClient get(String topic) throws Exception {
        // https://www.epochconverter.com/
        var now = Instant.now().minus(RssUtils.config.getBot().refresh(), SECONDS).getEpochSecond();
        var uri = URI.create(config.getFreshRSS().urlBase() + "reader/api/0/stream/contents/user/-/label/" + topic + "?ot=" + now);
        log.debug("URL: {}", uri);
        var request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(25))
                .GET()
                .uri(uri)
                .header("User-Agent", "TgFreshBot v" + RssUtils.getRelease())
                .header("Authorization", "GoogleLogin auth=" + config.getFreshRSS().auth())
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if ( this.statusCode == 429 ) {
            throw new TooManyRequestExceptions("Too Many Requests", this.body);
        }

        var headers = response.headers();
        headers.map().forEach((k, v) -> log.trace("{}:{}", k, v));
        log.trace("--------------------------------------------------------------------------------------------------");

        this.statusCode = response.statusCode();

        try {
            this.body = new JSONObject(response.body());

            if ( this.statusCode != 200 ) {
                log.warn(this.body.toString(2));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return this;
    }
}
