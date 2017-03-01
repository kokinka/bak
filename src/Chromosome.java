import java.util.ArrayList;

class Chromosome {
	ArrayList<Integer> genes;
	boolean isCircular;
	Chromosome(ArrayList<Integer> genes, boolean isCircular){
		this.genes = genes;
		this.isCircular = isCircular;
	}
}