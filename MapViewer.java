/*
 * HSR - Uebungen 'Algorithmen & Datenstrukturen 2'
 * Version: Wed Dec 16 21:00:24 CET 2015
 */

package uebung14.as.aufgabe01;

import javax.media.opengl.*;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.opengl.util.Animator;

/**
 * @author msuess
 */
public class MapViewer extends JFrame {

  private static final long serialVersionUID = 1L;
  private MapView glScene;
  private JPanel infoPanel;
  private JLabel infoLabel;
  private Map map;
  private SearchThread st;
  protected PathFinder pf;

  private double totWeight;

  public MapViewer() {
    this(0);
  }

  public MapViewer(int pathfinder) {
    super("Map Viewer");
    GLCanvas canvas = new GLCanvas();

    totWeight = 0;

    map = new Map(200, 200, 0.001);

    JFrame.setDefaultLookAndFeelDecorated(true);
    getContentPane().setLayout(new BorderLayout());
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
    setSize(1024, 768);
    setLocationRelativeTo(null); // position the frame in the screen center

    glScene = new MapView(getSize().width, getSize().height, map, this);
    canvas.addGLEventListener(glScene);
    canvas.addKeyListener(glScene);
    canvas.addMouseMotionListener(glScene);

    getContentPane().add(canvas, BorderLayout.CENTER);
    infoPanel = new JPanel();
    infoLabel = new JLabel("Total Path Weight: -");
    infoPanel.add(infoLabel);
    getContentPane().add(infoPanel, BorderLayout.NORTH);

    setVisible(true);

    switch (pathfinder) {
      default:
        pf = new PathFinderImpl(map);
        break;
    }

    searchPath();
    final Animator animator = new Animator(canvas);
    this.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {
        /* Run this on another thread than the AWT event queue to
         * make sure the call to Animator.stop() completes before
         * exiting.
         */
        new Thread(new Runnable() {

          public void run() {
            animator.stop();
            System.exit(0);
          }
        }).start();
      }
    });
    animator.start();
  }

  public void searchPath() {
    st = new SearchThread(map, (MapView) glScene, this, pf);
    st.start();
  }

  public void setTotWeight(double totWeight) {
    this.totWeight = totWeight;
    updateInfoLabel();
  }

  private void updateInfoLabel() {
    String weight = Math.round(totWeight) == 0 ? "-" : (new Long(Math
        .round(totWeight)).toString());
    infoLabel.setText("Total Path Weight: " + weight);
  }

  public class SearchThread extends Thread implements Observer {

    private Map map;
    private MapView mapView;
    private MapViewer mapViewer;
    private PathFinder pf;

    public SearchThread(Map map, MapView mapView, MapViewer mapViewer,
        PathFinder pf) {
      this.map = map;
      this.mapView = mapView;
      this.mapViewer = mapViewer;
      this.pf = pf;
    }

    public void run() {
      ArrayList<Point> path = new ArrayList<Point>();
      path.add(new Point(0, 0));
      mapView.updatePath(path);
      pf.addObserver(this);
      pf.findPath(2, 2, map.getHeight() - 2, map.getWidth() - 2);
    }

    public void update(Observable arg0, Object arg1) {
      mapView.updatePath(pf.getPath());
      mapViewer.setTotWeight(pf.calcTotalWeight(pf.getPath()));
      pf.deleteObserver(this);
    }
  }

  public static void main(String[] args) {
    new MapViewer(0);
  }
}
 
 
