package com.ethanamaher.helpers;

public class Cell {
    private int state;
    private int nextState;

    public Cell(int state) {
        this.state = state;
    }

    public boolean isAlive() {
        return this.state == 1;
    }

    public int getState() {
        return this.state;
    }

    public void setAlive() {
        this.state = 1;
    }

    public void setDead() {
        this.state = 0;
    }

    public void setNextState(int nextState) {
        this.nextState = nextState;
    }

    public void step() {
        this.state = this.nextState;
    }
}
