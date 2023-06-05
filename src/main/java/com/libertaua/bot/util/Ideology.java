package com.libertaua.bot.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Ideology {
    public String name;
    public String link;
    public Pair<Double, Double> coords;

    public Ideology(String name, Pair<Double, Double> coords) {
        this.name = name;
        this.coords = coords;
    }
}
