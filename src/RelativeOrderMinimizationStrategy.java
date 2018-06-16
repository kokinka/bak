import java.util.ArrayList;

public class RelativeOrderMinimizationStrategy implements MinimizationStrategy {
    private EvolutionTree strom;
    private ArrayList<ArrayList<Integer>> graf1;
    private ArrayList<ArrayList<Integer>> graf2;

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
        graf1 = new ArrayList<>();
        graf2 = new ArrayList<>();
        for (int i = 0; i<node.chromosomes.size(); i++){
            graf1.add(new ArrayList<>());
        }
        for (int i = 0; i<next.chromosomes.size(); i++){
            graf2.add(new ArrayList<>());
        }

        for (int i = 0; i<next.chromosomes.size(); i++){
            ArrayList<Boolean> spojene = new ArrayList<>();
            for (int j = 0; j<node.chromosomes.size(); j++){
                spojene.add(false);
            }
            for (int j = 0; j<next.chromosomes.get(i).genes.size(); j++){
                int chromosome = strom.which_chromosome(node, next.genePos.get(j));
                if (chromosome != -1 && !spojene.get(chromosome)){
                    graf1.get(chromosome).add(i);
                    graf2.get(i).add(chromosome);
                    spojene.set(chromosome, true);
                }
            }
        }

    }

    private ArrayList<Integer> poradie;
    private ArrayList<Boolean>spracovane1;
    private ArrayList<Boolean>spracovane2;

    private void relat_poradie(EvolutionTree.EvolutionNode node, EvolutionTree.EvolutionNode next){
        poradie = new ArrayList<>();
        spracovane1 = new ArrayList<>();
        for (int i = 0; i<node.chromosomes.size(); i++){
            spracovane1.add(false);
        }
        spracovane2 = new ArrayList<>();
        for (int i = 0; i<next.chromosomes.size(); i++){
            spracovane2.add(false);
        }
        for (int i = 0; i<node.chromosomes.size(); i++)
            dfs1(node, i, next);


    }

    private void dfs1(EvolutionTree.EvolutionNode node, int i, EvolutionTree.EvolutionNode other){
        if (i == node.chromosomes.size()) return;
        if (spracovane1.get(i)) return;
        spracovane1.set(i, true);
        for (int j = 0; j<graf1.get(i).size(); j++){
            dfs2(other, graf1.get(i).get(j), node);
        }
    }

    private void dfs2(EvolutionTree.EvolutionNode node, int i, EvolutionTree.EvolutionNode other){
        if (i == node.chromosomes.size()) return;
        if (spracovane2.get(i)) return;
        spracovane2.set(i, true);
        poradie.add(i);
        for (int j = 0; j<graf2.get(i).size(); j++){
            dfs1(other, graf2.get(i).get(j), node);
        }
    }

    private void optimalizuj(EvolutionTree.EvolutionNode node){
        EvolutionTree.EvolutionNode next = node.getFirst();
        if (next != null) {
            vytvor_graf(node, next);
            relat_poradie(node, next);
            strom.zmenPoradieChromozomov(next, poradie);
            optimalizuj(next);
        }
        next = node.getSecond();
        if (next != null) {
            vytvor_graf(node, next);
            relat_poradie(node, next);
            strom.zmenPoradieChromozomov(next, poradie);
            optimalizuj(next);
        }
    }
}
