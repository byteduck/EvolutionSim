package net.codepixl.EvolutionSim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphFrame extends JFrame {
	ArrayList<InfoPoint> history;

	public GraphFrame(ArrayList<InfoPoint> history) {
		this.history = history;
		this.add(new GraphPanel());
		this.pack();
		this.setLocationRelativeTo(null);
		this.setLocation(this.getX() + 350, this.getY());
		this.setTitle("Data");
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	private class GraphPanel extends JPanel {

		private int max = 0;
		private int borderGap = 30;
		private final Color graphColor = Color.green;
		private final Color pointColor = new Color(150, 50, 50);
		private final Stroke strokeColor = new BasicStroke(3f);
		private int pointWidth = 12;
		private int yTickCount = 10;
		
		public GraphPanel() {
			this.setPreferredSize(new Dimension(700, 700));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (history.size() > 1) {
				max = 0;
				for(InfoPoint p : history)
					if(Math.round(p.best+1) > max)
						max = (int) Math.round(p.best+1);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				double xScale = ((double) getWidth() - 2 * borderGap) / (history.size() - 1);
				double yScale = ((double) getHeight() - 2 * borderGap) / (max - 1);

				List<Point> graphPoints = new ArrayList<Point>();
				for (int i = 0; i < history.size(); i++) {
					int x1 = (int) (i * xScale + borderGap);
					int y1 = (int) ((max - history.get(i).best) * yScale + borderGap);
					graphPoints.add(new Point(x1, y1));
				}

				// create x and y axes
				g2.drawLine(borderGap, getHeight() - borderGap, borderGap, borderGap);
				g2.drawLine(borderGap, getHeight() - borderGap, getWidth() - borderGap, getHeight() - borderGap);

				// create hatch marks for y axis.
				for (int i = 0; i < yTickCount; i++) {
					int x0 = borderGap;
					int x1 = pointWidth + borderGap;
					int y0 = getHeight() - (((i + 1) * (getHeight() - borderGap * 2)) / yTickCount + borderGap);
					int y1 = y0;
					g2.drawLine(x0, y0, x1, y1);
				}

				// and for x axis
				for (int i = 0; i < history.size() - 1; i++) {
					int x0 = (i + 1) * (getWidth() - borderGap * 2) / (history.size() - 1) + borderGap;
					int x1 = x0;
					int y0 = getHeight() - borderGap;
					int y1 = y0 - pointWidth;
					g2.drawLine(x0, y0, x1, y1);
				}

				Stroke oldStroke = g2.getStroke();
				g2.setColor(graphColor);
				g2.setStroke(strokeColor);
				for (int i = 0; i < graphPoints.size() - 1; i++) {
					int x1 = graphPoints.get(i).x;
					int y1 = graphPoints.get(i).y;
					int x2 = graphPoints.get(i + 1).x;
					int y2 = graphPoints.get(i + 1).y;
					g2.drawLine(x1, y1, x2, y2);
				}

				g2.setStroke(oldStroke);
				g2.setColor(pointColor);
				for (int i = 0; i < graphPoints.size(); i++) {
					int x = graphPoints.get(i).x - pointWidth / 2;
					int y = graphPoints.get(i).y - pointWidth / 2;
					;
					int ovalW = pointWidth;
					int ovalH = pointWidth;
					g2.fillOval(x, y, ovalW, ovalH);
				}
			}else{
				g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
				g.drawString("Not enough data yet :(", this.getWidth() / 2 - g.getFontMetrics().stringWidth("Not enough data yet  :(") / 2, this.getHeight() / 2 + 25);
			}
		}

	}
}
