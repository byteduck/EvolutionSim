package net.codepixl.EvolutionSim;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

public class EvolutionSim extends JPanel{
	World world;
	ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	ArrayList<Joint> joints = new ArrayList<Joint>();
	ArrayList<Creature> creatures = new ArrayList<Creature>();
	ArrayList<Creature> cGen = new ArrayList<Creature>();
	ArrayList<InfoPoint> history = new ArrayList<InfoPoint>();
	int cCreature = 0;
	GameObject floor;
	static double deltaTime = 0;
	long ltime = 0;
	double timer = 0;
	Creature mainCreature, prevCreature;
	double timeMultiplier = 1;
	public JFrame frame;
	JLabel genLabel;
	JLabel infoLabel;
	int generation = 1;
	double bestDist = 0;
	JFrame graphFrame;
	boolean paused = false;
	
	public static void main(String[] args){
		new EvolutionSim();
	}
	
	/*
	 * This is very messy code. All that's happening here is setting up the JFrame and the gameloop.
	 */
	public EvolutionSim(){
		frame = new JFrame("Evolution Simulator");
		this.setPreferredSize(new Dimension(700,700));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setLocation(frame.getX()-350, frame.getY());
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem saveCreatures = new JMenuItem("Save creatures");
		saveCreatures.addActionListener((ActionEvent evt)->{
			paused = true;
			try{
				FileOutputStream fos = new FileOutputStream("creatures.txt");
				fos.write((""+cGen.size()).getBytes());
				for(Creature c : cGen)
					fos.write(("\n"+Nucleus.toDNA(c.getInfo().getBytes())).getBytes());
				fos.close();
			}catch (IOException e){
				e.printStackTrace();
			}
			paused = false;
		});
		fileMenu.add(saveCreatures);
		JMenuItem loadCreatures = new JMenuItem("Load creatures");
		loadCreatures.addActionListener((ActionEvent e)->{
			resetSim(false);
			try {
				loadCreatures("creatures.txt");
				paused = false;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		fileMenu.add(loadCreatures);
		menuBar.add(fileMenu);
		
		JMenu simMenu = new JMenu("Simulation");
		JMenuItem resetSim = new JMenuItem("Reset");
		resetSim.addActionListener((ActionEvent e)->{
			resetSim(true);
		});
		simMenu.add(resetSim);
		menuBar.add(simMenu);
		
		frame.setJMenuBar(menuBar);
		
		graphFrame = new GraphFrame(history);
		
		JPanel controls = new JPanel();
		JSlider slider = new JSlider(0,11,0);
		slider.setMajorTickSpacing(1);
		slider.setPaintTicks(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for(int i = 1; i < 12; i+=2)
			labelTable.put(i, new JLabel("x"+(int)(Math.pow(2, i))));
		
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		slider.addChangeListener((ChangeEvent e)->{
			timeMultiplier = Math.pow(2, slider.getValue());
			world.setAccumulatedTime(0);
		});
		controls.setLayout(new BorderLayout());
		controls.add(slider, BorderLayout.CENTER);
		genLabel = new JLabel("<html><center>Generation 0000<br>Creature 00</center></html>");
		Border border = genLabel.getBorder();
		Border margin = new EmptyBorder(10,10,10,10);
		genLabel.setBorder(new CompoundBorder(border, margin));
		controls.add(genLabel, BorderLayout.WEST);
		infoLabel = new JLabel("<html><center>Timer: 00<br>Distance: +000.0</center></html>");
		border = infoLabel.getBorder();
		margin = new EmptyBorder(10,10,10,10);
		infoLabel.setBorder(new CompoundBorder(border, margin));
		controls.add(infoLabel, BorderLayout.EAST);
		frame.add(controls, BorderLayout.SOUTH);
		
		resetSim(true);
		
		frame.setVisible(true);
		double aDelta = 0;
		while(true){
			if(!paused){
				ltime = System.nanoTime();
				
				repaint();
				mainCreature.focus(frame);
				world.update(deltaTime,1);
				
				deltaTime = aDelta*timeMultiplier;
				aDelta = ((System.nanoTime()-ltime)/1000000000d);
			}else{
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void loadCreatures(String fileName) throws IOException{
		Scanner s = new Scanner(new File(fileName));
		int numCreatures = Integer.parseInt(s.nextLine());
		for(int i = 0; i < numCreatures; i++)
			cGen.add(new Creature(new String(Nucleus.fromDNA(s.nextLine()))));
		mainCreature = cGen.get(0);
		addCreature(mainCreature);
		s.close();
	}

	public void resetSim(boolean createCreatures){
		paused = true;
		
		cGen.clear();
		creatures.clear();
		history.clear();
		gameObjects.clear();
		joints.clear();
		cCreature = 0;
		generation = 1;
		bestDist = 0;
		
		world = new World();
		
		if(createCreatures){
			mainCreature = new Creature();
			addCreature(mainCreature);
			for(int i = 2; i < 20; i++)
				cGen.add(new Creature());
		}else{
			mainCreature = null;
		}
		
		floor = new GameObject();
		floor.addFixture(new Rectangle(23,1));
		floor.setMass(MassType.INFINITE);
		floor.translate(23d/2d, -22);
		addObject(floor);
		
		world.addListener(new StepListener(){
			@Override
			public void begin(Step arg0, World arg1) {}
			@Override
			public void end(Step arg0, World arg1) {}
			@Override
			public void postSolve(Step arg0, World arg1){update();}
			@Override
			public void updatePerformed(Step arg0, World arg1) {}
		});
		
		graphFrame.repaint();
		
		if(createCreatures)
			paused = false;
	}
	
	private void update(){
		floor.getTransform().setTranslation(mainCreature.getPos().x, -22);
		double rDelta = deltaTime;
		deltaTime = world.getSettings().getStepFrequency();
		for(int i = 0; i < mainCreature.muscles.length; i++)
			mainCreature.muscles[i].update();
		for(int i = 0; i < creatures.size(); i++)
			creatures.get(i).update();
		
		timer+=deltaTime;
		if(timer > 15){
			unloadCreature(mainCreature);
			mainCreature.reset();
			cCreature++;
			if(cCreature < cGen.size()){
				mainCreature = cGen.get(cCreature);
				addCreature(mainCreature);
			}else{
				history.add(new InfoPoint(cGen));
				graphFrame.repaint();
				Collections.sort(cGen);
				System.out.println("Best: "+cGen.get(0).maxDistance+" Worst: "+cGen.get(cGen.size()-1).maxDistance);
				bestDist = Math.round(cGen.get(0).maxDistance*10)/10d;
				for(int i = cGen.size()/2; i < cGen.size(); i++)
					cGen.set(i, new Creature(cGen.get(i-cGen.size()/2)));
				for(int i = 0; i < cGen.size()/2; i++)
					cGen.set(i, new Creature(cGen.get(i), false));
				cCreature = 0;
				mainCreature = cGen.get(0);
				addCreature(mainCreature);
				generation++;
			}
			timer = 0;
		}
		deltaTime = rDelta;
	}

	public void addObject(GameObject g){
		gameObjects.add(g);
		world.addBody(g);
	}
	
	public void addJoint(Joint j){
		joints.add(j);
		world.addJoint(j);
	}
	
	public void removeObject(GameObject g){
		gameObjects.remove(g);
		world.removeBody(g);
	}
	
	public void removeJoint(Joint j){
		joints.remove(j);
		world.removeJoint(j);
	}
	
	public void addCreature(Creature c){
		for(GameObject g : c.nodes)
			addObject(g);
		for(Muscle j : c.muscles)
			addJoint(j);
		creatures.add(c);
	}
	
	public void unloadCreature(Creature c){
		for(GameObject g : c.nodes)
			removeObject(g);
		for(Muscle j : c.muscles)
			removeJoint(j);
		creatures.remove(c);
	}
	
	@Override
	public void paintComponent(Graphics a){
		super.paintComponent(a);
		Graphics2D g = (Graphics2D)a;
		//a.drawString("position: "+Math.round(mainCreature.getPos().x*10)/10d+","+Math.round(mainCreature.getPos().y*10)/10d+" timer: "+Math.round(timer)+" id: "+mainCreature.id+" creature: "+cCreature, 0, 10);
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		Graphics2DRenderer.applyTranslations(g);
		drawGrid(g);
		g.transform(yFlip);
		try{
			for(int i = 0; i < joints.size(); i++)
				renderJoint(joints.get(i),g);
			for(int i = 0; i < gameObjects.size(); i++)
				gameObjects.get(i).render(g);
			infoLabel.setText("<html><center>Timer: "+Math.round(timer)+"<br>Distance: "+Math.round(mainCreature.getPos().x*10)/10d+"</center></html>");
			genLabel.setText("<html><center>Generation "+generation+"<br>Creature: "+(cCreature+1)+"<br>Last gen best: "+bestDist+"</center></html>");
		}catch(NullPointerException e){
			//Since paint is run on the AWT thread, nullpointerexceptions will be thrown when in the middle of changing the creatures around.
		}
	}

	private void drawGrid(Graphics2D g) {
		for(int x = (int) (-40*GameObject.SCALE); x < 200*GameObject.SCALE; x+=GameObject.SCALE){
			for(int y = (int) (-10*GameObject.SCALE); y < 30*GameObject.SCALE; y+=GameObject.SCALE){
				g.drawRect(x, y, (int)GameObject.SCALE, (int)GameObject.SCALE);
			}
		}
	}

	public void renderJoint(Joint j, Graphics2D g){
		if(j instanceof Muscle)
			((Muscle) j).render(g);
	}
}
