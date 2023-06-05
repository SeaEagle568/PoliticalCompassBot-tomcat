package com.libertaua.bot.enums;

import lombok.Getter;

/**
 * This is answer buttons, that is used during main quiz
 */
public enum Answer implements Button{
    STRONG_AGREE  ("Однозначно так", 2),
    WEAK_AGREE("Скоріше так",1 ),
    DONT_KNOW ("Важко відповісти", 0),
    WEAK_DISAGREE ("Скоріше ні",-1),
    STRONG_DISAGREE ("Однозначно ні",-2)
    ;

    @Getter
    private final String text;
    private final int value;
    private final int invertedValue;

    @Override
    public String getButtonType() {
        return "ANSWER";
    }

    public int getValue(Boolean inverted) {
        if (inverted) return this.invertedValue;
        return this.value;
    }

    Answer(String text, int value) {
        this.text = text;
        this.value = value;
        this.invertedValue = -value;
    }

}
