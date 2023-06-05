package com.libertaua.bot.enums;

/**
 * BotState Phases enum
 * It has two spare phases (because i dont want to drop DB if i ever need more phases)
 */
public enum Phase {
    PRESTART,
    GREETING,
    SOCIAL,
    TESTING,
    RESULTS,
    UNKNOWN,
    SPARE
}
