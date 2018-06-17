import java.util.ArrayList;

public class RelativeOrderMinimizationStrategy implements MinimizationStrategy {
    private EvolutionTree strom;
    private ArrayList<ArrayList<ArrayList<Integer>>> graf;
    private ArrayList<Integer> poradie;
    private ArrayList<ArrayList<Boolean>> spracovane;

    @Override
    public EvolutionTree minimizeCrossing(EvolutionTree strom) {
        if (strom.isEmpty()) return null;
        this.strom = strom.copy();
        optimalizuj(this.strom.getRoot(), false);
        System.out.println("done");

        return this.strom;
    }

    private void vytvor_graf(EvolutionTree.EvolutionNode node, EvolutionTree.EvolutionNode next) {
        graf = new ArrayList<>();
        graf.add(new ArrayList<>());
        graf.add(new ArrayList<>());
        for (int i = 0; i < node.chromosomes.size(); i++) {
            graf.get(0).add(new ArrayList<>());
        }
        for (int i = 0; i < next.chromosomes.size(); i++) {
            graf.get(1).add(new ArrayList<>());
        }

        for (int i = 0; i < next.chromosomes.size(); i++) {
            ArrayList<Boolean> spojene = new ArrayList<>();
            for (int j = 0; j < node.chromosomes.size(); j++) {
                spojene.add(false);
            }
            for (int j = 0; j < next.chromosomes.get(i).genes.size(); j++) {
                int chromosome = strom.which_chromosome(node, next.chromosomes.get(i).genePos.get(j));
                if (chromosome != -1 && !spojene.get(chromosome)) {
                    graf.get(0).get(chromosome).add(i);
                    graf.get(1).get(i).add(chromosome);
                    spojene.set(chromosome, true);
                }
            }
        }

    }

    private void relat_poradie(EvolutionTree.EvolutionNode stableNode, EvolutionTree.EvolutionNode unstableNode) {
        poradie = new ArrayList<>();
        spracovane = new ArrayList<>();
        spracovane.add(new ArrayList<>());
        spracovane.add(new ArrayList<>());
        for (int i = 0; i < stableNode.chromosomes.size(); i++) {
            spracovane.get(0).add(false);
        }
        for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
            spracovane.get(1).add(false);
        }
        for (int i = 0; i < stableNode.chromosomes.size(); i++) {
            dfs(stableNode, i, 0, unstableNode);
        }
    }

    private void relatback(EvolutionTree.EvolutionNode stableNode, EvolutionTree.EvolutionNode unstableNode) {
        poradie = new ArrayList<>();
        spracovane = new ArrayList<>();
        spracovane.add(new ArrayList<>());
        spracovane.add(new ArrayList<>());
        for (int i = 0; i < stableNode.chromosomes.size(); i++) {
            spracovane.get(1).add(false);
        }
        for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
            spracovane.get(0).add(false);
        }
        for (int i = 0; i < stableNode.chromosomes.size(); i++) {
            dfsback(stableNode, i, 1, unstableNode);
        }

    }

    private void dfsback(EvolutionTree.EvolutionNode node, int i, int g, EvolutionTree.EvolutionNode other) {
        if (i == node.chromosomes.size()) return;
        if (spracovane.get(g).get(i)) return;
        spracovane.get(g).set(i, true);
        if (g == 0) poradie.add(i);
        for (int j = 0; j < graf.get(g).get(i).size(); j++) {
            dfsback(other, graf.get(g).get(i).get(j), (g + 1) % 2, node);
        }


    }

    private void dfs(EvolutionTree.EvolutionNode node, int i, int g, EvolutionTree.EvolutionNode other) {
        if (i == node.chromosomes.size()) return;
        if (spracovane.get(g).get(i)) return;
        spracovane.get(g).set(i, true);
        if (g == 1) poradie.add(i);
        for (int j = 0; j < graf.get(g).get(i).size(); j++) {
            dfs(other, graf.get(g).get(i).get(j), (g + 1) % 2, node);
        }
    }

    private void optimalizuj(EvolutionTree.EvolutionNode node, boolean backwards) {
        EvolutionTree.EvolutionNode next = node.getFirst();
        if (!backwards && next != null) {
            vytvor_graf(node, next);
            relat_poradie(node, next);
            strom.zmenPoradieChromozomov(next, poradie);
            EvolutionTree.EvolutionNode next2 = node.getSecond();
            if (next2 != null) {
                vytvor_graf(node, next2);
                relat_poradie(node, next2);
                strom.zmenPoradieChromozomov(next2, poradie);
                optimalizuj(next2, false);
            }
            optimalizuj(next, false);
        } else {
            EvolutionTree.EvolutionNode anc = node.getAncestor();
            if (anc != null) {
                vytvor_graf(anc, node);
                relatback(node, anc);
                strom.zmenPoradieChromozomov(anc, poradie);
                optimalizuj(anc, true);
            }
        }


    }


}
