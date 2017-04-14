import java.util.ArrayList;
import java.util.TreeSet;

public interface HeuristicStrategy {
	void heuristic(EvolutionTree.EvolutionNode node2, ArrayList<TreeSet<Integer>> connection);
}

class BarycenterStrategy implements HeuristicStrategy {

	@Override
	public void heuristic(EvolutionTree.EvolutionNode node2, ArrayList<TreeSet<Integer>> connection) {
		//pocitanie score pre chromozomy v node2
		for (int i = 0; i < node2.chromosomes.size(); i++) {
			for(Integer j : connection.get(i)){
				node2.chromosomes.get(i).score += (j+1)*Settings.chromosome_gap;
			}
			node2.chromosomes.get(i).score /= connection.get(i).size();
		}

		//pre kazdy chromozom v node2 si zapamatam ich aktualne poradie
		for (int i = 0; i < node2.chromosomes.size(); i++) {
			node2.chromosomes.get(i).relativeOrderID = i;
		}

		//sortovanie chromozomov v node2
		(node2.chromosomes).sort((o1, o2) -> {
			if(o1.score > o2.score) return 1;
			if(o1.score < o2.score) return -1;
			return 0;
		});
	}
}