package com.ethanamaher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class MNCA {

    private final String NEIGHBORHOOD_DIR = "src/main/resources/neighborhoods/worms";
    private final Dimension imageSize;
    private BufferedImage image;
    private int[][] imageArray;
    boolean needsRedraw;
    List<List<Coordinate>> neighborhoods;


    public MNCA() {
        needsRedraw = true;
        try {
            image = ImageIO.read(new File("src/main/resources/startKlecks.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        imageSize = new Dimension(image.getWidth(), image.getHeight());
        imageArray = convertTo2D(image);
        neighborhoods = loadNeighborhoods();
    }

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
                    if (imageArray[i][j] != -1) {
                        g2.setColor(Color.BLACK);
                        g2.drawLine(j, i, j, i);
                    } else {
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.drawLine(j, i, j, i);
                    }
                }
            }
            g2.dispose();


    }

    /**
     * converts the starting state of the automata to a 2d int[]
     * where -1 denotes dead cell and 1 denotes living cell
     * @param image the image of the start state
     * @return 2d array of the start state
     */
    private static int[][] convertTo2D(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[][] result = new int[height][width];
        for(int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += 3) {
            int argb = 0;
            argb += -16777216; // alpha channel
            argb += ((int) pixels[pixel] & 0xff); // blue
            argb += ((int) pixels[pixel+1] & 0xff << 8); // green
            argb += ((int) pixels[pixel+2] & 0xff << 16); // red

            result[row][col] = (argb == -1) ? -1 : 1;

            col++;
            if(col == width) {
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
        for(int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += 3) {
            int argb = 0;
            argb += -16777216; // alpha channel
            argb += ((int) pixels[pixel] & 0xff); // blue
            argb += ((int) pixels[pixel + 1] & 0xff << 8); // green
            argb += ((int) pixels[pixel + 2] & 0xff << 16); // red


            if (argb != -1) {
                Coordinate relativeCoordinate = new Coordinate(col-width/2, row-height/2);
                if(!(relativeCoordinate.getX() == 0 && relativeCoordinate.getY() == 0)) {
                    result.add(relativeCoordinate);
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
     * @return List<List<Coordinate>> list of coordinates for all neighborhoods
     */
    private List<List<Coordinate>> loadNeighborhoods() {
        List<List<Coordinate>> neighborhoods = new ArrayList<>();
        File neighborhoodDirectory = new File(NEIGHBORHOOD_DIR);
        File[] filesList = neighborhoodDirectory.listFiles();
        for(File f : filesList) {
            BufferedImage neighborhoodImage;
            try {
                neighborhoodImage = ImageIO.read(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            neighborhoods.add(convertTo2DRelativeCoordinates(neighborhoodImage));
        }
        return neighborhoods;
    }

    /**
     * Do an iteration over the cellular automata
     */
    private void step() {
        int[][] nextIterationArray = new int[imageArray.length][imageArray[0].length];

        for(int i = 0; i < imageArray.length; i++) {
            for(int j = 0; j < imageArray[i].length; j++) {
                int curr = imageArray[i][j];
                    for(int k = 0; k < neighborhoods.size(); k++) {
                        int neighborCount = getNeighborCount(neighborhoods.get(k), i, j);
                        nextIterationArray[i][j] = checkRules(k, neighborCount, curr);
                    }
            }
        }

        for(int i = 0; i < imageArray.length; i++)
        {
            int[] arr = nextIterationArray[i];
            int   aLength = nextIterationArray[0].length;
            imageArray[i] = new int[aLength];
            System.arraycopy(arr, 0, imageArray[i], 0, aLength);
        }
    }

    /*
    TODO: FIX RULES BECAUSE THIS SUCKS
        * need to store int the form of
        * [[min, max, bool], [min, max, bool]] for each neighborhood
        * ex: [[0, 5, true], [6, 10, false]]
        * for neighborhood 0
        * if 0-5 neighbors cell gets life
        * else if 6 to 10 neighbors cell dies
        * else cell value doesnt change
     */
    private int checkRules(int neighborhood, int neighborCount, int curr) {
        if(neighborhood == 0) {
            if(neighborCount >= 2 && neighborCount <= 45) return -1;
            else if (neighborCount >= 49 && neighborCount <= 51) return 1;
            else return curr;
        } else if(neighborhood == 1) {
            if(neighborCount >= 20 && neighborCount <= 55) return -1;
            else if(neighborCount >= 5 && neighborCount <= 14) return 1;
            else return curr;
        } else if(neighborhood == 2) {
            if(neighborCount >= 4 && neighborCount <= 37) return -1;
            else if(neighborCount >= 39 && neighborCount <= 41) return 1;
            else return curr;
        }

        //bacteria
//        if (neighborhood == 0) {
//            if (neighborCount >= 0 && neighborCount <= 17) return -1;
//            else if (neighborCount >= 40 && neighborCount <= 42) return 1;
//            else return curr;
//        } else if (neighborhood == 1) {
//            if (neighborCount >= 10 && neighborCount <= 13) return 1;
//            else return curr;
//        } else if (neighborhood == 2) {
//            if (neighborCount >= 9 && neighborCount <= 21) return -1;
//            else return curr;
//        } else if(neighborhood==3) {
//            if(neighborCount >= 78 && neighborCount<=89||
//                neighborCount>=108&&neighborCount<=500) return -1;
//            else return curr;
//        }
        return -1;
    }

    private int getNeighborCount(List<Coordinate> neighbors, int i, int j) {
        int neighborCount = 0;
        for(Coordinate relativeNeighbor : neighbors) {
            if( i + relativeNeighbor.getY() >= 0 && i + relativeNeighbor.getY() < imageArray.length &&
                    j + relativeNeighbor.getX() >= 0 && j + relativeNeighbor.getX() < imageArray[0].length &&
                    imageArray[i + relativeNeighbor.getY()][j + relativeNeighbor.getX()] != -1) {
                //this neighbor is alive
                neighborCount++;
            }
        }
        return neighborCount;
    }
}
