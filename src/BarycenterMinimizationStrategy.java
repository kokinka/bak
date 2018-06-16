import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BarycenterMinimizationStrategy implements MinimizationStrategy {
    EvolutionTree strom;

    @Override
    public EvolutionTree minimizeCrossing(EvolutionTree strom) {
        this.strom = strom;
        if (strom.isEmpty()) return null;
        int counter = 0;
        int bestCrossings = strom.countCrossings(strom.getRoot());
        System.out.println(bestCrossings);
        EvolutionTree bestTree = strom.copy();
        int lastCrossingNumber = Integer.MAX_VALUE;
        while (!(counter == 5)) {
            optimalizuj(strom.getRoot());
            int crossingNumber = strom.countCrossings(strom.getRoot());
            if (crossingNumber == lastCrossingNumber) break;
            if (crossingNumber > lastCrossingNumber) counter++;
            if (crossingNumber < bestCrossings) {
                bestCrossings = crossingNumber;
                bestTree = strom.copy();
            }
            lastCrossingNumber = crossingNumber;
        }
        System.out.println(bestCrossings);
        return bestTree;
    }

    private void optimalizuj(EvolutionTree.EvolutionNode node) {
        if (node.getAncestor()!= null) {
            Set<EvolutionTree.EvolutionNode> set = new HashSet<>();
            set.add(node.getAncestor());
            oneSidedOpt(set, node, false);
        } else oneSidedOpt(null, node, false);

        if (node.getFirst() != null) {
            Set<EvolutionTree.EvolutionNode> set = new HashSet<>();
            set.add(node.getFirst());
            optimalizuj(node.getFirst());
            if (node.getSecond() != null) {
                set.add(node.getSecond());
                optimalizuj(node.getSecond());
            }
            oneSidedOpt(set, node, true);
        }
    }

    private void oneSidedOpt(Set<EvolutionTree.EvolutionNode> stableNodeSet, EvolutionTree.EvolutionNode unstableNode, boolean isBackwards) {
        if (!isBackwards) {
            for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
                if (stableNodeSet == null) otocChromozomVKoreni(unstableNode, i);
                else {
                    ArrayList<Integer> result = rezatAOtocit(unstableNode.chromosomes.get(i), unstableNode.getAncestor());
                    strom.rez(unstableNode, i, result.get(0));
                    if (result.get(1) == 1) strom.otoc(unstableNode, i);
                }
            }
        }

        if (stableNodeSet == null) return;

        // zoznam id genov z druheho nodu(nodov), s ktorymi je chromozom spojeny, vyuzijeme v barycentrovej heuristike
        ArrayList<ArrayList<Integer>> geneConnection = new ArrayList<>();
        for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
            geneConnection.add(new ArrayList<>());
        }
        // toto pole hovori ktoremu chromozomu patri pozicia z genPos v potomkovi unstableNode
        // je potrebne aby sme si pamatali ako to bolo pred tym ako sa chromozomy premiesaju
        ArrayList<Integer> relativeChromosomesPos1 = new ArrayList<>();
        ArrayList<Integer> relativeChromosomesPos2 = new ArrayList<>();
        // v tejto casti vytvaram arraylist geneConnection a pole relativeCh pre oba pripady dopredu, dozadu
        if (!isBackwards) {
            if (unstableNode.getFirst() != null) {
                for (int i = 0; i < unstableNode.getFirst().genePos.size(); i++) {
                    relativeChromosomesPos1.add(strom.which_chromosome(unstableNode, unstableNode.getFirst().genePos.get(i)));
                }
            }
            if (unstableNode.getSecond() != null) {
                for (int i = 0; i < unstableNode.getSecond().genePos.size(); i++) {
                    relativeChromosomesPos2.add(strom.which_chromosome(unstableNode, unstableNode.getSecond().genePos.get(i)));
                }
            }
            for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
                geneConnection.get(i).addAll(unstableNode.chromosomes.get(i).genePos);
                ArrayList<Integer> minusJedna = new ArrayList<>();
                minusJedna.add(-1);
                geneConnection.get(i).removeAll(minusJedna);
            }
        } else {
            for (int i = 0; i < unstableNode.getFirst().genePos.size(); i++) {
                int which_ch = strom.which_chromosome(unstableNode, unstableNode.getFirst().genePos.get(i));
                relativeChromosomesPos1.add(which_ch);
                if (which_ch != -1) {
                    geneConnection.get(which_ch).add(i);
                }
            }
            if (stableNodeSet.size() == 2) {
                for (int i = 0; i < unstableNode.getSecond().genePos.size(); i++) {
                    int which_ch = strom.which_chromosome(unstableNode, unstableNode.getSecond().genePos.get(i));
                    relativeChromosomesPos2.add(which_ch);
                    if (which_ch != -1) {
                        geneConnection.get(which_ch).add(i);
                    }
                }
            }
        }

        //pocitanie score pre chromozomy v node
        for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
            for (Integer j : geneConnection.get(i)) {
                unstableNode.chromosomes.get(i).score += j + 1;
            }
            unstableNode.chromosomes.get(i).score /= geneConnection.get(i).size();
        }

        //pre kazdy chromozom v node si zapamatam ich aktualne poradie
        for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
            unstableNode.chromosomes.get(i).relativeOrderID = i;
        }

        //sortovanie chromozomov v node
        (unstableNode.chromosomes).sort((o1, o2) -> {
            if (o1.score > o2.score) return 1;
            if (o1.score < o2.score) return -1;
            return 0;
        });

        strom.normalizeNode(unstableNode, relativeChromosomesPos1, relativeChromosomesPos2);

    }

    private ArrayList<Integer> rezatAOtocit(Chromosome ch, EvolutionTree.EvolutionNode ancestor) {
        ArrayList<Integer> vysledokOtocenia = chcemOtacatOpt(ch, ancestor);
        ArrayList<Integer> result = new ArrayList<>();
        if (!ch.isCircular) {
            result.add(0);
            if (vysledokOtocenia.get(0) == 1) result.add(1);
            else result.add(0);
            return result;
        }
        int najuspesnejsiRez = 0;
        boolean chcemOtocit;
        if (vysledokOtocenia.get(0) == 1) {
            chcemOtocit = true;
        } else chcemOtocit = false;
        int minPocetKrizeni = vysledokOtocenia.get(1);
        for (int rez = 1; rez < ch.genePos.size(); rez++) {
            ArrayList<Integer> newGenePos = new ArrayList<>();
            ArrayList<Integer> newGenes = new ArrayList<>();
            for (int j = rez; j < ch.genePos.size(); j++) {
                newGenePos.add(ch.genePos.get(j));
                newGenes.add(ch.genes.get(j));
            }
            for (int j = 0; j < rez; j++) {
                newGenePos.add(ch.genePos.get(j));
                newGenes.add(ch.genes.get(j));
            }
            Chromosome newCh = new Chromosome(newGenes, true);
            newCh.genePos = newGenePos;
            int pocetKrizeni;
            vysledokOtocenia = chcemOtacatOpt(newCh, ancestor);
            pocetKrizeni = vysledokOtocenia.get(1);
            if (pocetKrizeni < minPocetKrizeni) {
                minPocetKrizeni = pocetKrizeni;
                najuspesnejsiRez = rez;
                if (vysledokOtocenia.get(0) == 1) {
                    chcemOtocit = true;
                } else chcemOtocit = false;
            }
        }

        result.add(najuspesnejsiRez);
        if (chcemOtocit) result.add(1);
        else result.add(0);
        return result;
    }

    private int spocitajKrizeniaPreChromozom(ArrayList<Integer> genePos) {
        int crossings = 0;
        for (int j = 0; j < genePos.size(); j++) {
            for (int i = j + 1; i < genePos.size(); i++) {
                if (genePos.get(i) == -1) continue;
                if (genePos.get(i) < genePos.get(j)) crossings++;
            }
        }
        return crossings;
    }

    private ArrayList<Integer> chcemOtacatOpt(Chromosome ch, EvolutionTree.EvolutionNode ancestor) {
        int myCrossings1 = spocitajKrizeniaPreChromozom(ch.genePos);

        ArrayList<Integer> newGenePos = new ArrayList<>();
        ArrayList<Integer> newGenes = new ArrayList<>();

        for (int i = ch.genePos.size() - 1; i >= 0; i--) {
            if (ch.genePos.get(i) != -1) {
                int ancestorGene = ancestor.allGenes.get(ch.genePos.get(i));
                if ((ancestorGene >= 0 && ch.genes.get(i) < 0)
                        || (ancestorGene < 0 && ch.genes.get(i) >= 0)
                        ) myCrossings1++;
            }
            if (ch.genes.get(i) < 0) myCrossings1++;
            newGenePos.add(ch.genePos.get(i));
            newGenes.add(ch.genes.get(i) * (-1));
        }
        int myCrossings2 = spocitajKrizeniaPreChromozom(newGenePos);
        for (int i = ch.genePos.size() - 1; i >= 0; i--) {
            if (newGenePos.get(i) != -1) {
                int ancestorGene = ancestor.allGenes.get(newGenePos.get(i));
                if ((ancestorGene >= 0 && newGenes.get(i) < 0)
                        || (ancestorGene < 0 && newGenes.get(i) >= 0)
                        ) myCrossings2++;
            }
            if (newGenes.get(i) < 0) myCrossings2++;
        }

        ArrayList<Integer> result = new ArrayList<>();
        if (myCrossings1 > myCrossings2) {
            result.add(1);
            result.add(myCrossings2);
        } else {
            result.add(0);
            result.add(myCrossings1);
        }

        return result;
    }

    private void otocChromozomVKoreni(EvolutionTree.EvolutionNode node, int ktory) {
        int reverseGenes = 0;
        Chromosome ch = node.chromosomes.get(ktory);
        for (int i = 0; i < ch.genes.size(); i++) {
            if (ch.genes.get(i) < 0) {
                reverseGenes++;
            }
        }
        if (reverseGenes > ch.genes.size() / 2) strom.otoc(node, ktory);
    }
}
