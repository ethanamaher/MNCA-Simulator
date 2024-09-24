package com.ethanamaher;

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VisualizerFrame extends JFrame {
    public VisualizerFrame() {
        super("MNCA Visualizer");
        VisualizerPanel panel = new VisualizerPanel();
        this.setContentPane(panel);
        pack();
    }


}
