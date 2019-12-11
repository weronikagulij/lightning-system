package com.example.sw_app;

public class ButtonState {
    private int r;
    private int g;
    private int b;
    private int buttonNumber;

    public ButtonState(int buttonNumber) {
        r = 0;
        g = 0;
        b = 0;
        this.buttonNumber = buttonNumber;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getButtonNumber() {
        return buttonNumber;
    }

    public void setR(int r) {
        this.r = r;
    }

    public void setG(int g) {
        this.g = g;
    }

    public void setB(int b) {
        this.b = b;
    }
}
