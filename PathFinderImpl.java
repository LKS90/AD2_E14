/*
 * HSR - Uebungen 'Algorithmen & Datenstrukturen 2'
 * Version: Wed Dec 16 21:00:24 CET 2015
 */
package uebung14.as.aufgabe01;

import java.awt.Point;
import java.util.ArrayList;

import net.datastructures.AdaptablePriorityQueue;
import net.datastructures.Entry;
import net.datastructures.HeapAdaptablePriorityQueue;

/**
 * A Dijkstra based implementation for a PathFinder.
 */
public class PathFinderImpl extends PathFinder {
  
  /** Length of the x-dimension of map. */
  final int LEN_X;
  /** Length of the y-dimension of map. */
  final int LEN_Y;
  
  /** The 'map' for the distances (for a given x/y-position): */
  double[][] distances;
  
  /** The 'map' for the locators (entries) of the adaptable priority-queue: */
  Entry<Double, VertexPos>[][] locators;
 
  /** The adaptable priority-queue storing the distances for every vertex: */
  AdaptablePriorityQueue<Double, VertexPos> q;
  
  /** The 'map' for the parent-information (for a given x/y-position): */
  Direction[][] parents;
 

  /**
   * Helper-type to support the handling with the four given directions 
   * (north, east, south and west).
   */
  enum Direction {

    NORTH(0, 1), EAST(1, 0), SOUTH(0, -1), WEST(-1, 0);

    private int deltaX;
    private int deltaY;

    Direction(int deltaX, int deltaY) {
      this.deltaX = deltaX;
      this.deltaY = deltaY;
    }

    /**
     * Calculates the next x-value for this direction.
     * @param actualX The actual x-position.
     * @return The next x-position when moving with this direction.
     */
    int nextX(int actualX) {
      return actualX + deltaX;
    }

    /**
     * Calculates the next y-value for this direction.
     * @param actualY The actual y-position.
     * @return The next y-position when moving with this direction.
     */
    int nextY(int actualY) {
      return actualY + deltaY;
    }

    /**
     * @return The opposite direction of this direction.
     */
    Direction getOpposite() {
      if (this == NORTH) {
        return SOUTH;
      } else if (this == EAST) {
        return WEST;
      } else if (this == SOUTH) {
        return NORTH;
      } else if (this == WEST) {
        return EAST;
      } else {
        throw new Error("Bad Direction!");
      }
    }

  } // enum Direction
  

  class VertexPos {
    int x;
    int y;
    public VertexPos(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
  
  
  @SuppressWarnings("unchecked")
  public PathFinderImpl(Map m) {
    super(m);
    q = new HeapAdaptablePriorityQueue<Double, VertexPos>();
    LEN_X = map.getWidth();
    LEN_Y = map.getHeight();
    distances = new double[LEN_X][LEN_Y];
    locators = new Entry[LEN_X][LEN_Y];
    parents = new Direction[LEN_X][LEN_Y];
  }

  /**
   * Finds the path running from (x0,y0) to (x1,y1). 
   * Stores the found path in the path-attribute and returns it.
   * Call notifyObservers at least at the end for the GUI.
   * 
   * @param x0
   *          Start-x.
   * @param y0
   *          Start-y.
   * @param x1
   *          End-x.
   * @param y1
   *          End-y.
   * @return The path found.
   */
  public ArrayList<Point> findPath(int x0, int y0, int x1, int y1) {
    
    
    // Just a Test (TODO: to delete):
    path.add(new Point(2, 2));
    path.add(new Point(150, 50));
    path.add(new Point(198, 198));
    
    // TODO Implement here... 
    

//          // showing actual position on map:
//          actualX = zx;
//          actualY = zy;
//          try {
//            Thread.sleep(1); // 0: much fastest;  >1 : slower, but more details
//          } catch (InterruptedException e) {}
//        }
//      }
//    }
    
    setChanged();
    notifyObservers();
    return path;
  } 

  private void generatePath(int endX, int endY) {
    path.add(0, new Point(endX, endY));
    int x = endX;
    int y = endY;
    while (parents[x][y] != null) {
      int newX = parents[x][y].nextX(x);
      int newY = parents[x][y].nextY(y);
      path.add(0, new Point(newX, newY));
      x = newX;
      y = newY;
    }
  }

} // PathFinderImpl

 
