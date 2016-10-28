package net.codepixl.EvolutionSim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class Creature implements Comparable{
	
	public GameObject[] nodes;
	public Muscle[] muscles;
	public World world;
	public EvolutionSim sim;
	public int id;
	public double maxDistance;
	
	public static int cID = 0;
	
	public int numNodes = new Random().nextInt(2)+3;

	public Vector2[] oPos;
	
	public Creature(EvolutionSim sim){
		this.sim = sim;
		this.id = cID;
		cID++;
		Random r = new Random();
		nodes = new GameObject[numNodes];
		muscles = new Muscle[numNodes+numNodes*(numNodes-3)/2];
		oPos = new Vector2[numNodes];
		
		for(int i = 0; i < nodes.length; i++){
			GameObject g = new GameObject();
			BodyFixture fix = new BodyFixture(new Rectangle(0.5,0.5));
			double frict = r.nextDouble()*10;
			fix.setFriction(frict);
			g.color = new Color((int) ((frict)/10d*255),0,0);
			g.addFixture(fix);
			Vector2 trans = new Vector2(r.nextDouble()*2-1, (r.nextDouble()*2-1)-19.5);
			g.getTransform().setTranslation(trans);
			oPos[i] = trans;
			g.setMass(MassType.NORMAL);
			nodes[i] = g;
		}
		int i = 0;
		ArrayList<NodeSet> nodesDone = new ArrayList<NodeSet>();
		for(GameObject n : nodes)
			for(GameObject on: nodes){
				if(on != n){
					if(!nodesDone.contains(new NodeSet(n,on))){
						Muscle m = new Muscle(n, on);
						m.setDistance(m.minLength);
						muscles[i] = m;
						nodesDone.add(new NodeSet(n,on));
						i++;
					}
				}
			}
	}
	
	public Creature(Creature mutateFrom){
		Random r = new Random();
		this.id = cID;
		cID++;
		//Mutate another node, 2% chance to add, 2% chance to remove
		int nodesToAdd = 0;
		switch(r.nextInt(100)){
			case 0:
				nodesToAdd = 1;
				break;
			case 1:
				nodesToAdd = -1;
				break;
		}
		
		numNodes = mutateFrom.numNodes+nodesToAdd;
		if(numNodes < 1)
			numNodes = 1;
		nodes = new GameObject[mutateFrom.numNodes+nodesToAdd];
		muscles = new Muscle[numNodes+numNodes*(numNodes-3)/2];
		oPos = new Vector2[mutateFrom.oPos.length+nodesToAdd];
		
		for(int i = 0; i < nodes.length; i++){
			nodes[i] = new GameObject();
			GameObject g = nodes[i];
			BodyFixture f = new BodyFixture(new Rectangle(0.5,0.5));
			
			//Mutate friction 10% chance
			int rand = r.nextInt(10);
			double frict;
			if(i < mutateFrom.numNodes)
				frict = mutateFrom.nodes[i].getFixture(0).getFriction();
			else
				frict = r.nextDouble()*10;
			if(rand == 0)
				frict+=r.nextDouble()-0.5;
			if(frict < 0)
				frict = 0;
			if(frict > 10)
				frict = 10;
			g.color = new Color((int) ((frict)/10d*255),0,0);
			f.setFriction(frict);
			g.addFixture(f);
			
			if(i < mutateFrom.numNodes){
				Vector2 shift;
				if(r.nextInt(10) < 1)
					shift = new Vector2(r.nextDouble()-0.5, r.nextDouble()-0.5);
				else
					shift = new Vector2();
				g.getTransform().setTranslation(mutateFrom.oPos[i].add(shift));
				oPos[i] = new Vector2(g.getTransform().getTranslation());
			}else{
				Vector2 trans = new Vector2(r.nextDouble()*2-1, (r.nextDouble()*2-1)-19.5);
				g.getTransform().setTranslation(trans);
				oPos[i] = trans;
			}
			
			g.setMass(MassType.NORMAL);
		}
		int i = 0;
		ArrayList<NodeSet> nodesDone = new ArrayList<NodeSet>();
		for(GameObject n : nodes)
			for(GameObject on: nodes){
				if(on != n){
					if(!nodesDone.contains(new NodeSet(n,on))){
						Muscle m;
						if(i < mutateFrom.muscles.length)
							m = new Muscle(n, on, mutateFrom.muscles[i]);
						else
							m = new Muscle(n,on);
						m.setDistance(m.minLength);
						muscles[i] = m;
						nodesDone.add(new NodeSet(n,on));
						i++;
					}
				}
			}
	}
	
	public Creature(Creature duplicate, boolean b) {
		this.id = cID;
		cID++;
		numNodes = duplicate.numNodes;
		nodes = new GameObject[duplicate.nodes.length];
		muscles = new Muscle[duplicate.muscles.length];
		oPos = new Vector2[duplicate.oPos.length];
		for(int i = 0; i < nodes.length; i++){
			nodes[i] = new GameObject();
			GameObject g = nodes[i];
			BodyFixture f = new BodyFixture(new Rectangle(0.5,0.5));
			g.addFixture(f);
			
			g.getTransform().setTranslation(duplicate.oPos[i]);
			oPos[i] = new Vector2(duplicate.oPos[i]);
			
			g.setMass(MassType.NORMAL);
			
			double frict = duplicate.nodes[i].getFixture(0).getFriction();
			if(frict < 0)
				frict = 0;
			if(frict > 10)
				frict = 10;
			g.color = new Color((int) ((frict)/10d*255),0,0);
			f.setFriction(frict);
		}
		int i = 0;
		ArrayList<NodeSet> nodesDone = new ArrayList<NodeSet>();
		for(GameObject n : nodes)
			for(GameObject on: nodes){
				if(on != n){
					if(!nodesDone.contains(new NodeSet(n,on))){
						Muscle m = new Muscle(n, on, duplicate.muscles[i], false);
						m.setDistance(m.minLength);
						muscles[i] = m;
						nodesDone.add(new NodeSet(n,on));
						i++;
					}
				}
			}
	}

	public void reset(){
		this.maxDistance = this.getPos().x;
		for(int i = 0; i < nodes.length; i++){
			GameObject g = nodes[i];
			g.setAngularVelocity(0);
			g.setLinearVelocity(0,0);
			g.getTransform().setTranslation(oPos[i]);
		}
		for(Muscle m : muscles)
			m.setDistance(m.minLength);
	}

	public void focus(JFrame frame) {
		Vector2 pos = getPos();
		Graphics2DRenderer.xTranslate = -pos.x+(frame.getWidth()/GameObject.SCALE/2);
		Graphics2DRenderer.yTranslate = pos.y+(frame.getWidth()/GameObject.SCALE/2)+3;
	}
	
	public Vector2 getPos(){
		Vector2 ret = new Vector2();
		for(GameObject g : nodes)
			ret = ret.add(g.getTransform().getTranslation());
		return new Vector2(ret.x/nodes.length, ret.y/nodes.length);
	}
	
	public void update(){
		for(GameObject g : nodes)
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
