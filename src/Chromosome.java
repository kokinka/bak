import java.util.ArrayList;

class Chromosome {
	ArrayList<Integer> genes;
	ArrayList<Integer> genePos;
	boolean isCircular;
	double score;
	int relativeOrderID;

	Chromosome(ArrayList<Integer> genes, boolean isCircular){
		this.genes = genes;
		this.isCircular = isCircular;
		this.score = 0;
		this.genePos = new ArrayList<>();
	}
}