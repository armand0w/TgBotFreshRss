package com.armandow.freshrss.model;

import java.util.Set;

public record FreshRSS(String urlBase, String auth, Set<String> topics) { }
