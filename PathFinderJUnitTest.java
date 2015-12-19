/*
 * HSR - Uebungen 'Algorithmen & Datenstrukturen 2'
 * Version: Wed Dec 16 21:00:24 CET 2015
 */

package uebung14.as.aufgabe01;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class PathFinderJUnitTest {

  final static int X_DIM = 3;
  final static int Y_DIM = 6;
  
  final byte[][] MAP = {    // +---> x / East
    {   0,    0,     0},    // |
    { 127,    0,     0},    // |
    {   0,    0,     0},    // v
    {   0,  127,   127},    // y / North
    {   0,    0,     0},
    {   0,  127,     0},
  };

  final int[][] PATH = { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 1, 2 }, { 0, 2 }, { 0, 3 },
    { 0, 4 }, { 1, 4 }, { 2, 4 }, { 2, 5 } };

  Map map;
  PathFinder pf;

  @Before
  public void setUp() {
    map = new JUnitMap(X_DIM, Y_DIM, MAP);
    pf = new PathFinderImpl(map);
  }

  @Test
  public void testFindPath() {
    ArrayList<Point> resultPath = pf.findPath(0, 0, X_DIM - 1, Y_DIM - 1);
    System.out.println("\nPath found:");
    assertNotNull(resultPath);
    for (Point point : resultPath) {
      System.out.println(point);
    }
    for (int i = 0; i < PATH.length; i++) {
      assertEquals(PATH[i][0], resultPath.get(i).x);
      assertEquals(PATH[i][1], resultPath.get(i).y);
    }
  }

}

class JUnitMap extends Map {
  
  public JUnitMap(int xDim, int yDim, byte[][] map) {
    super(xDim, yDim, 0);
    matrix = map;
    System.out.println("The Map:");
    print();
  }
 
  @Override
  public void print() {
    String[][] strArr = new String[getHeight()+2][getWidth()+2];
    for (int y = 0; y < strArr.length; y++) {
      for (int x = 0; x < strArr[0].length; x++) {
        strArr[y][x] = "";
      }
    }
    strArr[0][0] = "+";
    for (int y = 1; y < strArr.length - 1; y++) {
        strArr[y][0] = "|";
    }
    strArr[strArr.length-1][0] = "^ y / North";
    for (int x = 1; x < strArr[0].length - 1; x++) {
        strArr[0][x] = "-";
    }
    strArr[0][strArr[0].length-2] = "> x / East";
    for (int y = getHeight()-1; y >= 0 ; y--) {
      for (int x = 0; x < getWidth(); x++) {
        strArr[y+1][x+1] = Byte.toString(matrix[y][x]);
      }
    }
    for (int y = strArr.length - 1; y >= 0; y--) {
      for (int x = 0; x < strArr[0].length; x++) {
        System.out.print(strArr[y][x] + "\t");
      }
      System.out.println(" ");
    }
  }


}

/* Session-Log:

The Map:
^ y / North                                      
|       0       127     0                
|       0       0       0                
|       0       127     127              
|       0       0       0                
|       127     0       0                
|       0       0       0                
+       -       -       > x / East               

Path found:
java.awt.Point[x=0,y=0]
java.awt.Point[x=1,y=0]
java.awt.Point[x=1,y=1]
java.awt.Point[x=1,y=2]
java.awt.Point[x=0,y=2]
java.awt.Point[x=0,y=3]
java.awt.Point[x=0,y=4]
java.awt.Point[x=1,y=4]
java.awt.Point[x=2,y=4]
java.awt.Point[x=2,y=5]

*/
 
 
