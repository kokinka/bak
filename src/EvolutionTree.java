
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
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
        private ArrayList<Integer> genes;
        private ArrayList<Integer> genePos;
        private ArrayList<Integer> gene_x_pos;
        private ArrayList<Integer> blockNumAncF;
        private ArrayList<Integer> blockNumAncS;
        private ArrayList<Integer> blockNumDes;
        private HashMap<Integer, Integer> blockWidth;
        private double next;
        private int ancestorNum;
        private int width;

        public EvolutionNode(String name, String id, EvolutionNode ancestor, String event, double time, ArrayList<Integer> genes, ArrayList<Integer> genePos) {
            this.name = name;
            this.id = id;
            this.ancestor = ancestor;
            this.event = event;
            this.time = time;
            this.genes = genes;
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
            for (Integer a : this.genes) {
                if (Settings.is_draw(a)) {
                    i = i + Settings.line_gap + Settings.gene_width(a);
                }
            }
            if (i > 0) {
                i = i - Settings.line_gap;
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
        ArrayList genes = new ArrayList();
        ArrayList geneorder = new ArrayList();
        int i = (splitted.length - 6) / 2;
        for (int j = 5; j < 5 + i; j++) {
            genes.add(Integer.parseInt(splitted[j]));
            this.genes.add(Math.abs(Integer.parseInt(splitted[j])));
        }
        for (int j = 6 + i; j < splitted.length; j++) {
            geneorder.add(Integer.parseInt(splitted[j]));
        }
        parsed = new EvolutionTree.EvolutionNode(name, id, ancestor, event, time, genes, geneorder);
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
        rek(this.getRoot(), 5, 0, 0, Settings.height, fac);
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
        for (int a : anc.genes) {
            anc.getancblock(firstbool).add(i);
            i++;
        }
        int j = 0;
        for (int a : des.genes) {
            int ancestor = des.genePos.get(j);
            if (ancestor >= 0) {
                int ancblock = anc.getancblock(firstbool).get(ancestor);
                if (a != anc.genes.get(ancestor)) {
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
    private void rekBlok(EvolutionNode node) {
        if (node.ancestorNum > 0) {

            // brat i je zle , treba prej pre oba anc + desc prveho a desc  druheho --- > vytvorit metodu ktora papa list genov,list blokov a list sirok blokov a napcha ich do universu a Sets          
            this.fillBlocks(node.genes, node.getancblock(true), node.blockWidth);
            this.fillBlocks(node.getFirst().genes, node.getFirst().blockNumDes, node.blockWidth);
            this.rekBlok(node.getFirst());
            if (node.ancestorNum == 2) {
                this.fillBlocks(node.genes, node.getancblock(false), node.blockWidth);
                this.fillBlocks(node.getSecond().genes, node.getSecond().blockNumDes, node.blockWidth);
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
        int timedif_x;
        int line_y;
        double line_x;
        node.gene_x_pos = new ArrayList();
        first_y = block_y + (block_w - node.calcNodeWidth()) / 2;
        timedif_x = (int) (Math.max(node.next - Settings.time_diff, node.time) * Settings.scale_x);
        line_x = node.time * Settings.scale_x;
        if (!node.event.equals("root")) {
            line_y = first_y;
            int k = 0;
            for (int a : node.genePos) {

                // fac.setLineColor(this.gene_col[Math.abs(node.ancestor.genes.get(a))]);
                if (Settings.is_draw(node.genes.get(k))) {
                    if (a != -1) {
                        fac.drawGeneLine(prev_x, node.ancestor.gene_x_pos.get(a), line_x, line_y, node.genes.get(k));
                    }
                    line_y += Settings.line_gap + Settings.gene_width(node.genes.get(k));
                }

                k++;
            }
        } else {
            line_x = prev_x;
        }
        line_y = first_y;

        for (Integer a : node.genes) {
            //fac.setLineColor(this.gene_col[Math.abs(a)]);
            node.gene_x_pos.add(line_y);
            if (Settings.is_draw(a)) {
                fac.drawGeneLine(line_x, line_y, timedif_x, line_y, a);
                line_y += Settings.line_gap + Settings.gene_width(a);
            }

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

}
