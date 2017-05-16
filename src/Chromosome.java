import java.util.ArrayList;

class Chromosome {

	ArrayList<Integer> genes;
	ArrayList<Integer> genePos;
	boolean isCircular;
	double score = 0;
	int relativeOrderID;

	Chromosome(ArrayList<Integer> genes, boolean isCircular){
		this.genes = genes;
		this.isCircular = isCircular;
		this.genePos = new ArrayList<>();
	}

	//just for copy
	Chromosome(ArrayList<Integer> genes, ArrayList<Integer> genePos, boolean isCircular){
		this.genes = genes;
		this.genePos = genePos;
		this.isCircular = isCircular;
	}

	Chromosome copy(){
		ArrayList<Integer> genes = new ArrayList<>();
		genes.addAll(this.genes);
		ArrayList<Integer> genePos = new ArrayList<>();
		genePos.addAll(this.genePos);
		return new Chromosome(genes, genePos, this.isCircular);
	}
}