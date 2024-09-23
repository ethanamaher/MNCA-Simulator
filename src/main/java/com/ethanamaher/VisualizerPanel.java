package com.ethanamaher;

import javax.swing.*;
import java.awt.*;

public class VisualizerPanel extends JPanel implements Runnable {
    private final MNCA MNCA;
    private final int FPS = 30;

    private Thread gameThread;

    public VisualizerPanel() {
        setLayout(new BorderLayout(2, 2));
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        MNCA = new MNCA();
        setPreferredSize(MNCA.getImageSize());
        setOpaque(true);
        setBackground(Color.BLACK);

    }

    public MNCA getDisplay() {
        return MNCA;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = (double) 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 0) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        MNCA.draw(g2);
        g2.dispose();
    }

    public void update() {
        MNCA.update();
    }
}
