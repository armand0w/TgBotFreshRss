package com.armandow.freshrss.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
public class Config {
    private Bot bot;
    private Sentry sentry;
    private FreshRSS freshRSS;
}
