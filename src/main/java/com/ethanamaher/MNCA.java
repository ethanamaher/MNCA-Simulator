package com.ethanamaher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class MNCA {

    private final String NEIGHBORHOOD_DIR = "src/main/resources/neighborhoods/example";
    private final String NEIGHBORHOOD_RULES_FILE = NEIGHBORHOOD_DIR + "/rules.txt";
    private final String START_IMAGE_FILE = "src/main/resources/starts/example_start.png";

    private Dimension imageSize;
    private BufferedImage image;
    private int[][] imageArray;

    List<List<Coordinate>> neighborhoods;

    //stores intervals for neighborhood rules
    List<double[]> neighborhoodRuleIntervals;
    //stores the neighborhood an interval in neighborhoodRuleIntervals corresponds to
    List<int[]> neighborhoodRulesMap;

    public MNCA() {
        System.out.println("INITIALIZED");
        try {
            image = ImageIO.read(new File(START_IMAGE_FILE));
        } catch (Exception e) {

        }
        imageArray = convertTo2D(image);
        imageSize = new Dimension(image.getWidth(), image.getHeight());
        fillRandomly(imageSize);

        loadNeighborhoods();
        loadNeighborhoodRules();
    }

    private void fillRandomly(Dimension imageSize) {
        this.imageSize = imageSize;
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[i].length; j++) {
                if (imageArray[i][j] == 1) {
                    /*
                    Randomly fill colored space in image
                    to keep it from being just a big block of cells
                     */
                    double chance = Math.random();
                    if (chance <= .72) imageArray[i][j] = 1;
                    else imageArray[i][j] = 0;
                }
            }
        }
    }

    /**
     * TODO pause on close window
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
        for (int i = 0; i < imageArray.length; i++) { // row in array = y value in image
            for (int j = 0; j < imageArray[i].length; j++) { // col in array = x value in image
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
     * <p>
     * TODO add multiple states by color
     * <p>
     * argb = 0xffffffff
     * (1111 1111) (1111 1111) (1111 1111) (1111 1111)
     * alpha = argb >> 24
     * red = argb >> 16
     * blue
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
     * <p>
     * TODO add multiple states by color
     *
     * @param image image of the neighborhood
     * @return list of relative coordinates
     */
    private static List<Coordinate> convertToRelativeCoordinates(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        List<Coordinate> result = new ArrayList<>();

        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
            int set = pixels[pixel] + 1;

            if (set == 1) {
                Coordinate relative = new Coordinate(col - width / 2, row - height / 2);

                if (!(relative.getX() == 0 && relative.getY() == 0)) { // dont add the cell to its own neighbors list
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
     * Loads the neighborhoods from resources/neighborhoods
     * reads image file and converts to 2D coordinate array denoting the relative distances a neighbor would be from the center
     *
     * @return List<List < Coordinate>> list of coordinates for all neighborhoods
     */
    private void loadNeighborhoods() {
        neighborhoods = new ArrayList<>();
        File neighborhoodDirectory = new File(NEIGHBORHOOD_DIR);
        File[] filesList = neighborhoodDirectory.listFiles();
        BufferedImage neighborhoodImage;
        try {
            /**
             * should add check for if file is an image
             */
            for (File f : filesList) { // foreach neighborhood image file
                neighborhoodImage = ImageIO.read(f);
                if (neighborhoodImage == null) continue;
                neighborhoods.add(convertToRelativeCoordinates(neighborhoodImage));
            }
        } catch (IOException e) {
            System.err.println("Neighborhood file not found");
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads neighborhood rules from its rules.txt file
     * <p>
     * stores them in list of hashmaps [min, max] -> nextState
     * is there a more efficient way to do this?
     *
     * @return
     */
    private void loadNeighborhoodRules() {
        File neighborhoodRulesFile = new File(NEIGHBORHOOD_RULES_FILE);
        Scanner fileReader;
        try {
            fileReader = new Scanner(new FileInputStream(neighborhoodRulesFile));
            // number of rules
            int n = fileReader.nextInt();
            neighborhoodRuleIntervals = new ArrayList<>();
            neighborhoodRulesMap = new ArrayList<>();
            int neighborhood, nextState;
            double intervalMin, intervalMax;
            int curr = 0;
            while (curr < n) {
                neighborhood = fileReader.nextInt();
                intervalMin = fileReader.nextDouble();
                intervalMax = fileReader.nextDouble();
                nextState = fileReader.nextInt();
                double[] interval = new double[]{intervalMin, intervalMax};
                int[] mapping = new int[]{neighborhood, nextState};
                neighborhoodRuleIntervals.add(interval);
                neighborhoodRulesMap.add(mapping);
                curr++;
            }

        } catch (IOException e) {
            System.out.println("NO RULES FILE FOUND");
        }
    }

    /**
     * Do an iteration over the cellular automata
     */
    private void step() {
        int[][] nextIteration = new int[imageArray.length][imageArray[0].length];
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[i].length; j++) {
                int curr = imageArray[i][j];
                double[] neighborhoodSums = new double[neighborhoods.size()];
                for (int k = 0; k < neighborhoods.size(); k++) {
                    neighborhoodSums[k] = (double) getNeighborCount(neighborhoods.get(k), i, j) / neighborhoods.get(k).size();
                }
                nextIteration[i][j] = checkRules(neighborhoodSums, curr);
            }
        }

        for (int i = 0; i < nextIteration.length; i++) {
            System.arraycopy(nextIteration[i], 0, imageArray[i], 0, nextIteration[0].length);
        }
    }

    /**
     * Checks the expected next state of i, j based on the rules of its neighborhoods
     * <p>
     *
     * @param neighborhoodSumAvgs the percentage of neighbor cells on in each neighborhood
     * @param curr                state of current cell i, j
     * @return the expected next state of cell i, j
     */
    private int checkRules(double[] neighborhoodSumAvgs, int curr) {
        int output = curr;

        for (int i = 0; i < neighborhoodSumAvgs.length; i++) {
            for (int j = 0; j < neighborhoodRuleIntervals.size(); j++) {
                int neighborhood = neighborhoodRulesMap.get(j)[0];
                if (neighborhoodSumAvgs[neighborhood] >= neighborhoodRuleIntervals.get(j)[0] && neighborhoodSumAvgs[neighborhood] <= neighborhoodRuleIntervals.get(j)[1]) {
                    output = neighborhoodRulesMap.get(j)[1] == 1 ? 1 : 0;
                }

            }
        }
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
            if (i + relativeNeighbor.getY() >= 0 && i + relativeNeighbor.getY() < imageArray.length &&
                    j + relativeNeighbor.getX() >= 0 && j + relativeNeighbor.getX() < imageArray[0].length &&
                    imageArray[i + relativeNeighbor.getY()][j + relativeNeighbor.getX()] != 0) {
                //this neighbor is alive
                neighborCount++;
            }
        }
        return neighborCount;
    }
}
