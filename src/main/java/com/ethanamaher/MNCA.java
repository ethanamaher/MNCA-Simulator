package com.ethanamaher;

import com.ethanamaher.helpers.Cell;
import com.ethanamaher.helpers.Coordinate;
import com.ethanamaher.helpers.Interval;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class MNCA {

    private final String NEIGHBORHOOD_DIR = "src/main/resources/neighborhoods/example";
    private final String NEIGHBORHOOD_RULES_FILE = NEIGHBORHOOD_DIR + "/rules.txt";
    private final String START_IMAGE_FILE = "src/main/resources/starts/randomfilltest.png";

    private final Dimension imageSize;
    private BufferedImage image;
    private final Cell[][] displayedImage;
    private final Cell[][] bufferedImage;
    private int rows;
    private int cols;

    private List<List<Coordinate>> neighborhoods;

    //stores intervals for neighborhood rules
    private List<Interval> neighborhoodRuleIntervals;

    public MNCA() {
        System.out.println("INITIALIZED");
        try {
            image = ImageIO.read(new File(START_IMAGE_FILE));
        } catch (IOException e) {
            System.err.println("Image file not found at: " + START_IMAGE_FILE);
        }
        displayedImage = new Cell[image.getHeight()][image.getWidth()];
        this.rows = displayedImage.length;
        this.cols = displayedImage[0].length;
        bufferedImage = new Cell[rows][cols];
        convertTo2D(image);
        fillRandomly(displayedImage);
        this.imageSize = new Dimension(image.getWidth(), image.getHeight());

        loadNeighborhoods();
        loadNeighborhoodRules();
    }

    private void fillRandomly(Cell[][] image) {
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[i].length; j++) {
                if (image[i][j].isAlive()) {
                    /*
                    Randomly fill colored space in image
                    to keep it from being just a big block of cells
                     */
                    double chance = Math.random();
                    // 70% chance of starting alive if image has it alive
                    // modify this so it can work if no image is provided
                    if (chance <= .72) image[i][j].setState(1);
                    else image[i][j].setState(0);

                    bufferedImage[i][j].setState(image[i][j].getState());
                }
            }
        }
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
     * NOT WORKING
     *
     * @param g Graphics
     */
    synchronized protected void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < rows; i++) { // row in array = y value in image
            for (int j = 0; j < cols; j++) { // col in array = x value in image
                g2.setColor(displayedImage[i][j].isAlive() ? Color.WHITE : Color.BLACK);
                g2.drawLine(j, i, j, i);
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
    private void convertTo2D(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();

        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
            int state = pixels[pixel] + 1;
            bufferedImage[row][col] = new Cell(state);
            displayedImage[row][col] = bufferedImage[row][col];
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }
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
        List<Coordinate> result = new ArrayList<>(pixels.length);

        int centerX = width / 2;
        int centerY = height / 2;

        for (int pixelIndex = 0, row = 0, col = 0; pixelIndex < pixels.length; pixelIndex++) {
            boolean isSet = pixels[pixelIndex] == 0;

            if (isSet) {
                int relativeX = col - centerX;
                int relativeY = row - centerY;

                // Avoid adding the center coordinate (0, 0)
                if (relativeX != 0 || relativeY != 0) {
                    result.add(new Coordinate(relativeX, relativeY));
                }
            }

            // Update column and row indices
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
     */
    private void loadNeighborhoods() {
        neighborhoods = new ArrayList<>();
        File neighborhoodDirectory = new File(NEIGHBORHOOD_DIR);

        // Ensure the directory exists and is valid
        if (!neighborhoodDirectory.exists() || !neighborhoodDirectory.isDirectory()) {
            System.err.println("Error: Neighborhood directory not found or invalid: " + NEIGHBORHOOD_DIR);
            return;
        }

        File[] filesList = neighborhoodDirectory.listFiles();
        if (filesList == null || filesList.length == 0) {
            System.err.println("Error: No files found in neighborhood directory: " + NEIGHBORHOOD_DIR);
            return;
        }

        // Loop through each file and process image files
        for (File file : filesList) {
            if (!file.isFile() || !isImageFile(file)) {
                continue; // Skip non-image or invalid files
            }

            try {
                BufferedImage neighborhoodImage = ImageIO.read(file);
                if (neighborhoodImage != null) {
                    neighborhoods.add(convertToRelativeCoordinates(neighborhoodImage));
                }
            } catch (IOException e) {
                System.err.println("Error: Unable to read neighborhood image: " + file.getName());
                throw new RuntimeException(e);
            }
        }

        System.out.println("Successfully loaded " + neighborhoods.size() + " neighborhoods.");
    }

    // Helper method to check if a file is an image
    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".bmp");
    }

    /**
     * Loads neighborhood rules from its rules file
     * <p>
     * is there a more efficient way to do this?
     */
    private void loadNeighborhoodRules() {
        File neighborhoodRulesFile = new File(NEIGHBORHOOD_RULES_FILE);
        // Ensure file exists before attempting to read
        if (!neighborhoodRulesFile.exists()) {
            System.out.println("Error: Rules file not found: " + NEIGHBORHOOD_RULES_FILE);
            return;
        }

        try (Scanner fileReader = new Scanner(new FileInputStream(neighborhoodRulesFile))) {
            int numberOfRules = fileReader.nextInt();
            neighborhoodRuleIntervals = new ArrayList<>(numberOfRules);

            for(int i = 0; i < numberOfRules; i++) {
                int neighborhood = fileReader.nextInt();
                double intervalMin = fileReader.nextDouble();
                double intervalMax = fileReader.nextDouble();
                int nextState = fileReader.nextInt();

                // Create and store the interval object
                Interval interval = new Interval(neighborhood, intervalMin, intervalMax, nextState);
                neighborhoodRuleIntervals.add(interval);
            }
            System.out.println("Successfully loaded " + numberOfRules + " neighborhood rules.");

        } catch (FileNotFoundException e) {
            System.out.println("Error: Unable to find rules file: " + NEIGHBORHOOD_RULES_FILE);
        } catch (InputMismatchException e) {
            System.out.println("Error: Malformed data in rules file.");
        }
    }

    /**
     * Do an iteration over the cellular automata
     */
    private void step() {
        int numNeighborhoods = neighborhoods.size();

        // Cache neighborhood sizes to avoid repeated calls inside loops
        double[] neighborhoodSizes = new double[numNeighborhoods];
        for (int k = 0; k < numNeighborhoods; k++) {
            neighborhoodSizes[k] = neighborhoods.get(k).size();
        }


        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell currentCell = bufferedImage[i][j];
                double[] neighborhoodSums = new double[numNeighborhoods];

                for (int k = 0; k < numNeighborhoods; k++) {
                    int neighborCount = getNeighborCount(neighborhoods.get(k), i, j);
                    neighborhoodSums[k] = (double) neighborCount / neighborhoodSizes[k];
                }

                currentCell.setNextState(checkRules(neighborhoodSums, currentCell));
            }
        }

        for(int i = 0; i < bufferedImage.length; i++) {
            for(int j = 0; j < bufferedImage[0].length; j++) {
                bufferedImage[i][j].step();
            }
            System.arraycopy(bufferedImage[i], 0, displayedImage[i], 0, bufferedImage.length);
        }
    }

    /**
     * Checks the expected next state of i, j based on the rules of its neighborhoods
     * <p>
     *
     * @param neighborhoodSumAverages the percentage of neighbor cells on in each neighborhood
     * @param currentCell             state of current cell i, j
     * @return the expected next state of cell i, j
     */
    private int checkRules(double[] neighborhoodSumAverages, Cell currentCell) {
        int output = currentCell.getState();

        for (int i = 0; i < neighborhoodSumAverages.length; i++) {
            for (Interval currentInterval : neighborhoodRuleIntervals) {
                int neighborhood = currentInterval.getNeighborhood();
                if (currentInterval.contains(neighborhoodSumAverages[neighborhood])) {
                    output = currentInterval.getNextState();
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
            if (i + relativeNeighbor.getY() >= 0 && i + relativeNeighbor.getY() < rows &&
                    j + relativeNeighbor.getX() >= 0 && j + relativeNeighbor.getX() < cols &&
                    bufferedImage[i + relativeNeighbor.getY()][j + relativeNeighbor.getX()].isAlive()) {
                //this neighbor is alive
                neighborCount++;
            }
        }
        return neighborCount;
    }
}
