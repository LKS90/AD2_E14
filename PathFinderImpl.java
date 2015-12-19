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
   * Helper-type to support the handling with the eight given directions 
   * (north, east, south, west and all headings between them).
   */
  enum Direction {

    NORTH(0), NORTHEAST(45),
    EAST(90), SOUTHEAST(135),
    SOUTH(180), SOUTHWEST(225),
    WEST(270), NORTHWEST(315);

    private int deg;

    Direction(int deg) {
      this.deg = deg;
    }

    /**
     * Calculates the next x-value for this direction.
     * @param actualX The actual x-position.
     * @return The next x-position when moving with this direction.
     */
    int nextX(int actualX) {
      return actualX + (int) Math.round(Math.sin(Math.toRadians(deg)));
    }

    /**
     * Calculates the next y-value for this direction.
     * @param actualY The actual y-position.
     * @return The next y-position when moving with this direction.
     */
    int nextY(int actualY) {
      return actualY + (int) Math.round(Math.cos(Math.toRadians(deg)));
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
      } else if (this == NORTHEAST) {
        return SOUTHWEST;
      } else if (this == SOUTHEAST) {
        return NORTHWEST;
      } else if (this == SOUTHWEST) {
        return NORTHEAST;
      } else if (this == NORTHWEST) {
        return SOUTHEAST;
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
   for(int x = 0; x < LEN_X; x++) {
     for(int y = 0; y < LEN_Y; y++) {
       if (x == x0 && y == y0) {
         distances[x][y] = 0.0;
       }
       else {
   	     distances[x][y] = Double.MAX_VALUE;
       }
       VertexPos v = new VertexPos(x, y);
       Entry<Double, VertexPos> locator = q.insert(distances[x][y], v);
       locators[x][y] = locator;
   	 }
   }
   
   while (!q.isEmpty()) {
   	Entry<Double, VertexPos> location = q.removeMin();
   	int ux = location.getValue().x;
   	int uy = location.getValue().y;
   	if (ux == x1 && uy == y1) {
   		generatePath(ux,uy);
   		break;
   	}
   	int zx = 0;
   	int zy = 0;
   	
   	// Aim for the goal from the current position.
   	int[] aim = {(x1 - ux), (y1 - uy)};
   	double aim_val = Math.sqrt(aim[0] * aim[0] + aim[1] * aim[1]);
   	double[] aim_normalized = {aim[0] / aim_val, aim[1] / aim_val};

   	// The angle of the current aim in degrees
    double angle = 90 - Math.toDegrees(Math.atan2(aim_normalized[1], aim_normalized[0]));
    if (angle < 0) angle += 360;
    if (angle >= 360) angle -= 360;

    // Define an offset for the directions array depending on the aim.
    // If we head north (angle = 0), we want to have NORTHWEST (7), NORTH (0), NORTHEAST (1),
    // For northwest (angle = 45), we want to have NORTH (0), NORTHEAST (1), EAST (2), etc...
    int offset = 0;
    if (angle < 90) {
      offset = -1;
      if (angle > 45) offset++;
    }
    else if (angle < 180) {
      offset = 1;
      if (angle > 135) offset++;
    } 
    else if (angle < 270) {
      offset = 3;
      if (angle > 225) offset++;
    }
    else if (angle < 360) {
   	  offset = 5;
      if (angle > 315) offset++;
    }
        
    // Build an array with the three directions towards the goal, use the offset defined above.
    Direction[] all_directions = Direction.values();
    Direction[] directions = new Direction[3];
    for (int k = 0; k < directions.length; k++) {
      // Make sure the  index is not negative
      if (k + offset < 0) {
    	offset = all_directions.length - 1;
      }
      directions[k] = all_directions[(k + offset) % all_directions.length];    		
    }
    
    // Swap the middle and first directions, so we head towards the "center" and
    // only try and navigate around an obstacle if necessary.
    Direction temp = directions[0];
    directions[0] = directions[1];
    directions[1] = temp;
    
    for (Direction dir : directions) {
      zx = Math.max(Math.min(dir.nextX(ux), LEN_X - 1), 0);
      zy = Math.max(Math.min(dir.nextY(uy), LEN_Y - 1), 0);
            
      double r = distances[ux][uy] + map.calcWeight(ux, uy, zx, zy);
      if (r < distances[zx][zy]) {
    	distances[zx][zy] = r;
    	parents[zx][zy] = dir.getOpposite();
    	q.replaceKey(locators[zx][zy], r);
      }
    }
  }
    
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

 
