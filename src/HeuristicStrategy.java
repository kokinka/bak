import java.util.ArrayList;

public interface HeuristicStrategy {
	void heuristic(EvolutionTree.EvolutionNode node2, ArrayList<ArrayList<Integer>> connection);
}

class BarycenterStrategy implements HeuristicStrategy {

	@Override
	public void heuristic(EvolutionTree.EvolutionNode node, ArrayList<ArrayList<Integer>> connection) {
		//pocitanie score pre chromozomy v node
		for (int i = 0; i < node.chromosomes.size(); i++) {
			for(Integer j : connection.get(i)){
				node.chromosomes.get(i).score += j+1;
			}
			node.chromosomes.get(i).score /= connection.get(i).size();
		}

		//pre kazdy chromozom v node si zapamatam ich aktualne poradie
		for (int i = 0; i < node.chromosomes.size(); i++) {
			node.chromosomes.get(i).relativeOrderID = i;
		}

		//sortovanie chromozomov v node
		(node.chromosomes).sort((o1, o2) -> {
			if(o1.score > o2.score) return 1;
			if(o1.score < o2.score) return -1;
			return 0;
		});
	}
}