package net.codepixl.EvolutionSim;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Random;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.geometry.Vector2;

public class Muscle extends DistanceJoint{
	
	private boolean jointDir = true;
	public double minLength;
	public double maxLength;
	public double cSpeed, eSpeed;
	
	public Muscle(Body body1, Body body2) {
		super(body1, body2, body1.getTransform().getTranslation(), body2.getTransform().getTranslation());
		this.minLength = new Random().nextDouble()*2+0.5;
		this.maxLength = new Random().nextDouble()*2+1;
		this.cSpeed = new Random().nextDouble()*2+2;
		this.eSpeed = new Random().nextDouble()*2+2; 
		setFrequency(4);
		setDampingRatio(0.1);
		setDistance(2);
	}
	
	//One mutation per generation. Speed mutations have a 10% chance, length mutations have a 20% chance. One mutation only. (60% chance)
	public Muscle(Body body1, Body body2, Muscle mutateFrom){
		super(body1, body2, body1.getTransform().getTranslation(), body2.getTransform().getTranslation());
		Random r = new Random();
		int rand = r.nextInt(10);
		this.minLength = mutateFrom.minLength;
		this.maxLength = mutateFrom.maxLength;
		this.cSpeed = mutateFrom.cSpeed;
		this.eSpeed = mutateFrom.eSpeed;
		
		switch(rand){
			case 0:
			case 1:
				this.minLength+=r.nextDouble()-0.5;
				break;
			case 2:
			case 3:
				this.maxLength+=r.nextDouble()-0.5;
				break;
			case 4:
				this.cSpeed+=r.nextDouble()-0.5;
				break;
			case 5:
				this.eSpeed+=r.nextDouble()-0.5;
				break;
		}
		
		if(minLength < 0.2)
			minLength = 0.2;
		if(maxLength < 0.2)
			maxLength = 0.2;
		if(cSpeed <= 0)
			cSpeed = 0.1;
		if(eSpeed <= 0)
			eSpeed = 0.1;
		
		setFrequency(4);
		setDampingRatio(0.1);
		setDistance(2);
	}
	
	public Muscle(GameObject body1, GameObject body2, Muscle duplicate, boolean b) {
		super(body1, body2, body1.getTransform().getTranslation(), body2.getTransform().getTranslation());
		this.minLength = duplicate.minLength;
		this.maxLength = duplicate.maxLength;
		this.cSpeed = duplicate.cSpeed;
		this.eSpeed = duplicate.eSpeed;
		setFrequency(4);
		setDampingRatio(0.1);
		setDistance(2);
	}

	public Muscle(GameObject body1, GameObject body2, String input){
		super(body1, body2, body1.getTransform().getTranslation(), body2.getTransform().getTranslation());
		String[] params = input.split(",");
		maxLength = Double.parseDouble(params[0]);
		minLength = Double.parseDouble(params[1]);
		cSpeed = Double.parseDouble(params[2]);
		eSpeed = Double.parseDouble(params[3]);
		setFrequency(4);
		setDampingRatio(0.1);
		setDistance(2);
	}

	public void render(Graphics2D g){
		Stroke bef = g.getStroke();
		float stroke = (float)(-this.getBodyDistance()+maxLength)*10;
		if(stroke > 0)
			g.setStroke(new BasicStroke(stroke));
		Vector2 a = getAnchor1();
		Vector2 b = getAnchor2();
		g.drawLine((int)(a.x*GameObject.SCALE), (int)(a.y*GameObject.SCALE), (int)(b.x*GameObject.SCALE), (int)(b.y*GameObject.SCALE));
		g.setStroke(bef);
	}
	
	public void update(){
		double distance = getDistance();
		if(getDistance() < maxLength && jointDir){
			distance = distance+EvolutionSim.deltaTime*eSpeed;
		}else{
			distance = distance-EvolutionSim.deltaTime*cSpeed;
			jointDir = false;
		}
		if(distance < minLength){
			jointDir = true;
			distance = minLength;
		}
		setDistance(distance);
	}
	
	public double getBodyDistance(){
		return Math.sqrt(Math.pow(getBody1().getTransform().getTranslationX()-getBody2().getTransform().getTranslationX(), 2)+Math.pow(getBody1().getTransform().getTranslationY()-getBody2().getTransform().getTranslationY(), 2));
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Muscle){
			Muscle m = (Muscle)o;
			return 
					m.maxLength == maxLength &&
					m.minLength == minLength &&
					m.cSpeed == cSpeed &&
					m.eSpeed == eSpeed;
		}
		return false;
	}

	public String getInfo() {
		return maxLength+","+minLength+","+cSpeed+","+eSpeed;
	}

}
