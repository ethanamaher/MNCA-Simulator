package com.ethanamaher;

import javax.swing.*;
import java.awt.*;

public class VisualizerPanel extends JPanel {
    private final MNCA MNCA;


    public VisualizerPanel() {
        setLayout(new BorderLayout(2, 2));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        MNCA = new MNCA();
        setPreferredSize(MNCA.getImageSize());
        setOpaque(true);
        setBackground(Color.BLACK);

        Timer timer = new Timer(30, ((e) -> {
            repaint();
            MNCA.update();
        }));
        timer.start();

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        MNCA.draw(g2);
        g2.dispose();
    }
}
