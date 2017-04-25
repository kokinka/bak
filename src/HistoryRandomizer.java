import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class HistoryRandomizer {
	private static EvolutionTree strom = new EvolutionTree();

	private static void randomize(EvolutionTree.EvolutionNode node){
		if(node == null) return;

		permutuj(node);

		for (int i = 0; i < node.chromosomes.size(); i++) {
			Random random = new Random();
			int rand_val = random.nextInt(4) + 1;
			switch (rand_val) {
				case 1:
					if (random.nextBoolean()) strom.otoc(node, i);
					break;
				case 2:
					strom.rez(node, i, random.nextInt(node.chromosomes.get(i).genes.size()));
					break;
				case 3:
					if (random.nextBoolean()) strom.otoc(node, i);
					strom.rez(node, i, random.nextInt(node.chromosomes.get(i).genes.size()));
					break;
				case 4:
					strom.rez(node, i, random.nextInt(node.chromosomes.get(i).genes.size()));
					if (random.nextBoolean()) strom.otoc(node, i);
					break;
			}
		}

		randomize(node.getFirst());
		randomize(node.getSecond());
	}

	private static void permutuj(EvolutionTree.EvolutionNode node) {
		for (int i = 0; i < node.chromosomes.size(); i++) {
			node.chromosomes.get(i).relativeOrderID = i;
		}

		ArrayList<Integer> relativeChromosomesPos1 = new ArrayList<>();
		ArrayList<Integer> relativeChromosomesPos2 = new ArrayList<>();

		if (node.getFirst() != null) {
			for (int i = 0; i < node.getFirst().genePos.size(); i++) {
				relativeChromosomesPos1.add(strom.which_chromosome(node, node.getFirst().genePos.get(i)));
			}
		}
		if (node.getSecond() != null) {
			for (int i = 0; i < node.getSecond().genePos.size(); i++) {
				relativeChromosomesPos2.add(strom.which_chromosome(node, node.getSecond().genePos.get(i)));
			}
		}

		Collections.shuffle(node.chromosomes);

		strom.normalizeNode(node, relativeChromosomesPos1, relativeChromosomesPos2);
	}

	/** To use, call with one argument - path of the history file, you want to randomize **/
	public static void main(String[] args) throws IOException {
		if(args.length != 1) throw new IllegalArgumentException("Wrong number of arguments");
		File f = new File(args[0]);
		strom.load(f);
		File outFile = new File(args[0].split("\\.")[0] + "-random_output.history");
		outFile.delete();
		outFile.createNewFile();
		FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		randomize(strom.getRoot());
		strom.exportCrossings(bw);
		fw.close();
	}
}
