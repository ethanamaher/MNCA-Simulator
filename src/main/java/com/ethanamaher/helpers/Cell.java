package com.ethanamaher.helpers;

public class Cell {
    private int state;
    private int nextState;

    public Cell(int state) {
        this.state = state;
        this.nextState = state;
    }

    public boolean isAlive() {
        return this.state == 1;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setNextState(int nextState) {
        this.nextState = nextState;
    }

    public void step() {
        this.state = this.nextState;
    }
}
