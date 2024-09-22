package com.ethanamaher;

import javax.swing.JFrame;
public class VisualizerMain {
    public static void main(String[] args) {
        VisualizerFrame frame = new VisualizerFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(false);

        VisualizerPanel panel = new VisualizerPanel();
        frame.add(panel);
        frame.pack();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.startGameThread();
    }
}
