package net.codepixl.EvolutionSim;

/**
 * 
 * LibSparkline : a free Java sparkline chart library
 * 
 *
 * Project Info:  http://reporting.pentaho.org/libsparkline/
 *
 * (C) Copyright 2008, by Larry Ogrodnek, Pentaho Corporation and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the Apache License 2.0.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the Apache License 2.0 along with this library;
 * if not, a online version is available at http://www.apache.org/licenses/
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------
 * LineGraphDrawable.java
 * ------------
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * A very fast and very simple line-graph drawable. This code is based on the
 * LineGraph class writen by Larry Ogrodnek but instead of producing a
 * low-resolution image, this class writes the content into a Graphics2D
 * context.
 * 
 * @author Thomas Morgner
 */
public class LineGraphDrawable {
  private static final int DEFAULT_SPACING = 2;

  private int spacing;

  private Color color;

  private Color background;

  private Number[] data;

  /**
   * Creates a default bargraph drawable with some sensible default colors and
   * spacings.
   */
  public LineGraphDrawable() {
    this.color = Color.black;
    this.spacing = DEFAULT_SPACING;
  }

  /**
   * Returns the numeric data for the drawable or null, if the drawable has no
   * data.
   * 
   * @return the data.
   */
  public Number[] getData() {
    return data;
  }

  /**
   * Defines the numeric data for the drawable or null, if the drawable has no
   * data.
   * 
   * @param data
   *          the data (can be null).
   */
  public void setData(final Number[] data) {
    this.data = data;
  }

  /**
   * Returns the main color for the bars.
   * 
   * @return the main color for the bars, never null.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Defines the main color for the bars.
   * 
   * @param color
   *          the main color for the bars, never null.
   */
  public void setColor(final Color color) {
    if (color == null) {
      throw new NullPointerException();
    }
    this.color = color;
  }

  /**
   * Returns the color for the background of the graph. This property can be
   * null, in which case the bar will have a transparent background.
   * 
   * @return color for the background or null, if the graph has a transparent
   *         background color.
   */
  public Color getBackground() {
    return background;
  }

  /**
   * Defines the color for the background of the graph. This property can be
   * null, in which case the bar will have a transparent background.
   * 
   * @param background
   *          the background or null, if the graph has a transparent background
   *          color.
   */
  public void setBackground(final Color background) {
    this.background = background;
  }

  /**
   * Returns the spacing between the bars.
   * 
   * @return the spacing between the bars.
   */
  public int getSpacing() {
    return spacing;
  }

  /**
   * Defines the spacing between the bars.
   * 
   * @param spacing
   *          the spacing between the bars.
   */
  public void setSpacing(final int spacing) {
    this.spacing = spacing;
  }

  /**
   * Draws the bar-graph into the given Graphics2D context in the given area.
   * This method will not draw a graph if the data given is null or empty.
   * 
   * @param graphics
   *          the graphics context on which the bargraph should be rendered.
   * @param drawArea
   *          the area on which the bargraph should be drawn.
   */
  public void draw(final Graphics2D graphics, final Rectangle2D drawArea) {
    if (graphics == null) {
      throw new NullPointerException();
    }
    if (drawArea == null) {
      throw new NullPointerException();
    }

    final int height = (int) drawArea.getHeight();
    if (height <= 0) {
      return;
    }

    final Graphics2D g2 = (Graphics2D) graphics.create();
    if (background != null) {
      g2.setPaint(background);
      g2.draw(drawArea);
    }

    if (data == null || data.length == 0) {
      g2.dispose();
      return;
    }

    g2.translate(drawArea.getX(), drawArea.getY());

    float d = getDivisor(data, height);
    final int spacing = getSpacing();
    final int w = (((int) drawArea.getWidth()) - (spacing * (data.length - 1))) / (data.length - 1);

    float min = Float.MAX_VALUE;
    for (int index = 0; index < data.length; index++) {
      Number i = data[index];
      if (i == null) {
        continue;
      }
      final float value = i.floatValue();
      if (value < min) {
        min = value;
      }
    }

    int x = 0;
    int y = -1;

    if (d == 0.0) {
      // special case -- a horizontal straight line
      d = 1.0f;
      y = -height / 2;
    }

    final Line2D.Double line = new Line2D.Double();
    for (int i = 0; i < data.length - 1; i++) {
      final int px1 = x;
      x += (w + spacing);
      final int px2 = x;

      g2.setPaint(color);

      final Number number = data[i];
      final Number nextNumber = data[i + 1];
      if (number == null && nextNumber == null) {
        final float delta = height - ((0 - min) / d);
        line.setLine(px1, y + delta, px2, y + delta);
      } else if (number == null) {
        line.setLine(px1, y + (height - ((0 - min) / d)), px2, y
            + (height - ((nextNumber.floatValue() - min) / d)));
      } else if (nextNumber == null) {
        line.setLine(px1, y + (height - ((number.floatValue() - min) / d)), px2, y
            + (height - ((0 - min) / d)));
      } else {
        line.setLine(px1, y + (height - ((number.floatValue() - min) / d)), px2, y
            + (height - ((nextNumber.floatValue() - min) / d)));
      }
      g2.draw(line);

    }

    g2.dispose();

  }

  /**
   * Computes the scale factor to scale the given numeric data into the target
   * height.
   * 
   * @param data
   *          the numeric data.
   * @param height
   *          the target height of the graph.
   * @return the scale factor.
   */
  public static float getDivisor(final Number[] data, final int height) {
    if (data == null) {
      throw new NullPointerException("Data array must not be null.");
    }

    if (height < 1) {
      throw new IndexOutOfBoundsException("Height must be greater or equal to 1");
    }

    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;

    for (int index = 0; index < data.length; index++) {
      Number i = data[index];
      if (i == null) {
        continue;
      }

      final float numValue = i.floatValue();
      if (numValue < min) {
        min = numValue;
      }
      if (numValue > max) {
        max = numValue;
      }
    }

    if (max <= min) {
      return 1.0f;
    }
    if (height == 1) {
      return 0;
    }
    return (max - min) / (height - 1);
  }
}

   