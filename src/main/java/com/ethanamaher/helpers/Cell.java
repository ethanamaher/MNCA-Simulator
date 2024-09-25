package com.ethanamaher.helpers;

public class Cell {
    private boolean state;
    private boolean nextState;

    public Cell(boolean state) {
        this.state = state;
        this.nextState = state;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void setNextState(boolean nextState) {
        this.nextState = nextState;
    }

    public void step() {
        this.state = this.nextState;
    }
}
