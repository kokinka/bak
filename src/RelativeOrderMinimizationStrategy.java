import java.util.ArrayList;

public class RelativeOrderMinimizationStrategy implements MinimizationStrategy {
    private EvolutionTree strom;
    private ArrayList<ArrayList<ArrayList<Integer>>> graf;

    @Override
    public EvolutionTree minimizeCrossing(EvolutionTree strom) {
        if (strom.isEmpty()) return null;
        EvolutionTree best_tree = strom.copy();
        this.strom = best_tree;
//        int bestCrossings = strom.countCrossings(strom.getRoot());
//        System.out.println(bestCrossings);
        optimalizuj(this.strom.getRoot());
//        bestCrossings = this.strom.countCrossings(strom.getRoot());
//        System.out.println(bestCrossings);

        System.out.println("done");

        return best_tree;
    }

    private void vytvor_graf(EvolutionTree.EvolutionNode node, EvolutionTree.EvolutionNode next){
        graf = new ArrayList<>();
        graf.add(new ArrayList<>());
        graf.add(new ArrayList<>());
        for (int i = 0; i<node.chromosomes.size(); i++){
            graf.get(0).add(new ArrayList<>());
        }
        for (int i = 0; i<next.chromosomes.size(); i++){
            graf.get(1).add(new ArrayList<>());
        }

        for (int i = 0; i<next.chromosomes.size(); i++){
            ArrayList<Boolean> spojene = new ArrayList<>();
            for (int j = 0; j<node.chromosomes.size(); j++){
                spojene.add(false);
            }
            for (int j = 0; j<next.chromosomes.get(i).genes.size(); j++){
                int chromosome = strom.which_chromosome(node, next.genePos.get(j));
                if (chromosome != -1 && !spojene.get(chromosome)){
                    graf.get(0).get(chromosome).add(i);
                    graf.get(1).get(i).add(chromosome);
                    spojene.set(chromosome, true);
                }
            }
        }

    }

    private ArrayList<Integer> poradie;
    private ArrayList<ArrayList<Boolean>> spracovane;

    private void relat_poradie(EvolutionTree.EvolutionNode node, EvolutionTree.EvolutionNode next){
        poradie = new ArrayList<>();
        spracovane = new ArrayList<>();
        spracovane.add(new ArrayList<>());
        spracovane.add(new ArrayList<>());
        for (int i = 0; i<node.chromosomes.size(); i++){
            spracovane.get(0).add(false);
        }
        for (int i = 0; i<next.chromosomes.size(); i++){
            spracovane.get(1).add(false);
        }
        for (int i = 0; i<node.chromosomes.size(); i++)
            dfs(node, i, 0, next);


    }

    private void dfs(EvolutionTree.EvolutionNode node, int i, int g, EvolutionTree.EvolutionNode other){
        if (i == node.chromosomes.size()) return;
        if (spracovane.get(g).get(i)) return;
        spracovane.get(g).set(i, true);
        if (g == 1) poradie.add(i);
        for (int j = 0; j<graf.get(g).get(i).size(); j++){
            dfs(other, graf.get(g).get(i).get(j), (g+1)%2, node);
        }
    }

//    private void dfs2(EvolutionTree.EvolutionNode node, int i, EvolutionTree.EvolutionNode other){
//        if (i == node.chromosomes.size()) return;
//        if (spracovane2.get(i)) return;
//        spracovane2.set(i, true);
//        poradie.add(i);
//        for (int j = 0; j<graf2.get(i).size(); j++){
//            dfs1(other, graf2.get(i).get(j), node);
//        }
//    }

    private void optimalizuj(EvolutionTree.EvolutionNode node){
        EvolutionTree.EvolutionNode next = node.getFirst();
        optimalize(node, next);
        next = node.getSecond();
        optimalize(node, next);
    }

    private void optimalize(EvolutionTree.EvolutionNode node, EvolutionTree.EvolutionNode next) {
        if (next != null) {
            vytvor_graf(node, next);
            relat_poradie(node, next);
            strom.zmenPoradieChromozomov(next, poradie);
            optimalizuj(next);
        }
    }
}
