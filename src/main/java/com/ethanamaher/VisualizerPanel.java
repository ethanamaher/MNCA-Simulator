package com.ethanamaher;

import javax.swing.*;
import java.awt.*;

public class VisualizerPanel extends JPanel {
    private final MNCA MNCA;


    public VisualizerPanel() {
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        MNCA = new MNCA();
        setPreferredSize(MNCA.getImageSize());
        setOpaque(true);
        setBackground(Color.BLACK);

        Timer timer = new Timer(25, ((e) -> {
            //thread for calculations on bufferedImage start before painting current state
            Thread updateThread = (new Thread(MNCA::update));
            updateThread.start();

            repaint();

            // wait for calculations to finish
            try {
                updateThread.join();
            } catch (InterruptedException ignored) {

            }

            // wait for calculations to complete before continuing
            try {
                updateThread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

//            MNCA.update();
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
