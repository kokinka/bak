import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.paint.Color;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author user
 */
class EvolutionTree {

    private HashMap<String, EvolutionNode> map = new HashMap<String, EvolutionNode>();
    private EvolutionNode root;
    private float scaleX;
    // private float scaleY;
    private int diff_gen;
    private Color[] gene_col;
    private boolean block_calced;
    private HashMap<Integer, Integer> block_cover;
    private TreeSet<Integer> universe;
    private HashMap<Integer, TreeSet<Integer>> sets;  //sets.keylist je zoznam vsetkych genov
    private boolean optimized;
    private TreeSet<Integer> solution;
    private Set<Integer> genes;

    public EvolutionTree() {
        this.root = null;
        this.scaleX = 0;
        this.diff_gen = 0;
        this.block_calced = false;
        this.block_cover = new HashMap();
        this.universe = new TreeSet<Integer>();
        this.sets = new HashMap();
        this.optimized = false;
        this.solution = new TreeSet<Integer>();
        this.genes = new HashSet<Integer>();
    }

    public EvolutionTree(EvolutionNode root, float scaleX, int diff_gen, boolean block_calced,
        HashMap<Integer, Integer> block_cover, TreeSet<Integer> universe, HashMap<Integer, TreeSet<Integer>> sets,
        boolean optimized, TreeSet<Integer> solution, Set<Integer> genes) {
        this.root = root;
        this.scaleX = scaleX;
        this.diff_gen = diff_gen;
        this.block_calced = block_calced;
        this.block_cover = block_cover;
        this.universe = universe;
        this.sets = sets;
        this.optimized = optimized;
        this.solution = solution;
        this.genes = genes;
    }

    public EvolutionTree copy(){
        EvolutionNode root = this.root.copy();
        HashMap<String, EvolutionNode> map = new HashMap<>();
        map.putAll(this.map);
        HashMap<Integer, Integer> block_cover = new HashMap<>();
        block_cover.putAll(this.block_cover);
        TreeSet<Integer> universe = new TreeSet<>();
        universe.addAll(this.universe);
        HashMap<Integer, TreeSet<Integer>> sets = new HashMap<>();
        sets.putAll(this.sets);
        TreeSet<Integer> solution = new TreeSet<>();
        solution.addAll(this.solution);
        Set<Integer> genes = new HashSet<>();
        genes.addAll(this.genes);
        return new EvolutionTree(root, this.scaleX, this.diff_gen, this.block_calced, block_cover, universe,
            sets, this.optimized, solution, genes);
    }


    public EvolutionNode getRoot() {
        return root;
    }

    public void setRoot(EvolutionNode root) {
        this.root = root;
    }

    public Set<Integer> getGenes() {
        return genes;
    }


    void optimize() {
        //TTODO run Setcover solver and apply result on gene settings , not all genes have gene meta in setting nede to get complete list of genes
        if (!optimized) {
            setCover();
        }
        //0 draw-f , 1 draw-t transparent-t ,2 draw-t transparent-f
        for (Integer a : genes) {
            GeneMeta gene_meta = new GeneMeta(a);
            if (Settings.gene_meta.containsKey(a)) {
                gene_meta = Settings.gene_meta.get(a);
            }
            if (solution.contains(a)) {
                gene_meta.setHighlighted(true);
                gene_meta.setDraw(true);
                gene_meta.setTransparent(false);
            } else if (Settings.optimized == 0) {
                gene_meta.setDraw(false);
            } else if (Settings.optimized == 1) {
                gene_meta.setHighlighted(false);
                gene_meta.setDraw(true);
                gene_meta.setTransparent(true);
            } else {
                gene_meta.setHighlighted(false);
                gene_meta.setDraw(true);
                gene_meta.setTransparent(false);
            }
            Settings.gene_meta.put(a, gene_meta);
        }

    }

    void loadILP(File file) throws FileNotFoundException {
        if (!this.block_calced) {
            //treba zacinat aspon na jednotke,inak je neznama orientacia blokov
            this.findBlocks();

        }
        this.solution = new TreeSet();
        this.optimized = true;
        Scanner in = new Scanner(file);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.contains("<variable name")) {
                String[] splitted = line.split("\"");
                Integer a = Integer.parseInt(splitted[1].substring(1));
                if (splitted[5].equals("1")) {
                    solution.add(a);
                }
            }
        }
        optimize();

    }

    public class EvolutionNode {

        private String name;
        private String id;
        private EvolutionNode ancestor;
        private EvolutionNode first;
        private EvolutionNode second;
        private String event;
        private double time;
        ArrayList<Chromosome> chromosomes;
        ArrayList<Integer> allGenes;
        ArrayList<Integer> genePos;
        private ArrayList<Integer> gene_x_pos;
        private ArrayList<Integer> blockNumAncF;
        private ArrayList<Integer> blockNumAncS;
        private ArrayList<Integer> blockNumDes;
        private HashMap<Integer, Integer> blockWidth;
        private double next;
        private int ancestorNum;
        private int width;

        public EvolutionNode(String name, String id, EvolutionNode ancestor, String event, double time, ArrayList<Chromosome> chromosomes, ArrayList<Integer> genePos) {
            this.name = name;
            this.id = id;
            this.ancestor = ancestor;
            this.event = event;
            this.time = time;
            this.chromosomes = chromosomes;
            this.allGenes = new ArrayList<Integer>();
            for(Chromosome ch: chromosomes){
                allGenes.addAll(ch.genes);
            }
            this.genePos = genePos;
            this.next = -1;
            this.ancestorNum = 0;
            this.width = 0;
            this.blockNumAncF = new ArrayList();
            this.blockNumAncS = new ArrayList();
            this.blockNumDes = new ArrayList();
            this.blockWidth = new HashMap();
            this.gene_x_pos = new ArrayList();
        }

        //just for copy
        public EvolutionNode(String name, String id, EvolutionNode first, EvolutionNode second,
            String event, double time, ArrayList<Chromosome> chromosomes, ArrayList<Integer> allGenes,
            ArrayList<Integer> genePos, ArrayList<Integer> gene_x_pos, ArrayList<Integer> blockNumAncF,
            ArrayList<Integer> blockNumAncS, ArrayList<Integer> blockNumDes, HashMap<Integer, Integer> blockWidth,
            double next, int ancestorNum, int width){
            this.name = name;
            this.id = id;
            this.first = first;
            this.second = second;
            this.event = event;
            this.time = time;
            this.chromosomes = chromosomes;
            this.allGenes = allGenes;
            this.genePos = genePos;
            this.gene_x_pos = gene_x_pos;
            this.blockNumAncF = blockNumAncF;
            this.blockNumAncS = blockNumAncS;
            this.blockNumDes = blockNumDes;
            this.blockWidth = blockWidth;
            this.next = next;
            this.ancestorNum = ancestorNum;
            this.width = width;
        }

        EvolutionNode copy(){
            EvolutionNode first = null;
            EvolutionNode second = null;
            if(this.first != null) first = this.first.copy();
            if(this.second != null) second = this.second.copy();
            ArrayList<Chromosome> chromosomes = new ArrayList<>();
            for(Chromosome ch: this.chromosomes){
                chromosomes.add(ch.copy());
            }
            ArrayList<Integer> allGenes = new ArrayList<>();
            allGenes.addAll(this.allGenes);
            ArrayList<Integer> genePos = new ArrayList<>();
            genePos.addAll(this.genePos);
            ArrayList<Integer> gene_x_pos = new ArrayList<>();
            gene_x_pos.addAll(this.gene_x_pos);
            ArrayList<Integer> blockNumAncF = new ArrayList<>();
            blockNumAncF.addAll(this.blockNumAncF);
            ArrayList<Integer> blockNumAncS = new ArrayList<>();
            blockNumAncS.addAll(this.blockNumAncS);
            ArrayList<Integer> blockNumDes = new ArrayList<>();
            blockNumDes.addAll(this.blockNumDes);
            HashMap<Integer, Integer> blockWidth = new HashMap<>();
            blockWidth.putAll(this.blockWidth);

            EvolutionNode ancestor = new EvolutionNode(this.name, this.id, first, second, this.event,
                this.time, chromosomes, allGenes, genePos, gene_x_pos, blockNumAncF, blockNumAncS,
                blockNumDes, blockWidth, this.next, this.ancestorNum, this.width);
            if(first != null) first.ancestor = ancestor;
            if(second != null) second.ancestor = ancestor;

            return ancestor;
        }

        public int getWidth() {
            return width;
        }

        private ArrayList<Integer> getancblock(boolean first) {
            if (first) {
                return this.blockNumAncF;
            }
            return this.blockNumAncS;
        }

        private void setancblock(boolean first, ArrayList<Integer> newarr) {
            if (first) {
                this.blockNumAncF = newarr;
            } else {
                this.blockNumAncS = newarr;
            }
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public EvolutionNode getFirst() {
            return first;
        }

        public void setDescendant(EvolutionNode descendant) {
            this.ancestorNum++;
            if ((this.next == -1) || ((this.next != -1) && (this.next > descendant.time))) {
                this.next = descendant.time;

            }
            if (this.first == null) {
                this.first = descendant;
            } else if (this.second == null) {
                this.second = descendant;
            } else {
                try {
                    throw new Exception();
                } catch (Exception ex) {
                    Logger.getLogger(EvolutionTree.class.getName()).log(Level.SEVERE, "Node can't have more than two descendants", ex);
                }
            }

        }

        public EvolutionNode getSecond() {
            return second;
        }

        public int calcNodeWidth() {
            int i = 0;
            for(Chromosome ch: this.chromosomes){
                for(Integer a: ch.genes) {
                    if (Settings.is_draw(a)) {
                        i = i + Settings.line_gap + Settings.gene_width(a);
                    }
                }
                i = i + Settings.chromosome_gap;
            }
            if (i > 0) {
                i = i - Settings.line_gap;
                i = i - Settings.chromosome_gap;
            }
            return i;
        }

        public int calcBlockWidth() {
            return this.calcNodeWidth() + 2 * Settings.node_gap;
        }

    }

    public void load(File f) throws FileNotFoundException {
        Scanner fileReader = new Scanner(f);
        //EvolutionTree loaded = new EvolutionTree();
        while (fileReader.hasNextLine()) {
            String line = fileReader.nextLine();
            parse(line);
        }
        fileReader.close();
        this.calcScale();
        Settings.setGenesSet(this.genes);
        Settings.calcColors();
        Settings.clearGeneMeta();

    }

    public void calcScale() {
        Settings.scale_x = (Settings.width - 10) / this.scaleX;
    }

    private void parse(String s) {
        String[] splitted = s.split("\\s+");
        EvolutionTree.EvolutionNode parsed;
        String name = splitted[0];
        String id = splitted[1];
        String ancestor_id = splitted[2];
        EvolutionNode ancestor = null;
        if (!ancestor_id.equals("root")) {
            ancestor = map.get(ancestor_id);
        }
        float time = Float.parseFloat(splitted[3]);
        this.scaleX = Math.max(this.scaleX, time);
        String event = splitted[4];
        ArrayList<Integer> genes = new ArrayList<>();
        ArrayList<Chromosome> chromosomes = new ArrayList<>();
        int i = 5;
        boolean hasChromosomes = false;
        while(!splitted[i].equals("#")){
            if(splitted[i].equals("$")){
                hasChromosomes = true;
                chromosomes.add(new Chromosome(genes, false));
                genes = new ArrayList<>();
            } else if(splitted[i].equals("@")){
                hasChromosomes = true;
                chromosomes.add(new Chromosome(genes, true));
                genes = new ArrayList<>();
            } else {
                genes.add(Integer.parseInt(splitted[i]));
                this.genes.add(Math.abs(Integer.parseInt(splitted[i])));
            }
            i++;
        }
        if(!hasChromosomes) chromosomes.add(new Chromosome(genes, false));

        ArrayList<Integer> geneorder = new ArrayList<>();
        int k = 0;
        int counter = chromosomes.get(0).genes.size();
        int which_ch = 0;
        for (int j = i + 1; j < splitted.length; j++) {
            if(k == counter){
                which_ch++;
                counter += chromosomes.get(which_ch).genes.size();
            }
            chromosomes.get(which_ch).genePos.add(Integer.parseInt(splitted[j]));
            geneorder.add(Integer.parseInt(splitted[j]));
            k++;
        }
        parsed = new EvolutionTree.EvolutionNode(name, id, ancestor, event, time, chromosomes, geneorder);
        if (event.equals("root")) {
            this.setRoot(parsed);
        } else {
            ancestor.setDescendant(parsed);
        }
        map.put(id, parsed);
    }

    public void print(DrawFactory fac) {
        calcwidth(this.getRoot());
        fac.setLineWidth(Settings.line_size);

        boolean containsName = false;
        for(Integer a : this.getRoot().allGenes){
            String name = Settings.gene_name(a);
            if(name != null){
                containsName = true;
            }
        }
        if(containsName && (fac instanceof SVGDrawFactory)) fac.translateX(60);

        rek(this.getRoot(), 0, 0, 0, Settings.height, fac);

        //vypisovanie mien genov
        if(containsName && (fac instanceof SVGDrawFactory)){
            fac.translateX(-60);
            fac.writeGeneNames(this.getRoot().allGenes, this.getRoot().gene_x_pos);
        }
    }

    private int calcwidth(EvolutionNode node) {
        if (node.ancestorNum == 0) {
            node.setWidth(node.calcBlockWidth());
            return node.getWidth();
        } else if (node.ancestorNum == 1) {
            int des_wid = this.calcwidth(node.getFirst());
            node.setWidth(Math.max(des_wid, node.calcBlockWidth()));
            return node.getWidth();
        } else {
            int des_wid1 = this.calcwidth(node.getFirst());
            int des_wid2 = this.calcwidth(node.getSecond());
            node.setWidth(Math.max(des_wid1 + des_wid2, node.calcBlockWidth()));
            return node.getWidth();
        }
    }

    private int findBlocks(EvolutionNode node, int num) {
        if (node.ancestorNum > 0) {
            num = this.findBlocks(node.getFirst(), num);
            num = this.calcBlocks(node, node.getFirst(), num, true);
            if (node.ancestorNum == 2) {
                num = this.findBlocks(node.getSecond(), num);
                num = this.calcBlocks(node, node.getSecond(), num, false);
            }
        }

        return num;
    }

    public void findBlocks() {
        findBlocks(this.root, 1);
        this.block_calced = true;
        rekBlok(this.root);
    }

    private int calcBlocks(EvolutionNode anc, EvolutionNode des, int num, boolean firstbool) {
        int i = num;
        //TODO asi tam nebudu vsade allGenes, ale bude to nejako rozdelene na chromozomy, ak chceme vediet spajat optimalizacie
        for (int a : anc.allGenes) {
            anc.getancblock(firstbool).add(i);
            i++;
        }
        int j = 0;
        for (int a : des.allGenes) {
            int ancestor = des.genePos.get(j);
            if (ancestor >= 0) {
                int ancblock = anc.getancblock(firstbool).get(ancestor);
                if (a != anc.allGenes.get(ancestor)) {
                    des.blockNumDes.add(ancblock * -1);
                } else {
                    des.blockNumDes.add(ancblock);
                }
            } else {
                des.blockNumDes.add(i);
                i++;
            }
            j++;
        }
        j = 0;
        //anc blocks
        while (j < anc.getancblock(firstbool).size() - 1) {
            int first = anc.getancblock(firstbool).get(j);
            int second = anc.getancblock(firstbool).get(j + 1);
            boolean succes = compareBlocks(first, second, des.blockNumDes);
            int firstWidth = 1;
            if (anc.blockWidth.containsKey(first)) {
                firstWidth = anc.blockWidth.get(first);
            }
            if (succes) {
                int secondWidth = 1;
                anc.setancblock(firstbool, mergeBlocks(second, anc.getancblock(firstbool)));
                des.blockNumDes = mergeBlocks(second, des.blockNumDes);
                if (anc.blockWidth.containsKey(second)) {
                    secondWidth = anc.blockWidth.get(second);
                    anc.blockWidth.remove(second);
                }
                anc.blockWidth.put(first, firstWidth + secondWidth);
            } else {
                j++;
            }
        }

        //des blocks , , new part start
        j =0;
        while (j < des.blockNumDes.size() - 1) {
            int first = des.blockNumDes.get(j);
            int second = des.blockNumDes.get(j + 1);
            boolean succes = compareBlocks(first, second, anc.getancblock(firstbool));
            int firstWidth = 1;
            if (anc.blockWidth.containsKey(first)) {
                firstWidth = anc.blockWidth.get(first);
            }
            if (succes) {
                int secondWidth = 1;
                //anc.setancblock(firstbool,mergeBlocks(second, anc.getancblock(firstbool)));
                des.blockNumDes = mergeBlocks(second, des.blockNumDes);
                if (anc.blockWidth.containsKey(second)) {
                    secondWidth = anc.blockWidth.get(second);
                    anc.blockWidth.remove(second);
                }
                anc.blockWidth.put(first, firstWidth + secondWidth);
            } else {
                j++;
            }
        }

        //new part ende here
        return i;

    }

    private ArrayList<Integer> mergeBlocks(int merged, ArrayList<Integer> array) {
        ArrayList<Integer> newarray = new ArrayList();
        for (int i = 0; i < array.size(); i++) {
            int block = array.get(i);
            if ((block != merged) && (block != merged * -1)) {
                newarray.add(block);
            }
        }
        return newarray;
    }

    private boolean compareBlocks(int first, int second, ArrayList<Integer> array) {
        boolean occured = true; //if blocks haven't occured at all merge them if set true, if set false blocks need to occure at least once.
        for (int i = 0; i < array.size(); i++) {
            int a = array.get(i);
            if (a == first) {
                if (i < array.size() - 1) {
                    if (array.get(i + 1) == second) {
                        occured = true;
                        i++;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            if (a == second * -1) {
                if (i < array.size() - 1) {
                    if (array.get(i + 1) == first * -1) {
                        occured = true;
                        i++;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            }
            if ((a == second) || (a == first * -1)) {
                return false;
            }
        }
        return occured;
    }

    public void exportBlocks(BufferedWriter bw) throws IOException {

        if (!this.block_calced) {
            //treba zacinat aspon na jednotke,inak je neznama orientacia blokov
            this.findBlocks();
        }
        if (!this.optimized) {
            setCover();
        }
        bw.write("Universe size = " + universe.size() + " ,containing " + sets.size() + " sets, " + solution.size() + " used in solution.");
        bw.newLine();
        bw.write("Universe: " + universe.toString());
        bw.newLine();
        bw.write("Sets: " + sets.toString());
        bw.newLine();
        bw.write("Solution: " + solution.toString());
        bw.newLine();
        bw.close();
    }

    public void exportILP(BufferedWriter bw) throws IOException {

        String line = "";
        String elements = "";
        if (!this.block_calced) {
            //treba zacinat aspon na jednotke,inak je neznama orientacia blokov
            this.findBlocks();
        }
        //pamatat globalne , znova to prechadzat je svinstvo
        bw.write("Minimize");
        bw.newLine();
        Object[] subsets = sets.keySet().toArray();
        for (int i = 0; i < subsets.length - 1; i++) {
            line = line.concat("x" + subsets[i] + " +");
            elements = elements.concat("x" + subsets[i] + " ");

        }
        line = line.concat("x" + subsets[subsets.length - 1]);
        elements = elements.concat("x" + subsets[subsets.length - 1]);
        bw.write(line);
        line = "";
        bw.newLine();
        bw.write("Subject To");
        bw.newLine();
        for (int a : universe) {
            line = "";
            boolean first = true;
            for (int s : sets.keySet()) {
                if (sets.get(s).contains(a)) {
                    if (!first) {
                        line = line.concat(" + ");
                    }
                    line = line.concat("x" + s);
                    first = false;
                }
            }
            int times = 1;
            if (this.block_cover.containsKey(a)) {
                times = this.block_cover.get(a);
            }
            line = line.concat(" >= " + times);
            bw.write(line);
            bw.newLine();
        }
        bw.write("Binary");
        bw.newLine();
        bw.write(elements);
        bw.newLine();
        bw.write("End");
        bw.close();

    }

    /// optimalizovat zostavenie set coveru ako ILP
/*      private void rekILP(EvolutionNode node, Set universe, HashMap<Integer, TreeSet<Integer>> event_covered) {
     if (node.ancestorNum > 0) {
     rekILP(node.getFirst(),universe,event_covered);
     if (node.ancestorNum == 2) {
     rekILP(node.getSecond(),universe,event_covered);
     }
     }

     }  */

    public void exportCrossings(BufferedWriter bw) throws IOException {
        exportCrossingsNode(bw, this.getRoot());
        bw.close();
    }

    public void exportCrossingsNode(BufferedWriter bw, EvolutionNode node) throws IOException {
        String line = "";
        line += node.name + " " + node.id + " ";
        if(node.ancestor == null) line += "root ";
        else line += node.ancestor.id + " ";
        line += node.time + " " + node.event + " ";
        for(Chromosome ch : node.chromosomes){
            for (Integer gene: ch.genes){
                line += gene + " ";
            }
            if(ch.isCircular) line += "@ ";
            else line += "$ ";

        }
        line += "# ";
        for (Integer pos: node.genePos){
                line += pos + " ";
        }
        bw.write(line);
        bw.newLine();
        if(node.getFirst() != null) exportCrossingsNode(bw, node.getFirst());
        if(node.getSecond()!= null) exportCrossingsNode(bw, node.getSecond());
    }

    private void rekBlok(EvolutionNode node) {
        if (node.ancestorNum > 0) {
            //TODO asi tam nebudu vsade allGenes, ale bude to nejako rozdelene na chromozomy
            // brat i je zle , treba prej pre oba anc + desc prveho a desc  druheho --- > vytvorit metodu ktora papa list genov,list blokov a list sirok blokov a napcha ich do universu a Sets
            this.fillBlocks(node.allGenes, node.getancblock(true), node.blockWidth);
            this.fillBlocks(node.getFirst().allGenes, node.getFirst().blockNumDes, node.blockWidth);
            this.rekBlok(node.getFirst());
            if (node.ancestorNum == 2) {
                this.fillBlocks(node.allGenes, node.getancblock(false), node.blockWidth);
                this.fillBlocks(node.getSecond().allGenes, node.getSecond().blockNumDes, node.blockWidth);
                this.rekBlok(node.getSecond());
            }
        }
    }

    private void setCover() {
                this.optimized = true;
        if (!this.block_calced) {
            //treba zacinat aspon na jednotke,inak je neznama orientacia blokov
            this.findBlocks();
        }
        TreeSet<Integer> _universe = (TreeSet<Integer>) universe.clone();
        HashMap<Integer, TreeSet<Integer>> _sets = setsClone();
        //na zaciatku odstranime z universa aj setov bloky ktore nechceme pokryt
        TreeSet<Integer> not_covered = new TreeSet();
        //odstranujeme iba tie bloky ktore mam pokryte dost krat, tj. sme na ich poslednom pokryti
        for (int block : _universe) {
            if (this.block_cover.containsKey(block)) {
                int cover_times = this.block_cover.get(block);
                if (cover_times == 0) {
                    not_covered.add(block);
                }
            }
        }
        if (not_covered.size() > 0) {
            this.setsRemove(not_covered, _sets);
            _universe.removeAll(not_covered);
        }
        while (_universe.size() > 0) {
            int biggest_set = -1;
            int size = -1;
            //najdeme najvacsi set
            for (int a : _sets.keySet()) {
                TreeSet current_set = _sets.get(a);
                if (current_set.size() > size) {
                    size = current_set.size();
                    biggest_set = a;
                }
            }
            //pridame do riesenia
            solution.add(biggest_set);
            TreeSet<Integer> covered = _sets.get(biggest_set);
            TreeSet<Integer> remove = new TreeSet();
            //odstranujeme iba tie bloky ktore mam pokryte dost krat, tj. sme na ich poslednom pokryti
            for (int block : covered) {
                if (this.block_cover.containsKey(block)) {
                    int cover_times = this.block_cover.get(block);
                    if (cover_times > 1) {
                        this.block_cover.put(block, cover_times - 1);
                    } else {
                        remove.add(block);
                    }
                } else {
                    remove.add(block);
                }
            }
            //odstranime ho , aj jeho prvky zo vsetkych setov + universa
            _sets.remove(biggest_set);
            this.setsRemove(remove, _sets);
            _universe.removeAll(remove);
        }

    }

    private HashMap<Integer, TreeSet<Integer>> setsClone() {
        HashMap<Integer, TreeSet<Integer>> _sets = new HashMap<Integer, TreeSet<Integer>>();
        Set<Integer> Keys = sets.keySet();
        for (Integer a : Keys) {
            TreeSet<Integer> copied = (TreeSet<Integer>) sets.get(a).clone();
            _sets.put(a, copied);
        }
        return _sets;
    }

    private void setsRemove(Set<Integer> to_remove, HashMap<Integer, TreeSet<Integer>> sets) {
        TreeSet<Integer> empty_sets = new TreeSet();
        for (int a : sets.keySet()) {
            TreeSet current_set = sets.get(a);
            current_set.removeAll(to_remove);
            if (current_set.size() == 0) {
                empty_sets.add(a);
            }
            sets.put(a, current_set);

        }
        for (int empty : empty_sets) {
            sets.remove(empty);
        }
    }

    private void fillBlocks(ArrayList<Integer> genes, ArrayList<Integer> blocks, HashMap<Integer, Integer> block_width) {
        //prida kazdy block -tj. udalost do universa
        for (int a : blocks) {
            universe.add(Math.abs(a));
        }
        int blockpos = 0;
        //blockend sa nainicializuje na hodnotu sirky blocku blockval, ktory je nultym v poradi
        int blockval = Math.abs(blocks.get(blockpos));
        int blockend = 1;
        if (block_width.containsKey(blockval)) {
            blockend = block_width.get(blockval);
        }
        for (int i = 0; i < genes.size(); i++) {
            TreeSet geneset = new TreeSet<Integer>();
            int current_gene = Math.abs(genes.get(i));
            if (sets.containsKey(current_gene)) {
                geneset = sets.get(current_gene);
            }
            geneset.add(blockval);
            sets.put(current_gene, geneset);
            blockend--;
            if (blockend <= 0) {
                blockpos++;
                if (blockpos < blocks.size()) {
                    blockval = Math.abs(blocks.get(blockpos));
                    blockend = 1;
                    if (block_width.containsKey(blockval)) {
                        blockend = block_width.get(blockval);
                    }
                }
            }
        }
    }

    private void rek(EvolutionNode node, double prev_x, double prev_y, int block_y, int block_w, DrawFactory fac) {
        int first_y;
        int timedif_x = 0;
        int line_y;
        double line_x;
        node.gene_x_pos = new ArrayList();
        first_y = block_y + (block_w - node.calcNodeWidth()) / 2;
        line_x = node.time * Settings.scale_x;
        timedif_x = (int) (Math.max(node.next - Settings.time_diff, node.time) * Settings.scale_x);
        if (!node.event.equals("root")) {
            int chromosomeDiff = node.chromosomes.size() - node.ancestor.chromosomes.size();
            if(chromosomeDiff == 0) {
                line_y = first_y;
            } else {
                line_y = first_y - ((1/2) * chromosomeDiff * Settings.chromosome_gap);
            }
	        int k = 0;
	        int l = node.chromosomes.get(0).genes.size() -1; //dalsi koniec chromozomu
	        int m = 0; //na ktorom sme chromozome
            double chromosomeStart = line_y;
	        for (int a : node.genePos) {
		        if (Settings.is_draw(node.allGenes.get(k))) {
                    if (a != -1) {
                        fac.drawGeneLine(prev_x, node.ancestor.gene_x_pos.get(a), line_x, line_y, node.allGenes.get(k));
                    }
                    //rectangle sa bude kreslit v tomto ife
                    if(k==l && l!= node.allGenes.size() - 1){
                        if(Settings.showBoxes) {
                            fac.drawRectangle(line_x - Settings.line_gap, chromosomeStart - Settings.line_gap,
                                line_x + Settings.line_gap, line_y + Settings.line_gap,
                                node.chromosomes.get(m).isCircular);
                        }
                        line_y += Settings.line_gap + Settings.gene_width(node.allGenes.get(k))
                            + Settings.chromosome_gap;
                        chromosomeStart = line_y;
                        m++;
                        l += node.chromosomes.get(m).genes.size();
                    } else line_y += Settings.line_gap + Settings.gene_width(node.allGenes.get(k));
                }
                k++;
            }
            if(Settings.showBoxes) {
                fac.drawRectangle(line_x - Settings.line_gap,
                    chromosomeStart - Settings.line_gap,
                    line_x + Settings.line_gap,
                    line_y - Settings.gene_width(node.allGenes.get(node.allGenes.size() - 1)),
                    node.chromosomes.get(m).isCircular);
            }
        } else {
            line_x = prev_x;
        }
        int chromosomeDiff = 0;
        if (!node.event.equals("root")) {
            chromosomeDiff = node.chromosomes.size() - node.ancestor.chromosomes.size();
        }

        line_y = first_y - ((1/2) * chromosomeDiff * Settings.chromosome_gap);

        for(Chromosome ch: node.chromosomes) {
            for (Integer a : ch.genes) {
                //fac.setLineColor(this.gene_col[Math.abs(a)]);
                node.gene_x_pos.add(line_y);
                if (Settings.is_draw(a)) {
                    fac.drawGeneLine(line_x, line_y, timedif_x, line_y, a);
                    line_y += Settings.line_gap + Settings.gene_width(a);
                }
            }
            line_y+= Settings.chromosome_gap;
        }

        line_y -= Settings.chromosome_gap;
        if(node.getFirst() == null && Settings.showLeafNames){
            fac.writeLeafName(node.name, line_x,
                line_y - node.calcNodeWidth() - Settings.line_gap);
        }

        if (node.ancestorNum == 1) {
            rek(node.getFirst(), timedif_x, first_y, block_y, block_w, fac);
        } else if (node.ancestorNum == 2) {
            double block_scale = Math.max(block_w / node.getWidth(), 1);
            int first_block = (int) (node.getFirst().getWidth() * block_scale);
            int second_block = (int) (node.getSecond().getWidth() * block_scale);
            rek(node.getFirst(), timedif_x, first_y, block_y, first_block, fac);
            rek(node.getSecond(), timedif_x, first_y, block_y + first_block, second_block, fac);
        }
    }

    int which_chromosome(EvolutionNode node, int pos){
        if(pos == -1) return pos;
        int which = -1;
        int counter = node.chromosomes.get(0).genes.size();
        for (int i = 0; i < node.chromosomes.size(); i++) {
            which++;
            if(pos < counter) return which;
            counter += node.chromosomes.get(i+1).genes.size();
        }
        return which;
    }

    private int getNewPos(int oldPos, int beforeId, int afterId, EvolutionNode node){
        if(oldPos == -1) return -1;
        int result = oldPos;
        for (int i = 0; i < node.chromosomes.size(); i++) {
            if(node.chromosomes.get(i).relativeOrderID < beforeId){
                result -= node.chromosomes.get(i).genePos.size();
            }
            if(i < afterId) result += node.chromosomes.get(i).genePos.size();
        }
        return result;
    }

    // zmeni genePos pre chromozomy v node1, aj cely genePos v node1 podla toho ako sa premiesal node2
    void changeGenePos(EvolutionNode node1, EvolutionNode node2, ArrayList<Integer> relativeChromosomesPos) {
        ArrayList<Integer> newGenePos = new ArrayList<>();
        int counter = node1.chromosomes.get(0).genePos.size();
        int which_ch = 0;
        ArrayList<Integer> newChromosomeGenePos = new ArrayList<>();
        for (int i = 0; i < node1.genePos.size(); i++) {
            if(i == counter){
                node1.chromosomes.get(which_ch).genePos = newChromosomeGenePos;
                newChromosomeGenePos = new ArrayList<>();
                which_ch++;
                counter+=node1.chromosomes.get(which_ch).genePos.size();
            }
            int beforeId = relativeChromosomesPos.get(i);
            int afterId = -1;
            for (int j = 0; j < node2.chromosomes.size(); j++) {
                if(node2.chromosomes.get(j).relativeOrderID == beforeId){
                    afterId = j;
                    break;
                }
            }
            int newPos = getNewPos(node1.genePos.get(i), beforeId, afterId, node2);
            newGenePos.add(newPos);
            newChromosomeGenePos.add(newPos);
        }
        node1.chromosomes.get(node1.chromosomes.size() - 1).genePos = newChromosomeGenePos;
        node1.genePos = newGenePos;
    }

    private void oneSidedOpt(Set<EvolutionNode> stableNodeSet, EvolutionNode unstableNode, boolean isBackwards){
        if(!isBackwards) {
            for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
                if (stableNodeSet == null) otocChromozomVKoreni(unstableNode, i);
                else {
                    ArrayList<Integer> result = rezatAOtocit(unstableNode.chromosomes.get(i), unstableNode.ancestor);
                    rez(unstableNode, i, result.get(0));
                    if (result.get(1) == 1) otoc(unstableNode, i);
                }
            }
        }

        if(stableNodeSet == null) return;

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
        if(!isBackwards) {
            if (unstableNode.getFirst() != null) {
                for (int i = 0; i < unstableNode.getFirst().genePos.size(); i++) {
                    relativeChromosomesPos1.add(which_chromosome(unstableNode, unstableNode.getFirst().genePos.get(i)));
                }
            }
            if (unstableNode.getSecond() != null) {
                for (int i = 0; i < unstableNode.getSecond().genePos.size(); i++) {
                    relativeChromosomesPos2.add(which_chromosome(unstableNode, unstableNode.getSecond().genePos.get(i)));
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
                int which_ch = which_chromosome(unstableNode, unstableNode.getFirst().genePos.get(i));
                relativeChromosomesPos1.add(which_ch);
                if(which_ch != -1) {
                    geneConnection.get(which_ch).add(i);
                }
            }
            if(stableNodeSet.size() == 2){
                for (int i = 0; i < unstableNode.getSecond().genePos.size(); i++) {
                    int which_ch = which_chromosome(unstableNode, unstableNode.getSecond().genePos.get(i));
                    relativeChromosomesPos2.add(which_ch);
                    if(which_ch != -1) {
                        geneConnection.get(which_ch).add(i);
                    }
                }
            }
        }

        //pocitanie score pre chromozomy v node
        for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
            for(Integer j : geneConnection.get(i)){
                unstableNode.chromosomes.get(i).score += j+1;
            }
            unstableNode.chromosomes.get(i).score /= geneConnection.get(i).size();
        }

        //pre kazdy chromozom v node si zapamatam ich aktualne poradie
        for (int i = 0; i < unstableNode.chromosomes.size(); i++) {
            unstableNode.chromosomes.get(i).relativeOrderID = i;
        }

        //sortovanie chromozomov v node
        (unstableNode.chromosomes).sort((o1, o2) -> {
            if(o1.score > o2.score) return 1;
            if(o1.score < o2.score) return -1;
            return 0;
        });

        normalizeNode(unstableNode, relativeChromosomesPos1, relativeChromosomesPos2);

    }

    private ArrayList<Integer> rezatAOtocit(Chromosome ch, EvolutionNode ancestor) {
        ArrayList<Integer> vysledokOtocenia = chcemOtacatOpt(ch, ancestor);
        ArrayList<Integer> result = new ArrayList<>();
        if(!ch.isCircular){
            result.add(0);
            if(vysledokOtocenia.get(0) == 1) result.add(1);
            else result.add(0);
            return result;
        }
        int najuspesnejsiRez = 0;
        boolean chcemOtocit;
        if(vysledokOtocenia.get(0) == 1) {
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
                if(vysledokOtocenia.get(0) == 1) {
                    chcemOtocit = true;
                } else chcemOtocit = false;
            }
        }

        result.add(najuspesnejsiRez);
        if(chcemOtocit) result.add(1);
        else result.add(0);
        return result;
    }

    private int spocitajKrizeniaPreChromozom(ArrayList<Integer> genePos) {
        int crossings = 0;
        for(int j = 0; j < genePos.size(); j++){
            for (int i = j+1; i < genePos.size(); i++) {
                if(genePos.get(i) == -1) continue;
                if(genePos.get(i) < genePos.get(j)) crossings++;
            }
        }
        return crossings;
    }

    private ArrayList<Integer> chcemOtacatOpt(Chromosome ch, EvolutionNode ancestor) {
        int myCrossings1 = spocitajKrizeniaPreChromozom(ch.genePos);

        ArrayList<Integer> newGenePos = new ArrayList<>();
        ArrayList<Integer> newGenes = new ArrayList<>();

        for (int i = ch.genePos.size() -1; i >= 0; i--) {
            if(ch.genePos.get(i) != -1) {
                int ancestorGene = ancestor.allGenes.get(ch.genePos.get(i));
                if ((ancestorGene >= 0 && ch.genes.get(i) < 0)
                    || (ancestorGene < 0 && ch.genes.get(i) >= 0)
                    ) myCrossings1++;
            }
            if(ch.genes.get(i) < 0) myCrossings1++;
            newGenePos.add(ch.genePos.get(i));
            newGenes.add(ch.genes.get(i) * (-1));
        }
        int myCrossings2 = spocitajKrizeniaPreChromozom(newGenePos);
        for (int i = ch.genePos.size() -1; i >= 0; i--) {
            if(newGenePos.get(i) != -1) {
                int ancestorGene = ancestor.allGenes.get(newGenePos.get(i));
                if ((ancestorGene >= 0 && newGenes.get(i) < 0)
                    || (ancestorGene < 0 && newGenes.get(i) >= 0)
                    ) myCrossings2++;
            }
            if(newGenes.get(i) < 0) myCrossings2++;
        }

        ArrayList<Integer> result = new ArrayList<>();
        if(myCrossings1 > myCrossings2){
            result.add(1);
            result.add(myCrossings2);
        } else {
            result.add(0);
            result.add(myCrossings1);
        }

        return result;
    }

    private void otocChromozomVKoreni(EvolutionNode node, int ktory){
        int reverseGenes = 0;
        Chromosome ch = node.chromosomes.get(ktory);
        for (int i = 0; i < ch.genes.size(); i++) {
            if(ch.genes.get(i) < 0){
                reverseGenes++;
            }
        }
        if(reverseGenes > ch.genes.size()/2) otoc(node, ktory);
    }

    void normalizeNode(EvolutionNode unstableNode, ArrayList<Integer> relativeChromosomesPos1, ArrayList<Integer> relativeChromosomesPos2) {
        // najdenie noveho genePos pre nasledovnikov node2
        if(unstableNode.getFirst()!= null) {
            changeGenePos(unstableNode.getFirst(), unstableNode, relativeChromosomesPos1);
        }
        if(unstableNode.getSecond()!= null) {
            changeGenePos(unstableNode.getSecond(), unstableNode, relativeChromosomesPos2);
        }
        // uz len oprava arraylistov allGenes a genePos + vynulovanie skore pre node2
        ArrayList<Integer> newAllGenes = new ArrayList<>();
        ArrayList<Integer> newGenePos = new ArrayList<>();
        for(Chromosome ch: unstableNode.chromosomes){
            newAllGenes.addAll(ch.genes);
            newGenePos.addAll(ch.genePos);
            ch.score = 0;
        }
        unstableNode.allGenes = newAllGenes;
        unstableNode.genePos = newGenePos;
    }

    EvolutionTree level_by_level_sweep(){
        if(this.getRoot() == null) return null;
        int counter = 0;
        int bestCrossings = countCrossings(this.getRoot());
        System.out.println(bestCrossings);
        EvolutionTree bestTree = this.copy();
        int lastCrossingNumber = Integer.MAX_VALUE;
        while(!(counter == 5)){
            optimalizuj(this.getRoot());
            int crossingNumber = countCrossings(this.getRoot());
            if(crossingNumber == lastCrossingNumber) break;
            if(crossingNumber > lastCrossingNumber) counter++;
            if(crossingNumber < bestCrossings){
                bestCrossings = crossingNumber;
                bestTree = this.copy();
            }
            lastCrossingNumber = crossingNumber;
        }
        System.out.println(bestCrossings);
        return bestTree;
    }


    private void optimalizuj(EvolutionNode node) {
        if (node.ancestor != null) {
            Set<EvolutionNode> set = new HashSet<>();
            set.add(node.ancestor);
            oneSidedOpt(set, node, false);
        } else oneSidedOpt(null, node, false);

        if (node.getFirst() != null) {
            Set<EvolutionNode> set = new HashSet<>();
            set.add(node.getFirst());
            optimalizuj(node.getFirst());
            if (node.getSecond() != null) {
                set.add(node.getSecond());
                optimalizuj(node.getSecond());
            }
            oneSidedOpt(set, node, true);
        }
    }

    Integer countCrossings(EvolutionNode node) {
        int sum = 0;
        if (node.getFirst() != null) {
            sum = countCrossings2Layers(node, node.getFirst()) + countCrossings(node.getFirst());
            if(node.getSecond() != null) {
                sum += countCrossings2Layers(node, node.getSecond()) + countCrossings(node.getSecond());
            }
        }
        return sum;
    }

    private int countCrossings2Layers(EvolutionNode node1, EvolutionNode node2) {
        //if(node2 == null) return 0;
        int crossings = 0;
        for (int i = 0; i < node2.genePos.size(); i++) {
            int from = node2.genePos.get(i);
            if(from!= -1) {
                int geneFrom = node1.allGenes.get(from);
                int geneTo = node2.allGenes.get(i);
                //ak nastala zmena orientacie, tiez sa to berie ako crossing
                if ((geneFrom < 0 && geneTo > 0) || (geneFrom > 0 && geneTo < 0)) {
                    crossings++;
                }
                if(geneFrom < 0 && node1.event.equals("root")) crossings++;
            }
            if(node2.allGenes.get(i) < 0) crossings++;
            for (int j = i+1; j < node2.genePos.size(); j++){
                int newFrom = node2.genePos.get(j);
                if(newFrom != -1 && newFrom < from) crossings++;
            }
        }
        return crossings;
    }

    void otoc(EvolutionTree.EvolutionNode node, int ktory){
        ArrayList<Integer> newChromosomeGenePos = new ArrayList<>();
        ArrayList<Integer> newChromosomeGenes = new ArrayList<>();
        Chromosome ch = node.chromosomes.get(ktory);
        for (int i = ch.genePos.size() -1; i >= 0; i--) {
            newChromosomeGenePos.add(ch.genePos.get(i));
            newChromosomeGenes.add(ch.genes.get(i)*(-1));
        }
        int fromId = 0;
        for (int i = 0; i < ktory; i++) {
            fromId += node.chromosomes.get(i).genes.size();
        }
        int toId = fromId + ch.genes.size() - 1;

        zmenGenePosPoOtacani(node.getFirst(), fromId, toId);
        zmenGenePosPoOtacani(node.getSecond(), fromId, toId);

        ArrayList<Integer> newAllGenes = new ArrayList<>();
        ArrayList<Integer> newGenePos = new ArrayList<>();
        for (int i = 0; i < node.chromosomes.size(); i++) {
            if(i == ktory){
                ch.genePos = newChromosomeGenePos;
                ch.genes = newChromosomeGenes;
            }
            newAllGenes.addAll(node.chromosomes.get(i).genes);
            newGenePos.addAll(node.chromosomes.get(i).genePos);
        }
        node.allGenes = newAllGenes;
        node.genePos = newGenePos;
    }

    private void zmenGenePosPoOtacani(EvolutionTree.EvolutionNode node, int fromId, int toId){
        if(node == null) return;

        ArrayList<Integer> newGenePos = new ArrayList<>();
        int counter = node.chromosomes.get(0).genePos.size();
        int which_ch = 0;
        ArrayList<Integer> newChromosomeGenePos = new ArrayList<>();
        for (int i = 0; i < node.genePos.size(); i++) {
            if(i == counter){
                node.chromosomes.get(which_ch).genePos = newChromosomeGenePos;
                newChromosomeGenePos = new ArrayList<>();
                which_ch++;
                counter += node.chromosomes.get(which_ch).genePos.size();
            }

            if(node.genePos.get(i) >= fromId && node.genePos.get(i) <= toId){
                int newPos = toId - node.genePos.get(i) + fromId;
                newGenePos.add(newPos);
                newChromosomeGenePos.add(newPos);
            } else {
                newGenePos.add(node.genePos.get(i));
                newChromosomeGenePos.add(node.genePos.get(i));
            }
        }
        node.chromosomes.get(node.chromosomes.size() - 1).genePos = newChromosomeGenePos;
        node.genePos = newGenePos;
    }

    void rez(EvolutionTree.EvolutionNode node, int ktory, int kde){
        if(!node.chromosomes.get(ktory).isCircular) return;
        if(kde == 0) return;

        ArrayList<Integer> newChromosomeGenePos = new ArrayList<>();
        ArrayList<Integer> newChromosomeGenes = new ArrayList<>();
        Chromosome ch = node.chromosomes.get(ktory);
        for (int i = kde; i < ch.genePos.size(); i++) {
            newChromosomeGenePos.add(ch.genePos.get(i));
            newChromosomeGenes.add(ch.genes.get(i));
        }
        for (int i = 0; i < kde; i++) {
            newChromosomeGenePos.add(ch.genePos.get(i));
            newChromosomeGenes.add(ch.genes.get(i));
        }

        int fromId = 0;
        for (int i = 0; i < ktory; i++) {
            fromId += node.chromosomes.get(i).genes.size();
        }
        int toId = fromId + ch.genes.size() - 1;

        zmenGenePosPoRezani(node.getFirst(), fromId, toId, kde);
        zmenGenePosPoRezani(node.getSecond(), fromId, toId, kde);

        ArrayList<Integer> newAllGenes = new ArrayList<>();
        ArrayList<Integer> newGenePos = new ArrayList<>();
        for (int i = 0; i < node.chromosomes.size(); i++) {
            if(i == ktory){
                ch.genePos = newChromosomeGenePos;
                ch.genes = newChromosomeGenes;
            }
            newAllGenes.addAll(node.chromosomes.get(i).genes);
            newGenePos.addAll(node.chromosomes.get(i).genePos);
        }
        node.allGenes = newAllGenes;
        node.genePos = newGenePos;
    }

    private void zmenGenePosPoRezani(EvolutionTree.EvolutionNode node, int fromId, int toId, int kde) {
        if(node == null) return;

        ArrayList<Integer> newGenePos = new ArrayList<>();
        int counter = node.chromosomes.get(0).genePos.size();
        int which_ch = 0;
        ArrayList<Integer> newChromosomeGenePos = new ArrayList<>();
        for (int i = 0; i < node.genePos.size(); i++) {
            if(i == counter){
                node.chromosomes.get(which_ch).genePos = newChromosomeGenePos;
                newChromosomeGenePos = new ArrayList<>();
                which_ch++;
                counter += node.chromosomes.get(which_ch).genePos.size();
            }

            if(node.genePos.get(i) >= fromId && node.genePos.get(i) < fromId + kde) {
                int newPos = node.genePos.get(i) + (toId - fromId - kde + 1);
                newGenePos.add(newPos);
                newChromosomeGenePos.add(newPos);
            } else if(node.genePos.get(i) >= (fromId + kde) && node.genePos.get(i) <= toId){
                int newPos = node.genePos.get(i) - kde;
                newGenePos.add(newPos);
                newChromosomeGenePos.add(newPos);
            } else {
                newGenePos.add(node.genePos.get(i));
                newChromosomeGenePos.add(node.genePos.get(i));
            }
        }
        node.chromosomes.get(node.chromosomes.size() - 1).genePos = newChromosomeGenePos;
        node.genePos = newGenePos;
    }
}
