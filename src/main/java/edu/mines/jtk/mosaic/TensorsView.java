/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.mosaic;

import java.awt.*;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.EigenTensors2;
import edu.mines.jtk.dsp.Sampling;
import static edu.mines.jtk.util.ArrayMath.*;

/**
 * A view of a 2D metric tensor field. A metric tensor corresponds to a 
 * symmetric positive-definite 2x2 matrix, and is displayed as an ellipse.
 * Ellipses are drawn for only a uniformly sampled subset of the tensors.
 *
 * @author Dave Hale, Colorado School of Mines
 * @version 2010.08.28
 */
public class TensorsView extends TiledView {

  /**
   * Orientation of sample axes x1 and x2. For example, the default 
   * orientation X1RIGHT_X2UP corresponds to x1 increasing horizontally 
   * from left to right, and x2 increasing vertically from bottom to top.
   */
  public enum Orientation {
    X1RIGHT_X2UP,
    X1DOWN_X2RIGHT
  }

  /**
   * Constructs a view of the specified tensors.
   * @param et the tensors.
   */
  public TensorsView(EigenTensors2 et) {
    this(new Sampling(et.getN1(),1.0,0.0),
         new Sampling(et.getN2(),1.0,0.0),
         et);
  }

  /**
   * Constructs a view of the specified tensors.
   * @param s1 sampling of 1st dimension.
   * @param s2 sampling of 2nd dimension.
   * @param et the tensors.
   */
  public TensorsView(Sampling s1, Sampling s2, EigenTensors2 et) {
    _s1 = s1;
    _s2 = s2;
    updateBestProjectors();
    set(et);
  }

  /**
   * Sets the tensors for this view.
   * @param et the tensors.
   */
  public void set(EigenTensors2 et) {
    _et = et;
    updateTensorEllipses();
  }

  /**
   * Sets the orientation of (x1,x2) axes.
   * @param orientation the orientation.
   */
  public void setOrientation(Orientation orientation) {
    if (_orientation!=orientation) {
      _orientation = orientation;
      updateBestProjectors();
      repaint();
    }
  }

  /**
   * Gets the orientation of (x1,x2) axes.
   * @return the orientation.
   */
  public Orientation getOrientation() {
    return _orientation;
  }

  /**
   * Sets the number of ellipses displayed along the larger dimension.
   * Ellipses are displayed for only a subset of the sampled tensors.
   * The specified number of ellipses roughly equals the number that 
   * will be displayed along the axis with the largest number of tensors.
   * <p>
   * The sizes of the ellipses are chosen so that they never overlap.
   * Therefore, this parameter indirectly determines the size of the 
   * the ellipses drawn. One can display either a large number of small
   * ellipses or a smaller number of larger ellipses.
   * <p>
   * The default number is 20.
   * @param ne the number of ellipses displayed along the larger dimension.
   */
  public void setEllipsesDisplayed(int ne) {
    _ne = ne;
    updateTensorEllipses();
  }

  /**
   * Sets the line width.
   * The default width is zero, for the thinnest lines.
   * @param width the line width.
   */
  public void setLineWidth(float width) {
    if (_lineWidth!=width) {
      _lineWidth = width;
      updateBestProjectors();
      repaint();
    }
  }

  /**
   * Sets the line color.
   * The default line color is the tile foreground color. 
   * That default is used if the specified line color is null.
   * @param color the line color; null, for tile foreground color.
   */
  public void setLineColor(Color color) {
    if (!equalColors(_lineColor,color)) {
      _lineColor = color;
      repaint();
    }
  }

  public void paint(Graphics2D g2d) {
    g2d.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    Projector hp = getHorizontalProjector();
    Projector vp = getVerticalProjector();
    Transcaler ts = getTranscaler();
    ts = ts.combineWith(hp,vp);

    // Line width from graphics context.
    float lineWidth = 1.0f;
    Stroke stroke = g2d.getStroke();
    if (stroke instanceof BasicStroke) {
      BasicStroke bs = (BasicStroke)stroke;
      lineWidth = bs.getLineWidth();
    }

    // Graphics context for polygons.
    Graphics2D gpoly = (Graphics2D)g2d.create();
    float width = lineWidth;
    if (_lineWidth!=0.0f)
      width *= _lineWidth;
    BasicStroke bs = new BasicStroke(width);
    gpoly.setStroke(bs);
    if (_lineColor!=null)
      gpoly.setColor(_lineColor);

    // Draw ellipses as polygons.
    int ne = _x1.length;
    int np = _x1[0].length;
    int[] xp = new int[np];
    int[] yp = new int[np];
    float[][] xv = null;
    float[][] yv = null;
    if (_orientation==Orientation.X1RIGHT_X2UP) {
      xv = _x1;
      yv = _x2;
    } else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
      xv = _x2;
      yv = _x1;
    }
    for (int ie=0; ie<ne; ++ie) {
      float[] xe = xv[ie];
      float[] ye = yv[ie];
      for (int ip=0; ip<np; ++ip) {
        xp[ip] = ts.x(xe[ip]);
        yp[ip] = ts.y(ye[ip]);
      }
      gpoly.drawPolygon(xp,yp,np);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private Sampling _s1; // sampling of x1 axis
  private Sampling _s2; // sampling of x2 axis
  private EigenTensors2 _et; // the tensors
  private int _ne = 20; // number of ellipses along longer axis
  private int _np = 50; // number of points in each ellipse polyline
  private float[][] _x1; // x1 coordinates of points in ellipse polylines
  private float[][] _x2; // x2 coordinates of points in ellipse polylines
  private Orientation _orientation = Orientation.X1RIGHT_X2UP;
  private float _lineWidth = 0.0f;
  private Color _lineColor = null;

  private void updateTensorEllipses() {
    int n1 = _s1.getCount();
    int n2 = _s2.getCount();
    double d1 = _s1.getDelta();
    double d2 = _s2.getDelta();
    double f1 = _s1.getFirst();
    double f2 = _s2.getFirst();

    // Ellipse sampling.
    int np = _np; // number of points in polygon approximating ellipse
    double dp = 2.0*PI/np; // increment in angle for points on ellipse
    double fp = 0.0f; // angle for first point on ellipse
    int ne = min(_ne,n1,n2); // nominal number of ellipses for long axis
    int ns = max(n1/ne,n2/ne); // span in samples of largest ellipse
    int m1 = (n1-1)/ns; // number of ellipses along x1 axis
    int m2 = (n2-1)/ns; // number of ellipses along x2 axis
    int j1 = (n1-1-(m1-1)*ns)/2; // index of first ellipse along x1 axis
    int j2 = (n2-1-(m2-1)*ns)/2; // index of first ellipse along x2 axis
    int nm = m1*m2; // total number of ellipses

    // Maximum eigenvalue.
    float emax = 0.0f;
    float[] a = new float[2];
    for (int i2=0; i2<n2; ++i2) {
      for (int i1=0; i1<n1; ++i1) {
        _et.getEigenvalues(i1,i2,a);
        emax = max(emax,a[0],a[1]);
      }
    }

    // Maximum radius (in samples) of any ellipse.
    // Reduce radius slightly to ensure a gap between ellipses.
    double r = (emax>0.0)?0.48*ns/sqrt(emax):0.0;

    // Ellipse (x1,x2) coordinates.
    _x1 = new float[nm][np];
    _x2 = new float[nm][np];
    float[] u = new float[2];
    for (int i2=j2,k2=0,im=0; i2<n2 && k2<m2; i2+=ns,++k2) {
      for (int i1=j1,k1=0; i1<n1 && k1<m1; i1+=ns,++k1,++im) {
        _et.getEigenvalues(i1,i2,a);
        _et.getEigenvectorU(i1,i2,u);
        double u1 = u[0];
        double u2 = u[1];
        double v1 = -u2;
        double v2 =  u1;
        double du = a[0];
        double dv = a[1];
        double ru = r*sqrt(du);
        double rv = r*sqrt(dv);
        for (int ip=0; ip<np; ++ip) {
          double p = fp+ip*dp;
          double cosp = cos(p);
          double sinp = sin(p);
          _x1[im][ip] = (float)(f1+d1*(i1+ru*cosp*u1-rv*sinp*u2));
          _x2[im][ip] = (float)(f2+d2*(i2+rv*sinp*u1+ru*cosp*u2));
        }
      }
    }
  }

  private void updateBestProjectors() {
    int n1 = _s1.getCount();
    int n2 = _s2.getCount();
    double d1 = _s1.getDelta();
    double d2 = _s2.getDelta();
    double f1 = _s1.getFirst();
    double f2 = _s2.getFirst();

    // (x0,y0) = sample coordinates for (left,top) of view.
    // (x1,y1) = sample coordinates for (right,bottom) of view.
    // (ux0,uy0) = normalized coordinates for (left,top) of view.
    // (ux1,uy1) = normalized coordinates for (right,bottom) of view.
    double x0,y0,x1,y1,ux0,uy0,ux1,uy1;
    if (_orientation==Orientation.X1DOWN_X2RIGHT) {
      x0 = f2;
      x1 = f2+d2*(n2-1);
      y0 = f1;
      y1 = f1+d1*(n1-1);
      ux0 = 0.5/n2;
      ux1 = 1.0-0.5/n2;
      uy0 = 0.5/n1;
      uy1 = 1.0-0.5/n1;
    } else { // if (_orientation==Orientation.X1RIGHT_X2UP)
      x0 = f1;
      x1 = f1+d1*(n1-1);
      y0 = f2+d2*(n2-1);
      y1 = f2;
      ux0 = 0.5/n1;
      ux1 = 1.0-0.5/n1;
      uy0 = 0.5/n2;
      uy1 = 1.0-0.5/n2;
    }

    // Best projectors.
    Projector bhp = new Projector(x0,x1,ux0,ux1);
    Projector bvp = new Projector(y0,y1,uy0,uy1);
    setBestProjectors(bhp,bvp);
  }

  private boolean equalColors(Color ca, Color cb) {
    return (ca==null)?cb==null:ca.equals(cb);
  }
}