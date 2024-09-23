package com.ethanamaher;

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VisualizerFrame extends JFrame {
    public VisualizerFrame() {
        super("MNCA Visualizer");
        VisualizerPanel panel = new VisualizerPanel();
        final MNCA display = panel.getDisplay();
        this.setContentPane(panel);
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                display.closing();
            }
        });

        panel.startGameThread();
    }


}
