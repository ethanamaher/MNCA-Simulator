package com.ethanamaher;

public class Interval {
    private double min, max;
    private int neighborhood;
    private int nextState;

    public Interval() {}

    public Interval(int neighborhood, double min, double max, int nextState) {
        this.neighborhood = neighborhood;
        this.min = min;
        this.max = max;
        this.nextState = nextState;
    }

    /**
     * Returns if a number is in this interval.
     * Inclusive bounds
     * @param number
     * @return
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

    public int getNextState() {
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
