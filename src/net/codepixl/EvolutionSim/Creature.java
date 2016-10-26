package net.codepixl.EvolutionSim;

import java.awt.Color;
import java.util.Comparator;
import java.util.Random;

import javax.swing.JFrame;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class Creature implements Comparable{
	
	public GameObject[] gameObjects;
	public Muscle[] muscles;
	public World world;
	public EvolutionSim sim;
	public int id;
	public double maxDistance;
	
	public static int cID = 0;
	
	public Creature(EvolutionSim sim){
		this.sim = sim;
		this.id = cID;
		cID++;
		Random r = new Random();
		gameObjects = new GameObject[3];
		muscles = new Muscle[3];
		
		GameObject a = new GameObject();
		BodyFixture fix = new BodyFixture(new Rectangle(0.5,0.5));
		double frict = r.nextDouble()*2+1;
		fix.setFriction(frict);
		a.color = new Color((int) ((frict-1)/2d*255),0,0);
		a.addFixture(fix);
		a.getTransform().setTranslation(0, -21);
		a.setMass(MassType.NORMAL);
		gameObjects[0] = a;
		
		GameObject b = new GameObject();
		fix = new BodyFixture(new Rectangle(0.5,0.5));
		frict = r.nextDouble()*2+1;
		fix.setFriction(frict);
		b.color = new Color((int) ((frict-1)/2d*255),0,0);
		b.addFixture(fix);
		b.getTransform().setTranslation(2, -21);
		b.setMass(MassType.NORMAL);
		gameObjects[1] = b;
		
		GameObject c = new GameObject();
		fix = new BodyFixture(new Rectangle(0.5,0.5));
		frict = r.nextDouble()*2+1;
		fix.setFriction(frict);
		c.color = new Color((int) ((frict-1)/2d*255),0,0);
		c.addFixture(fix);
		c.getTransform().setTranslation(1, -20);
		c.setMass(MassType.NORMAL);
		gameObjects[2] = c;
		
		Muscle ja = new Muscle(a,b);
		ja.setFrequency(4);
		ja.setDampingRatio(0.1);
		ja.setDistance(2);
		muscles[0] = ja;
		
		Muscle jb = new Muscle(b,c);
		jb.setFrequency(4);
		jb.setDampingRatio(0.1);
		jb.setDistance(2);
		muscles[1] = jb;
		
		Muscle jc = new Muscle(c,a);
		jc.setFrequency(4);
		jc.setDampingRatio(0.1);
		jc.setDistance(2);
		muscles[2] = jc;
	}
	
	public Creature(Creature mutateFrom){
		Random r = new Random();
		this.id = cID;
		cID++;
		gameObjects = new GameObject[mutateFrom.gameObjects.length];
		muscles = new Muscle[mutateFrom.muscles.length];
		for(int i = 0; i < gameObjects.length; i++){
			gameObjects[i] = new GameObject();
			GameObject g = gameObjects[i];
			GameObject mt = mutateFrom.gameObjects[i];
			BodyFixture f = new BodyFixture(new Rectangle(0.5,0.5));
			g.addFixture(f);
			
			switch(i){
				case 0:
					g.getTransform().setTranslation(0, -21);
					break;
				case 1:
					g.getTransform().setTranslation(2, -21);
					break;
				case 2:
					g.getTransform().setTranslation(1, -20);
					break;
			}
			
			g.setMass(MassType.NORMAL);
			
			//Mutate friction 10% chance
			int rand = r.nextInt(10);
			double frict = mutateFrom.gameObjects[i].getFixture(0).getFriction();
			if(rand == 0)
				frict+=r.nextDouble()-0.5;
			if(frict < 1)
				frict = 1;
			if(frict > 3)
				g.color = new Color(255,0,0);
			else
				g.color = new Color((int) ((frict-1)/2d*255),0,0);
			f.setFriction(frict);
		}
		muscles[0] = new Muscle(gameObjects[0],gameObjects[1],mutateFrom.muscles[0]);
		muscles[1] = new Muscle(gameObjects[1],gameObjects[2],mutateFrom.muscles[1]);
		muscles[2] = new Muscle(gameObjects[2],gameObjects[0],mutateFrom.muscles[2]);
	}
	
	public Creature(Creature duplicate, boolean b) {
		this.id = cID;
		cID++;
		gameObjects = new GameObject[duplicate.gameObjects.length];
		muscles = new Muscle[duplicate.muscles.length];
		for(int i = 0; i < gameObjects.length; i++){
			gameObjects[i] = new GameObject();
			GameObject g = gameObjects[i];
			BodyFixture f = new BodyFixture(new Rectangle(0.5,0.5));
			g.addFixture(f);
			
			switch(i){
				case 0:
					g.getTransform().setTranslation(0, -21);
					break;
				case 1:
					g.getTransform().setTranslation(2, -21);
					break;
				case 2:
					g.getTransform().setTranslation(1, -20);
					break;
			}
			
			g.setMass(MassType.NORMAL);
			
			double frict = duplicate.gameObjects[i].getFixture(0).getFriction();
			g.color = new Color((int) ((frict-1)/2d*255),0,0);
			f.setFriction(frict);
		}
		for(GameObject g: gameObjects)
			System.out.println(g.getFixture(0).getFriction());
		muscles[0] = new Muscle(gameObjects[0],gameObjects[1],duplicate.muscles[0], false);
		muscles[1] = new Muscle(gameObjects[1],gameObjects[2],duplicate.muscles[1], false);
		muscles[2] = new Muscle(gameObjects[2],gameObjects[0],duplicate.muscles[2], false);
	}

	public void reset(){
		this.maxDistance = this.getPos().x;
		gameObjects[0].getTransform().setTranslation(0, -21);
		gameObjects[1].getTransform().setTranslation(2, -21);
		gameObjects[2].getTransform().setTranslation(1, -20);
	}

	public void focus(JFrame frame) {
		Vector2 pos = getPos();
		Graphics2DRenderer.xTranslate = -pos.x+(frame.getWidth()/GameObject.SCALE/2);
		Graphics2DRenderer.yTranslate = pos.y+(frame.getWidth()/GameObject.SCALE/2);
	}
	
	public Vector2 getPos(){
		Vector2 ret = new Vector2();
		for(GameObject g : gameObjects)
			ret = ret.add(g.getTransform().getTranslation());
		return new Vector2(ret.x/gameObjects.length, ret.y/gameObjects.length);
	}
	
	public void update(){
		for(GameObject g : gameObjects)
			g.getTransform().setRotation(0);
	}

	@Override
	public int compareTo(Object o){
		if(o instanceof Creature)
			return new Double(((Creature)o).maxDistance).compareTo(maxDistance);
		else
			return -1;
	}
	
}
