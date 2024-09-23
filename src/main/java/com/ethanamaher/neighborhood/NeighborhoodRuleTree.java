package com.ethanamaher.neighborhood;

public class NeighborhoodRuleTree {
    private NeighborhoodIntervalNode root;
    private int nodeCount;

    public NeighborhoodRuleTree() {
        root = null;
        nodeCount = 0;
    }

    public NeighborhoodRuleTree(NeighborhoodIntervalNode root) {
        this.root = root;
        nodeCount = 1;
    }

    public NeighborhoodIntervalNode addNode(NeighborhoodIntervalNode node) {
        // if node.intervalMin > root.intervalMax
        // go to right
        // else if node.intervalMax < root.intervalMin
        // go to left
        // else
        // overlapping interval

        return node;
    }

    public NeighborhoodIntervalNode getRoot() {
        return this.root;
    }

    public boolean isEmpty() {
        return nodeCount == 0;
    }

}
