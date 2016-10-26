package net.codepixl.EvolutionSim;

public class NodeSet{
	public GameObject a;
	public GameObject b;
	public NodeSet(GameObject a, GameObject b){
		this.a = a;
		this.b = b;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof NodeSet)
			return (((NodeSet) o).a == a && ((NodeSet) o).b == b) || (((NodeSet) o).a == b && ((NodeSet) o).b == a);
		return false;
	}
}
