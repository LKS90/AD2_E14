/*
 * HSR - Uebungen 'Algorithmen & Datenstrukturen 2'
 * Version: Wed Dec 16 21:00:24 CET 2015
 */
package uebung14.as.aufgabe01;

import java.awt.Point;
import java.util.Iterator;
import java.util.Observable;
import java.util.ArrayList;

/**
 * Base class for all path finders.
 * 
 * @author thabo
 */
public abstract class PathFinder extends Observable {

  // the map on which to find the path
  protected Map map;
  // the path
  protected ArrayList<Point> path;
  protected int actualX;
  protected int actualY;

  /**
   * Pathfinder for the given map.
   */
  public PathFinder(Map m) {
    map = m;
    path = new ArrayList<Point>();
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
  public abstract ArrayList<Point> findPath(int x0, int y0, int x1, int y1);

  public ArrayList<Point> getPath() {
    return path;
  }

  /**
   * Calculate the total weight of the provided path.
   * 
   * @param p
   *          The path.
   * @return Total weight.
   */
  public double calcTotalWeight(ArrayList<Point> p) {
    double w = 0;
    if (p.size() > 0) {
      Iterator<Point> it = p.iterator();
      Point pt1, pt2;
      pt2 = (Point) it.next();
      while (it.hasNext()) {
        pt1 = pt2;
        pt2 = (Point) it.next();
        w += map.calcWeight(pt1.x, pt1.y, pt1.x, pt2.x);
      }
    }
    return w;
  }

  public Point getActualXY() {
    return new Point(actualX, actualY);
  }
}
 
 
