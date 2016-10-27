package net.codepixl.EvolutionSim;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JFrame;

public class GraphFrame extends JFrame{
	ArrayList<InfoPoint> history;
	public GraphFrame(ArrayList<InfoPoint> history){
		this.history = history;
		this.setSize(700, 700);
		this.setLocationRelativeTo(null);
		this.setLocation(this.getX()+350, this.getY());
		this.setTitle("Data");
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	@Override
	public void paint(Graphics a){
		super.paint(a);
		Graphics2D g = (Graphics2D)a;
		if(history.size() > 1){
			/*double min = history.get(0).worst;
			double max = history.get(0).best;
			for(InfoPoint i : history){
				if(i.worst < min) min = i.worst;
				if(i.best > max) max = i.best;
			}
			double scale = this.getWidth()/(max-min);
			int i = 1;
			for(InfoPoint p : history){
				g.setColor(Color.RED);
				g.drawRect(i*10, (int) (p.best*scale), 10, 10);
				i++;
			}*/
			Number[] data = new Number[history.size()];
			for(int i = 0; i < history.size(); i++)
				data[i] = history.get(i).best;
			LineGraphDrawable lg = new LineGraphDrawable();
			lg.setData(data);
			lg.draw(g, new Rectangle2D.Double(0,0,this.getWidth(),this.getHeight()));
		}else{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 50)); 
			g.drawString("Not enough data yet :(", this.getWidth()/2-g.getFontMetrics().stringWidth("Not enough data yet  :(")/2, this.getHeight()/2+25);
		}
		
	}
}
