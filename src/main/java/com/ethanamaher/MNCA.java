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

    private final String NEIGHBORHOOD_DIR = "src/main/resources/neighborhoods/test";
    private Dimension imageSize;
    private BufferedImage image;
    private int[][] imageArray;
    boolean needsRedraw;
    List<List<Coordinate>> neighborhoods;
    List<double[]> neighborhoodRuleIntervals;
    List<Integer> neighborhoodRulesMap;
    List<Boolean> neighborhoodRuleStates;

    public MNCA() {
        System.out.println("INITIALIZED");
        needsRedraw = true;
        try {
            image = ImageIO.read(new File("src/main/resources/starts/start-0001.png"));
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
        for(int i = 0; i < imageArray.length; i++) {
            for(int j = 0; j < imageArray[i].length; j++) {
                if(imageArray[i][j]==1) {
                    double chance = Math.random();
                    if (chance <= .5) imageArray[i][j] = 1;
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
    private void loadNeighborhoods() {
        neighborhoods = new ArrayList<>();
        File neighborhoodDirectory = new File(NEIGHBORHOOD_DIR);
        File[] filesList = neighborhoodDirectory.listFiles();
        BufferedImage neighborhoodImage;
        try {
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
     *
     * stores them in list of hashmaps [min, max] -> nextState
     * is there a more efficient way to do this?
     *
     * @return
     */
    private void loadNeighborhoodRules() {
        File neighborhoodRulesFile = new File(NEIGHBORHOOD_DIR + "/rules1.txt");
        Scanner fileReader;
        try {
            fileReader = new Scanner(new FileInputStream(neighborhoodRulesFile));
            int n = fileReader.nextInt();
            neighborhoodRuleIntervals = new ArrayList<>();
            neighborhoodRulesMap = new ArrayList<>();
            neighborhoodRuleStates = new ArrayList<>();
            int curr = 0;
            while (curr < n) {
                int rulec = fileReader.nextInt();
                int neighborhood = fileReader.nextInt();
                for (int i = 0; i < rulec; i++) {
                    double intervalMin = fileReader.nextDouble();
                    double intervalMax = fileReader.nextDouble();
                    int nextState = fileReader.nextInt();
                    double[] interval = new double[]{intervalMin, intervalMax};
                    neighborhoodRuleIntervals.add(interval);
                    neighborhoodRulesMap.add(neighborhood);
                    neighborhoodRuleStates.add(nextState==1);
                }
                curr++;
            }

//            for(HashMap<double[], Integer> hm : neighborhoodRules) {
//                for(double[] key : hm.keySet()) {
//                    System.out.printf("[%.3f, %.3f] %d%n", key[0], key[1], hm.get(key));
//                }
//                System.out.println();
//            }

        } catch (IOException e) {
            System.out.println("NO RULES FILE FOUND");
        }
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
                    neighborhoodSums[k] = (double) getNeighborCount(neighborhoods.get(k), i, j) / neighborhoods.get(k).size();
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
     * <p>
     * [[.230, .320], 0],
     * [.470, .550], 1],
     * [.710, .800], 1],
     *
     * @param neighborhoodSumAvgs the percentage of neighbor cells on in each neighborhood
     * @param curr                state of current cell i, j
     * @return the expected next state of cell i, j
     */
    private int checkRules(double[] neighborhoodSumAvgs, int curr) {
        int output = curr;

        for(int i = 0; i < neighborhoodSumAvgs.length; i++) {
            for(int j = 0; j < neighborhoodRuleIntervals.size(); j++) {
                double[] range = neighborhoodRuleIntervals.get(j);
                int neighborhood = neighborhoodRulesMap.get(j);
                if(neighborhoodSumAvgs[neighborhood] >= range[0] && neighborhoodSumAvgs[neighborhood] <= range[1]) {
                    output = neighborhoodRuleStates.get(j) ? 1 : 0;
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
