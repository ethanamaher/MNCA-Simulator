package com.ethanamaher.neighborhood;

public class NeighborhoodIntervalNode {
    private NeighborhoodIntervalNode left;
    private NeighborhoodIntervalNode right;

    double intervalCenter;
    boolean nextStateAlive;

    NeighborhoodIntervalNode() {

    }

    NeighborhoodIntervalNode(double intervalMin, double intervalMax, boolean nextStateAlive) {
        this.intervalCenter = (intervalMax + intervalMin) / 2;
        this.nextStateAlive = nextStateAlive;
    }

    NeighborhoodIntervalNode getLeftChild() {
        return this.left;
    }

    NeighborhoodIntervalNode getRightChild() {
        return this.right;
    }

    NeighborhoodIntervalNode setLeftChild(NeighborhoodIntervalNode left) {
        this.left = left;
        return this.left;
    }

    NeighborhoodIntervalNode setRightChild(NeighborhoodIntervalNode right) {
        this.right = right;
        return this.right;
    }

}
