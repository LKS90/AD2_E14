/*
 * HSR - Uebungen 'Algorithmen & Datenstrukturen 2'
 * Version: Wed Dec 16 21:00:24 CET 2015
 */

package uebung14.as.aufgabe01;

/**
 * Generates random altitude maps (altitude is normalized within [-128,127]).
 */
public class Map {

  /**
   * The cliffy factor determines how smooth the resulting map will be. Small
   * factors will result in smooth maps, large ones in cliffy maps. The factor
   * determines the amount of initial seeds (cliffy*width*height) seeds will be
   * used.
   */
  public static final double DEFAULT_CLIFFY = 0.01;

  public static final double WEIGHT_FACTOR = 100;
  // width of the map
  private int width;
  // height of the map
  private int height;
  // @see DEFAULT_CLIFFY
  private double cliffy;
  // internal map storage in matrix[height][width]
  protected byte[][] matrix;

  /**
   * Construct a widthxheight map with cliffy topology.
   */
  public Map(int width, int height, double cliffy) {
    this.width = width;
    this.height = height;
    this.cliffy = cliffy;
    matrix = new byte[height][width];
    createMatrix();
  }

  /**
   * Construct a widthxheight map with DEFAULT_CLIFFY topology.
   */
  public Map(int width, int height) {
    this(width, height, DEFAULT_CLIFFY);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public byte[][] getMatrix() {
    return matrix;
  }

  /**
   * Calculate the path-cost between two z levels.
   * 
   * @param z0
   *          Z level 1.
   * @param z1
   *          Z level 2.
   * @return The weight.
   */
  public double calcWeight(int z0, int z1) {
    return Math.abs(WEIGHT_FACTOR * (z0 - z1));
  }

  /**
   * Calculate the path-cost between the z levels of two points.
   */
  public double calcWeight(int x0, int y0, int x1, int y1) {
    //return calcWeight(matrix[y0][x0],matrix[y1][x1]);
    return Math.sqrt(calcSquaredDist(x0, y0, x1, y1)
        + Math.pow(WEIGHT_FACTOR * (matrix[y0][x0] - matrix[y1][x1]), 2));
  }

  public double calcDist(int x0, int y0, int x1, int y1) {
    return Math.sqrt(calcSquaredDist(x0, y0, x1, y1));
  }

  public double calcSquaredDist(int x0, int y0, int x1, int y1) {
    return Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2);
  }

  // randomly construct a map
  public void createMatrix() {
    int nSeed = (int) Math.round(cliffy * width * height);
    int[] seedX = new int[nSeed];
    int[] seedY = new int[nSeed];
    int[] seedZ = new int[nSeed];
    int minZ = 255;
    int maxZ = 0;
    for (int i = 0; i < nSeed; i++) {
      seedX[i] = (int) Math.round(Math.random() * width);
      seedY[i] = (int) Math.round(Math.random() * height);
      seedZ[i] = (int) Math.round(Math.random() * 255 - 128);
    }
    double val;
    double tot;
    double dist;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        val = 0;
        tot = 0;
        for (int z = 0; z < nSeed; z++) {
          dist = (Math.pow(y - seedY[z], 2) + Math.pow(x - seedX[z], 2));
          dist = dist != 0 ? 1 / dist : 1;
          val += seedZ[z] * dist;
          tot += dist;
        }
        matrix[y][x] = (byte) Math.round(val / tot);
        if (matrix[y][x] > maxZ)
          maxZ = matrix[y][x];
        if (matrix[y][x] < minZ)
          minZ = matrix[y][x];
      }
    }
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (matrix[y][x] >= 0) {
          matrix[y][x] = (byte) (matrix[y][x] * 127.0 / maxZ);
        } else {
          matrix[y][x] = (byte) (matrix[y][x] * -128.0 / minZ);
        }
      }
    }
  }

  /**
   * Print the matrix in matlab style.
   */
  public void printMatlab() {
    System.out.print("M=[");
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        System.out.print(matrix[y][x] + " ");
      }
      System.out.print("; ");
    }
    System.out.println("]");
  }

  /**
   * Pretty print.
   */
  public void print() {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        System.out.print(matrix[y][x] + "\t");
      }
      System.out.println(" ");
    }
  }

  /**
   * Test function.
   */
  public static void main(String[] args) {
    Map m = new Map(50, 50, 0.01);
    m.print();
  }
}
 
 
