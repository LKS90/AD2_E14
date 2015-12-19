/*
 * HSR - Uebungen 'Algorithmen & Datenstrukturen 2'
 * Version: Wed Dec 16 21:00:24 CET 2015
 */

package uebung14.as.aufgabe01;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class MapView implements GLEventListener, KeyListener,
    MouseMotionListener {

  // modification rates
  private static final double ROTATION = 4.0;
  private static final double OFFSET_X = 0.3;
  private static final double OFFSET_Y = 0.15;
  private static final double ZOOM = 0.3;

  private Map map;
  private GLU glu = new GLU();
  private MapViewer mapViewer;
  private FlyThroughThread ftt;
  private ArrayList<Point> path;
  private byte[][] matrix;
  private boolean[][] visitedMatrix;
  private float[][] hColor;
  private boolean fly;
  private float[] pt1;
  private float[] pt2;

  private static int[] HCOLOR_BLUE = { 0, 0, 154 };
  private static int[] HCOLOR_GREEN = { 21, 128, 0 };
  private static int[] HCOLOR_BROWN = { 128, 78, 0 };
  private static int[] HCOLOR_WHITE = { 255, 255, 255 };

  private final int hColorBlueHeight = 0;
  private final int hColorGreenHeight = 150;
  private final int hColorBrownHeight = 180;
  private final int hColorWhiteHeight = 255;

  private float xrot = -40;
  private float yrot = 0;
  private float zrot = -20;
  private float xpos = 0.0f;
  private float ypos = 0.0f;
  private float zpos = -2.5f;

  private int mouseX = 0;
  private int mouseY = 0;

  public MapView(int w, int h, Map map, MapViewer mapViewer) {
    path = null;
    fly = false;

    this.map = map;
    this.mapViewer = mapViewer;

    hColor = new float[hColorWhiteHeight + 1][3];
    int tmpHeight = hColorGreenHeight - hColorBlueHeight;
    for (int i = hColorBlueHeight; i < hColorGreenHeight; i++) {
      int tmpPos = i - hColorBlueHeight;
      float tmpAmt = ((float) tmpPos) / ((float) tmpHeight);
      hColor[i][0] = (HCOLOR_BLUE[0] * (1 - tmpAmt) + HCOLOR_GREEN[0]
          * (tmpAmt)) / 255.0f;
      hColor[i][1] = (HCOLOR_BLUE[1] * (1 - tmpAmt) + HCOLOR_GREEN[1]
          * (tmpAmt)) / 255.0f;
      hColor[i][2] = (HCOLOR_BLUE[2] * (1 - tmpAmt) + HCOLOR_GREEN[2]
          * (tmpAmt)) / 255.0f;
    }
    tmpHeight = hColorBrownHeight - hColorGreenHeight;
    for (int i = hColorGreenHeight; i < hColorBrownHeight; i++) {
      int tmpPos = i - hColorGreenHeight;
      float tmpAmt = ((float) tmpPos) / ((float) tmpHeight);
      hColor[i][0] = (HCOLOR_GREEN[0] * (1 - tmpAmt) + HCOLOR_BROWN[0]
          * (tmpAmt)) / 255.0f;
      hColor[i][1] = (HCOLOR_GREEN[1] * (1 - tmpAmt) + HCOLOR_BROWN[1]
          * (tmpAmt)) / 255.0f;
      hColor[i][2] = (HCOLOR_GREEN[2] * (1 - tmpAmt) + HCOLOR_BROWN[2]
          * (tmpAmt)) / 255.0f;
    }
    tmpHeight = hColorWhiteHeight - hColorBrownHeight;
    for (int i = hColorBrownHeight; i <= hColorWhiteHeight; i++) {
      int tmpPos = i - hColorBrownHeight;
      float tmpAmt = ((float) tmpPos) / ((float) tmpHeight);
      hColor[i][0] = (HCOLOR_BROWN[0] * (1 - tmpAmt) + HCOLOR_WHITE[0]
          * (tmpAmt)) / 255.0f;
      hColor[i][1] = (HCOLOR_BROWN[1] * (1 - tmpAmt) + HCOLOR_WHITE[1]
          * (tmpAmt)) / 255.0f;
      hColor[i][2] = (HCOLOR_BROWN[2] * (1 - tmpAmt) + HCOLOR_WHITE[2]
          * (tmpAmt)) / 255.0f;
    }
  }

  /*****************************************************************************
   * The reshape method. *
   * ***************************************************************************
   * This method is called at each resize event of the OpenGl component. * * It
   * resize the Viewport of the scene and set the Viewing Volume. *
   ****************************************************************************/
  public void reshape(GLAutoDrawable drawable, int x, int y, int width,
      int height) {
    GL gl = drawable.getGL();
    float h = (float) height / (float) width;

    /*
     * First, we use two objects of GLAnimCanvas : gl ad glu.
     * - gl provides access to OpenGl functionality and graphics card extension.
     */
    System.out.println("Width : " + width + " Height: " + height);
    if (height <= 0)
      height = 1;

    /*
     * Set the current Viewport region.
     * The Viewport is the region of the window in which the scene is drawn.
     *
     * Here: all the space.
     */
    gl.glViewport(0, 0, width, height);

    /*
     * Select the Projection matrix stack.
     */
    gl.glMatrixMode(GL.GL_PROJECTION); // select the projection matrix
    gl.glLoadIdentity(); // reset the current matrix

    glu.gluPerspective(45.0f, h, 0.0001, 20.0);

    /*
     * Select the Modelview matrix stack.
     * This matrix contains the different transformations like the translation, rotation,
     * scale...
     * We only reset the matrix.
     *
     * In the drawing methods, the current matrix is the last selected. So the matrix is
     * the Modelview matrix.
     */
    gl.glMatrixMode(GL.GL_MODELVIEW); //select the Modelview Matrix
    gl.glLoadIdentity(); //set the ModelView matrix to identity
  }

  /*****************************************************************************
   * The init method. *
   * ********************************************************** This is called
   * just after the GL-Context is create * * This method is used to : * -
   * activate some modes like texture, light ... * - affect value to properties * -
   * import testures, load your data ... *
   ****************************************************************************/
  public void init(GLAutoDrawable drawable) {
    GL gl = drawable.getGL();

    System.out.println("INIT GL IS: " + gl.getClass().getName());

    System.out.println("Chosen GLCapabilities: "
        + drawable.getChosenGLCapabilities());

    /*
     * Enables the Smooth color shading mode to create graduate color.
     * The effect can be shown on the Pyramid of the Tutorial 04.
     */
    gl.glShadeModel(GL.GL_SMOOTH);
    /*
     * Defines the clearing color in RGB mode (Red Green Blue).
     * This color is the background color of the scene.
     * Components must be a float number in the interval [0,1].
     * Here: R=0, G=0, B=0 so the color is black.
     * (r=1, G=1, B=1 is the white color)
     *
     * Rem: the last value is the Alpha components (also called RGBA).
     * We should show its effects in the Blending tutorial (09).
     */
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    /*
     * The Depth buffer.
     * This buffer keeps tracks the depth of each points of the scene.
     * This buffer, when an object is drawn, tests if the object
     * is behind or in front of the objects already drawn.
     * GL_LEQUAL test if the current object to be drawn is closer
     * or as the same distance than.
     */
    gl.glClearDepth(1.0); // enable Clearing of the Depth buffer
    gl.glDepthFunc(GL.GL_LEQUAL); // type of Depth test
    gl.glEnable(GL.GL_DEPTH_TEST); // enable Depth testing

    // define the correction done to the perspective calculation (perspective looks a it better)
    gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
  }

  /*****************************************************************************
   * Draw the OpenGl scene. *
   * ****************************************************************** Here we
   * place all the code related to the drawing of the scene. * This method is
   * called by the drawing loop (the display method). *
   ****************************************************************************/
  public void drawGlScene(GLAutoDrawable drawable) {
    GL gl = drawable.getGL();
    /*
     * We begin to clear the color and the depth buffer and reset the view.
     * After, we translate the repere axis to the back. This is used to view
     * the scene : Remember to call something like that !
     */
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); //Clear the buffers

    matrix = map.getMatrix();
    int msizey = matrix.length;
    int msizex = matrix[0].length;
    if (visitedMatrix == null) {
      visitedMatrix = new boolean[msizey][msizex];
    }

    if (fly) {
      // in fly through mode, use different perspective and other view
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluPerspective(45.0f, 1, 0.001f, 20.0f);
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();
      glu.gluLookAt(pt1[0], pt1[1], pt1[2], pt2[0], pt2[1], pt2[2], 0.0f, 0.0f,
          1.0f);
    } else {
      // reset the view and move/rotate to correct position
      gl.glLoadIdentity();
      gl.glTranslatef(xpos, ypos, zpos);
      gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
      gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);
      gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);
    }
    
    Point actualPos = mapViewer.pf.getActualXY();
    int myX = (int)actualPos.getX();
    int myY = (int)actualPos.getY();
    visitedMatrix[myY][myX] = true;

    for (int x = 0; x < msizex - 1; x++) {
      // draw the map surface
      gl.glBegin(GL.GL_QUAD_STRIP);
      for (int y = 0; y < msizey; y++) {
        float diff = 0.2f;
        gl.glColor3f(hColor[matrix[y][x] + 128][0] + (visitedMatrix[y][x] ? + diff:0.0f),
                     hColor[matrix[y][x] + 128][1] + (visitedMatrix[y][x] ? + diff:0.0f), 
                     hColor[matrix[y][x] + 128][2] + (visitedMatrix[y][x] ? + diff:0.0f));
        gl.glVertex3f(((float) x) / (float) msizex - 0.5f, 
                      ((float) y) / (float) msizey - 0.5f, 
                      matrix[y][x] / 500f);
        gl.glColor3f(hColor[matrix[y][x + 1] + 128][0] + (visitedMatrix[y][x] ? + diff:0.0f),
                     hColor[matrix[y][x + 1] + 128][1] + (visitedMatrix[y][x] ? + diff:0.0f),
                     hColor[matrix[y][x + 1] + 128][2] + (visitedMatrix[y][x] ? + diff:0.0f));
        gl.glVertex3f(((float) x + 1) / (float) msizex - 0.5f, 
                      ((float) y) / (float) msizey - 0.5f, 
                      matrix[y][x + 1] / 500f);
      }
      gl.glEnd();
      // draw side part for x = 0
      if (x == 0) {
        gl.glBegin(GL.GL_QUAD_STRIP);
        for (int y = 0; y < msizey; y++) {
          gl.glColor3f(hColor[matrix[y][x] + 128][0],
              hColor[matrix[y][x] + 128][1], hColor[matrix[y][x] + 128][2]);
          gl.glVertex3f(((float) x) / (float) msizex - 0.5f, ((float) y)
              / (float) msizey - 0.5f, matrix[y][x] / 500f);
          gl.glColor3f(hColor[matrix[y][x] + 128][0],
              hColor[matrix[y][x] + 128][1], hColor[matrix[y][x] + 128][2]);
          gl.glVertex3f(((float) x) / (float) msizex - 0.5f, ((float) y)
              / (float) msizey - 0.5f, -0.25f);
        }
        gl.glEnd();
      }
      // draw side part for x = 1
      if (x == msizex - 2) {
        gl.glBegin(GL.GL_QUAD_STRIP);
        for (int y = 0; y < msizey; y++) {
          gl.glColor3f(hColor[matrix[y][x + 1] + 128][0],
              hColor[matrix[y][x + 1] + 128][1],
              hColor[matrix[y][x + 1] + 128][2]);
          gl.glVertex3f(((float) x + 1) / (float) msizex - 0.5f, ((float) y)
              / (float) msizey - 0.5f, matrix[y][x + 1] / 500f);
          gl.glColor3f(hColor[matrix[y][x + 1] + 128][0],
              hColor[matrix[y][x + 1] + 128][1],
              hColor[matrix[y][x + 1] + 128][2]);
          gl.glVertex3f(((float) x + 1) / (float) msizex - 0.5f, ((float) y)
              / (float) msizey - 0.5f, -0.25f);
        }
        gl.glEnd();
      }
    }
    // draw side part for y = 0
    int y = 0;
    gl.glBegin(GL.GL_QUAD_STRIP);
    for (int x = 0; x < msizex; x++) {
      gl.glColor3f(hColor[matrix[y][x] + 128][0],
          hColor[matrix[y][x] + 128][1], hColor[matrix[y][x] + 128][2]);
      gl.glVertex3f(((float) x) / (float) msizex - 0.5f, ((float) y)
          / (float) msizey - 0.5f, matrix[y][x] / 500f);
      gl.glColor3f(hColor[matrix[y][x] + 128][0],
          hColor[matrix[y][x] + 128][1], hColor[matrix[y][x] + 128][2]);
      gl.glVertex3f(((float) x) / (float) msizex - 0.5f, ((float) y)
          / (float) msizey - 0.5f, -0.25f);
    }
    gl.glEnd();
    // draw side part for y = 1
    y = msizey - 1;
    gl.glBegin(GL.GL_QUAD_STRIP);
    for (int x = 0; x < msizex; x++) {
      gl.glColor3f(hColor[matrix[y][x] + 128][0],
          hColor[matrix[y][x] + 128][1], hColor[matrix[y][x] + 128][2]);
      gl.glVertex3f(((float) x) / (float) msizex - 0.5f, ((float) y)
          / (float) msizey - 0.5f, matrix[y][x] / 500f);
      gl.glColor3f(hColor[matrix[y][x] + 128][0],
          hColor[matrix[y][x] + 128][1], hColor[matrix[y][x] + 128][2]);
      gl.glVertex3f(((float) x) / (float) msizex - 0.5f, ((float) y)
          / (float) msizey - 0.5f, -0.25f);
    }
    gl.glEnd();

    // draw bottom part
    gl.glBegin(GL.GL_QUAD_STRIP);
    gl.glColor3f(hColor[0][0], hColor[0][1], hColor[0][2]);
    gl.glVertex3f(((float) 0) / (float) msizey - 0.5f, ((float) 0)
        / (float) msizey - 0.5f, -0.25f);
    gl.glVertex3f(((float) 0) / (float) msizey - 0.5f, ((float) msizey - 1)
        / (float) msizey - 0.5f, -0.25f);
    gl.glVertex3f(((float) msizex - 1) / (float) msizex - 0.5f, -0.5f, -0.25f);
    gl.glVertex3f(((float) msizex - 1) / (float) msizex - 0.5f,
        ((float) msizey - 1) / (float) msizey - 0.5f, -0.25f);
    gl.glEnd();

    // draw path if available
    if (path != null && path.size() > 0) {
      Point pt;
      gl.glLineWidth(3);
      gl.glBegin(GL.GL_LINE_STRIP);


      for (int i = 0; i < path.size(); i++) {
        pt = path.get(i);
        gl.glColor3f(1f, 0f, 0f);
        gl.glVertex3f(((float) pt.x) / (float) msizex - 0.5f, 
                      ((float) pt.y) / (float) msizey - 0.5f, 
                      (matrix[pt.y][pt.x] + 10) / 500f);
      }
      gl.glEnd();
      
      gl.glPushMatrix();
      pt = path.get(0);
      gl.glTranslatef(((float) pt.x) / (float) msizex - 0.5f, ((float) pt.y)
          / (float) msizey - 0.5f, (matrix[pt.y][pt.x] + 10) / 500f);
      gl.glColor3f(1f, 0.9137f, 0.0184f);
      glu.gluSphere(glu.gluNewQuadric(), 0.003f, 32, 16);
      gl.glPopMatrix();

      gl.glPushMatrix();
      pt = path.get(path.size()-1);
      gl.glTranslatef(((float) pt.x) / (float) msizex - 0.5f, ((float) pt.y)
          / (float) msizey - 0.5f, (matrix[pt.y][pt.x] + 10) / 500f);
      gl.glColor3f(0.9451f, 0.5608f, 0.10196f);
      glu.gluSphere(glu.gluNewQuadric(), 0.003f, 32, 16);
      gl.glPopMatrix();
    }
    
    //gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    //gl.glCullFace(GL_BACK);
    //gl.glEnable(GL_CULL_FACE);

    gl.glFlush();
  }

  public void setFly(boolean fly) {
    this.fly = fly;
  }

  public void setLookAt(float[] pt1, float[] pt2) {
    this.pt1 = pt1;
    this.pt2 = pt2;
  }

  /**
   * This method starts or stops the flight through the map.
   */
  public void flyThrough() {
    if (fly) {
      fly = false;
      ftt.stopFly();
      ftt = null;
    } else {
      ftt = new FlyThroughThread(map, path, this);
      ftt.start();
    }
  }

  /**
   * This method is used to update the path through the map.
   * 
   * @param path
   *          ArrayList that represents the path through the map.
   */
  public void updatePath(ArrayList<Point> path) {
    this.path = path;
  }

  /**
   * Display loop, it checks for errors and calls the method to draw the scene.
   */
  public void display(GLAutoDrawable drawable) {
    // draw the scene
    drawGlScene(drawable);
  }

  public void displayChanged(GLAutoDrawable drawable, boolean arg1, boolean arg2) {
    // draw the scene
    drawGlScene(drawable);
  }

  //*******************************************
  // key Events
  public void keyReleased(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      // close application
      case KeyEvent.VK_ESCAPE:
        System.exit(0);
        break;
    }
  }

  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      // change position of the camera
      case KeyEvent.VK_LEFT:
        xpos += OFFSET_X;
        break;
      case KeyEvent.VK_RIGHT:
        xpos -= OFFSET_X;
        break;
      case KeyEvent.VK_UP:
        zpos += ZOOM;
        break;
      case KeyEvent.VK_DOWN:
        zpos -= ZOOM;
        break;
      case KeyEvent.VK_PAGE_UP:
        ypos -= OFFSET_Y;
        break;
      case KeyEvent.VK_PAGE_DOWN:
        ypos += OFFSET_Y;
        break;

      // allow to increase and decrease the fly through speed
      case KeyEvent.VK_I:
        if (ftt != null)
          ftt.increaseSpeed();
        break;
      case KeyEvent.VK_D:
        if (ftt != null)
          ftt.decreaseSpeed();
        break;

      // start fly through
      case KeyEvent.VK_F:
        flyThrough();
        break;
      // print the map
      case KeyEvent.VK_P:
        map.print();
        break;
      // generate new map (and search path again)
      case KeyEvent.VK_R:
        map.createMatrix();
        mapViewer.searchPath();
        break;
    }
  }

  public void keyTyped(KeyEvent ke) {
  }

  //*******************************************
  // mouse events
  public void mouseEntered(MouseEvent me) {
  }

  public void mouseExited(MouseEvent me) {
  }

  public void mouseClicked(MouseEvent me) {
  }

  public void mousePressed(MouseEvent me) {
  }

  public void mouseReleased(MouseEvent me) {
  }

  public void mouseMoved(MouseEvent me) {
  }

  public void mouseDragged(MouseEvent me) {
    if (!fly) {
      if (me.getX() > mouseX) {
        zrot += ROTATION;
      } else if (me.getX() < mouseX) {
        zrot -= ROTATION;
      }
      if (me.getY() > mouseY) {
        xrot += ROTATION;
      } else if (me.getY() < mouseY) {
        xrot -= ROTATION;
      }
      mouseX = me.getX();
      mouseY = me.getY();
    }
  }

  /**
   * Thread that generates the positions for the flight along the path through
   * the map.
   * 
   * @author msuess
   */
  public class FlyThroughThread extends Thread {

    private static final int FPS = 25;

    private int speed;
    private boolean stop;
    private ArrayList<Point> path;
    private MapView mapView;

    /**
     * Constructor of the fly through thread.
     * 
     * @param map
     *          Map to fly through.
     * @param path
     *          Path on which to fly.
     * @param mapView
     *          View in which to draw.
     */
    public FlyThroughThread(Map map, ArrayList<Point> path, MapView mapView) {
      this.path = path;
      this.mapView = mapView;

      speed = 25;
    }

    /**
     * Runs the flight through the map (generates FPS frames per step).
     */
    public void run() {
      if (matrix != null && path != null) {
        mapView.setFly(true);
        int i = 0;
        stop = false;
        int z1, z2;
        int msizey = matrix.length;
        int msizex = matrix[0].length;
        float[] pt1 = new float[3];
        float[] pt2 = new float[3];
        Point tmpPt2, tmpPt3, tmpPt4, tmpPt5;
        while (!stop && i < path.size() - 1) {
          tmpPt2 = i > 0 ? (Point) path.get(i - 1) : (Point) path.get(0);
          tmpPt3 = (Point) path.get(i);
          tmpPt4 = i < path.size() - 9 ? (Point) path.get(i + 9)
              : (Point) path.get(path.size()-1);
          tmpPt5 = i < path.size() - 10 ? (Point) path.get(i + 10)
              : (Point) path.get(path.size()-1);

          // generates roughly <FPS> frames per step
          for (int j = 0; j < FPS; j++) {
            z1 = matrix[tmpPt2.y][tmpPt3.x];
            z2 = matrix[tmpPt2.y][tmpPt3.x];
            pt1[0] = (j * ((tmpPt3.x - tmpPt2.x) / 25f) + tmpPt2.x)
                / (float) msizex - 0.5f;
            pt1[1] = (j * ((tmpPt3.y - tmpPt2.y) / 25f) + tmpPt2.y)
                / (float) msizey - 0.5f;
            pt1[2] = ((j * ((z2 - z1) / 25f) + z1) + 20) / 500f;

            z1 = matrix[tmpPt4.y][tmpPt4.x];
            z2 = matrix[tmpPt5.y][tmpPt5.x];
            pt2[0] = (j * ((tmpPt5.x - tmpPt4.x) / 25f) + tmpPt4.x)
                / (float) msizex - 0.5f;
            pt2[1] = (j * ((tmpPt5.y - tmpPt4.y) / 25f) + tmpPt4.y)
                / (float) msizey - 0.5f;
            pt2[2] = ((j * ((z2 - z1) / 25f) + z1) + 20) / 500f;

            mapView.setLookAt(pt1, pt2);

            try {
              Thread.sleep(1000 / (FPS * (long) Math.ceil(speed / 4)));
            } catch (InterruptedException e) {
            }
          }

          i++;
        }
        mapView.setFly(false);
        mapView.setLookAt(null, null);
      }
    }

    /**
     * Increase fly through speed.
     */
    public void increaseSpeed() {
      speed++;
    }

    /**
     * Decrease fly through speed.
     */
    public void decreaseSpeed() {
      speed = speed > 4 ? speed - 1 : 4;
    }

    /**
     * Stop the flight.
     */
    public void stopFly() {
      stop = true;
    }
  }

}
 
 
