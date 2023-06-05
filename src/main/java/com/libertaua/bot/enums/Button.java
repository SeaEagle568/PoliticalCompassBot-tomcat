package com.libertaua.bot.enums;

/**
 * Interface for buttons (Util and Answers)
 * Can return a button by text
 *
 * @author seaeagle
 */
public interface Button {
    static Button getButton(String text) {
        for (Util button : Util.values()){
            if (button.getText().equals(text)) return button;
        }
        for (Answer button : Answer.values()){
            if (button.getText().equals(text)) return button;
        }
        return Util.NULL;
    }
    String getButtonType();
    String getText();
}
