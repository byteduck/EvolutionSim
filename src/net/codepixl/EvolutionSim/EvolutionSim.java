package net.codepixl.EvolutionSim;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
	int cCreature = 0;
	GameObject floor;
	GameObject marker;
	static double deltaTime = 0;
	long ltime = 0;
	double timer = 0;
	Creature mainCreature, prevCreature;
	double timeMultiplier = 1;
	int times = 0;
	
	public static void main(String[] args){
		new EvolutionSim();
	}
	
	public EvolutionSim(){
		JFrame frame = new JFrame("Evolution Simulator");
		this.setPreferredSize(new Dimension(1000,1000));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(this);
		frame.pack();
		frame.setLocationRelativeTo(null);
		world = new World();
		
		mainCreature = new Creature(this);
		addCreature(mainCreature);
		cGen.add(mainCreature);
		for(int i = 1; i < 10; i++)
			cGen.add(new Creature(this));
		
		floor = new GameObject();
		floor.addFixture(new Rectangle(23,1));
		floor.setMass(MassType.INFINITE);
		floor.translate(23d/2d, -22);
		addObject(floor);
		
		marker = new GameObject();
		marker.addFixture(new Rectangle(1,1));
		marker.translate(23/2d, -10);
		addObject(marker);

		frame.setVisible(true);
		
		world.addListener(new StepListener(){
			@Override
			public void begin(Step arg0, World arg1) {
				
			}
			@Override
			public void end(Step arg0, World arg1) {
				
			}
			@Override
			public void postSolve(Step arg0, World arg1){
				update();
				
			}
			@Override
			public void updatePerformed(Step arg0, World arg1) {
				// TODO Auto-generated method stub
			}
		});
		
		while(true){
			times = 0;
			ltime = System.nanoTime();
			
			mainCreature.focus(frame);
			
			world.update(deltaTime);
			
			repaint();
			
			deltaTime = ((System.nanoTime()-ltime)/1000000000d)*timeMultiplier;
		}
	}
	
	private void update(){
		double rDelta = deltaTime;
		deltaTime = 1d/60d;
		timeMultiplier = 1;
		for(int i = 0; i < mainCreature.muscles.length; i++)
			mainCreature.muscles[i].update();
		floor.getTransform().setTranslation(mainCreature.getPos().x, -22);
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
				Collections.sort(cGen);
				System.out.println("Best: "+cGen.get(0).maxDistance+" Worst: "+cGen.get(cGen.size()-1).maxDistance);
				for(int i = cGen.size()/2; i < cGen.size(); i++)
					cGen.set(i, new Creature(cGen.get(i-cGen.size()/2)));
				for(int i = 0; i < cGen.size()/2; i++)
					cGen.set(i, new Creature(cGen.get(i), false));
				cCreature = 1;
				mainCreature = cGen.get(0);
				addCreature(mainCreature);
				timeMultiplier = 1;
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
		for(GameObject g : c.gameObjects)
			addObject(g);
		for(Muscle j : c.muscles)
			addJoint(j);
		creatures.add(c);
	}
	
	public void unloadCreature(Creature c){
		for(GameObject g : c.gameObjects)
			removeObject(g);
		for(Muscle j : c.muscles)
			removeJoint(j);
		creatures.remove(c);
	}
	
	@Override
	public void paintComponent(Graphics a){
		super.paintComponent(a);
		Graphics2D g = (Graphics2D)a;
		a.drawString("position: "+Math.round(mainCreature.getPos().x*10)/10d+","+Math.round(mainCreature.getPos().y*10)/10d+" timer: "+Math.round(timer)+" id: "+mainCreature.id+" creature: "+cCreature, 0, 10);
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		Graphics2DRenderer.applyTranslations(g);
		g.transform(yFlip);
		for(int i = 0; i < joints.size(); i++)
			renderJoint(joints.get(i),g);
		for(int i = 0; i < gameObjects.size(); i++)
			gameObjects.get(i).render(g);
	}

	public void renderJoint(Joint j, Graphics2D g){
		if(j instanceof Muscle)
			((Muscle) j).render(g);
	}
}
