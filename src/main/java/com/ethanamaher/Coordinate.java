package com.ethanamaher;

public class Coordinate {
    private int x, y;

    Coordinate() {
        x = Integer.MIN_VALUE;
        y = Integer.MIN_VALUE;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
