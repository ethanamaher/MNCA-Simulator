package com.ethanamaher.helpers;

public class Interval {
    private double min, max;
    private int neighborhood;
    private boolean nextState;

    public Interval() {}

    public Interval(int neighborhood, double min, double max, boolean nextState) {
        this.neighborhood = neighborhood;
        this.min = min;
        this.max = max;
        this.nextState = nextState;
    }

    /**
     * Returns if a number is in this interval.
     * Inclusive bounds
     * @param number a number
     * @return boolean whether the number is in the interval
     */
    public boolean contains(double number) {
        return number >= this.min && number <= this.max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getNeighborhood() {
        return neighborhood;
    }

    public boolean getNextState() {
        return nextState;
    }

    @Override
    public String toString() {
        String output = "";
        output += "Neighborhood: " + this.neighborhood + "\n" +
                "Min: " + this.min + "\n" +
                "Max: " + this.max + "\n" +
                "Next State: " + this.nextState + "\n";
        return output;
    }
}
