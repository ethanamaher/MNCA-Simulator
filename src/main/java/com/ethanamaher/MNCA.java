package com.ethanamaher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MNCA {

    private final String NEIGHBORHOOD_DIR = "src/main/resources/neighborhoods/test";
    private final Dimension imageSize;
    private BufferedImage image;
    private int[][] imageArray;
    boolean needsRedraw;
    List<List<Coordinate>> neighborhoods;
    List<HashMap<int[], Boolean>> neighborhoodRules;


    public MNCA() {
        System.out.println("INITIALIZED");
        needsRedraw = true;
        try {
            // best to keep start states smaller than 800x600, calculation slows down a bit
            image = ImageIO.read(new File("src/main/resources/starts/start-0001.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        imageSize = new Dimension(image.getWidth(), image.getHeight());
        imageArray = convertTo2D(image);
        neighborhoods = loadNeighborhoods();
        neighborhoodRules = loadNeighborhoodRules();
    }

    /**
     *  TODO pause on close window
     */
    public void closing() {

    }

    public Dimension getImageSize() {
        return imageSize;
    }

    public void update() {
        step();
    }

    /**
     * draw the current state
     *
     * @param g Graphics
     */
    synchronized protected void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[i].length; j++) {
                if (imageArray[i][j] != 0) {
                    g2.setColor(Color.WHITE); // living cell
                    g2.drawLine(j, i, j, i);
                } else {
                    g2.setColor(Color.BLACK); // dead cell
                    g2.drawLine(j, i, j, i);
                }
            }
        }
        g2.dispose();
    }

    /**
     * converts the starting state of the automata to a 2d int[]
     * where -1 denotes dead cell and 1 denotes living cell
     *
     * @param image the image of the start state
     * @return 2d array of the start state
     */
    private static int[][] convertTo2D(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
            int set = pixels[pixel] + 1;
            result[row][col] = set;
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    /**
     * Converts an image to a list of coordinates that contain their relative distance
     * from the center coordinate
     *
     * @param image image of the neighborhood
     * @return list of relative coordinates
     */
    private static List<Coordinate> convertTo2DRelativeCoordinates(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        List<Coordinate> result = new ArrayList<>();

        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
            int set = pixels[pixel] + 1;

            if (set == 1) {
                Coordinate relative = new Coordinate(col - width / 2, row - height / 2);

                if (!(relative.getX() == 0 && relative.getY() == 0)) {
                    result.add(relative);
                }
            }
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }
        return result;
    }

    /**
     * Loads the neighborhoods from resources.neighborhoods
     * reads image file and converts to 2D coordinate array denoting the relative distances a neighbor would be from the center
     *
     * @return List<List < Coordinate>> list of coordinates for all neighborhoods
     */
    private List<List<Coordinate>> loadNeighborhoods() {
        List<List<Coordinate>> neighborhoods = new ArrayList<>();
        File neighborhoodDirectory = new File(NEIGHBORHOOD_DIR);
        File[] filesList = neighborhoodDirectory.listFiles();
        for (File f : filesList) {
            BufferedImage neighborhoodImage;
            try {
                neighborhoodImage = ImageIO.read(f);
                if (neighborhoodImage == null) continue;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            neighborhoods.add(convertTo2DRelativeCoordinates(neighborhoodImage));
        }
        return neighborhoods;
    }

    /**
     * TODO figure this out
     *
     * @return
     */
    private List<HashMap<int[], Boolean>> loadNeighborhoodRules() {
        List<HashMap<int[], Boolean>> rulesList = new ArrayList<>();
        File neighborhoodRulesFile = new File(NEIGHBORHOOD_DIR + "/rules.txt");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(neighborhoodRulesFile)));
            String rule = br.readLine();

            System.out.println(rule);


        } catch (IOException e) {
            System.out.println("NO RULES FILE FOUND");
        }
        return rulesList;
    }

    /**
     * Do an iteration over the cellular automata
     */
    private void step() {
        int[][] nextIterationArray = new int[imageArray.length][imageArray[0].length];
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[i].length; j++) {
                int curr = imageArray[i][j];
                double[] neighborhoodSums = new double[neighborhoods.size()];
                for (int k = 0; k < neighborhoods.size(); k++) {
                    int neighborCount = getNeighborCount(neighborhoods.get(k), i, j);
                    neighborhoodSums[k] = (double) neighborCount / neighborhoods.get(k).size();
                }
                nextIterationArray[i][j] = checkRules(neighborhoodSums, curr);

            }
        }

        for (int i = 0; i < imageArray.length; i++) {
            int[] arr = nextIterationArray[i];
            int aLength = nextIterationArray[0].length;
            imageArray[i] = new int[aLength];
            System.arraycopy(arr, 0, imageArray[i], 0, aLength);
        }
    }

    /**
     * Checks the expected next state of i, j based on the rules of its neighborhoods
     *
     * @param neighborhoodSumAvgs the percentage of neighbor cells on in each neighborhood
     * @param curr                state of current cell i, j
     * @return the expected next state of cell i, j
     */
    private int checkRules(double[] neighborhoodSumAvgs, int curr) {
        int output = curr;


        if (neighborhoodSumAvgs[0] >= .230 && neighborhoodSumAvgs[0] <= .320)
            output = 0;
        else if (neighborhoodSumAvgs[0] >= .470 && neighborhoodSumAvgs[0] <= .550)
            output = 1;
        else if (neighborhoodSumAvgs[0] >= .710 && neighborhoodSumAvgs[0] <= .800)
            output = 1;

        if (neighborhoodSumAvgs[1] >= .110 && neighborhoodSumAvgs[1] <= .260)
            output = 0;
        else if (neighborhoodSumAvgs[1] >= .370 && neighborhoodSumAvgs[1] <= .460)
            output = 1;
        else if (neighborhoodSumAvgs[1] >= .520 && neighborhoodSumAvgs[1] <= .650)
            output = 0;
        else if (neighborhoodSumAvgs[1] >= .730 && neighborhoodSumAvgs[1] <= .860)
            output = 0;


        return output;
    }

    /**
     * Returns the count of all neighboring cells on
     *
     * @param neighbors the neighborhood to scan
     * @param i         cell row
     * @param j         cell column
     * @return the count of neighbors on (1)
     */
    private int getNeighborCount(List<Coordinate> neighbors, int i, int j) {
        int neighborCount = 0;
        for (Coordinate relativeNeighbor : neighbors) {
            if (i + relativeNeighbor.getX() >= 0 && i + relativeNeighbor.getX() < imageArray.length &&
                    j + relativeNeighbor.getY() >= 0 && j + relativeNeighbor.getY() < imageArray[0].length &&
                    imageArray[i + relativeNeighbor.getX()][j + relativeNeighbor.getY()] != 0) {
                //this neighbor is alive
                neighborCount++;
            }
        }
        return neighborCount;
    }
}
