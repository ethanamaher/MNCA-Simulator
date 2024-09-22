package com.ethanamaher;

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VisualizerFrame extends JFrame {
    public VisualizerFrame() {
       super("Math Visualizer");
       VisualizerPanel panel = new VisualizerPanel();
       final MNCA display = panel.getDisplay();
        setContentPane(panel);
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                display.closing();
            }
        });
    }


}
