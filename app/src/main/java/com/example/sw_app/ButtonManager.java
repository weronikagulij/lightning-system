package com.example.sw_app;

import java.util.ArrayList;
import java.util.List;

public class ButtonManager {
    private List<ButtonState> buttonStateList;
    private ButtonState activeButton;

    public ButtonManager() {
        buttonStateList = new ArrayList<>();
    }

    public void addButton(ButtonState buttonState) {
        buttonStateList.add(buttonState);
    }

    public void setActiveButton(ButtonState activeButton) {
        this.activeButton = activeButton;
    }

    public ButtonState getActiveButton() {
        return activeButton;
    }

    public ButtonState getButtonByNumber(int i) {
        return buttonStateList.get(i);
    }

    public void setActiveButtonByNumber(int i) {
        this.activeButton = buttonStateList.get(i);
    }
}
