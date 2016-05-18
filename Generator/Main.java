/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/// zjednosdusit,potom scucnut
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
///inversion chyba
class HistoryGenerator {
    Random rn = new Random();
    int node_numb = 5;
    int gene_numb = 4;
    double event_per_node = 1;
    double geom_chance = 0.437758;
    private int max_evets = 1;
    double len_rate = 0.75;
    boolean duplication = true;
    boolean inversion = true;
    boolean deletion = false;
    boolean transposition = true;
    boolean insertion = false;
    double dup_rate = 0;
    double ins_rate = 0;
    double inv_rate = 0;
    double trans_rate = 0;
    double del_rate = 0;
    int max_inserted = 10;
    int max_deleted = 10;
    String title = "Output";
    int next_gene_numb = gene_numb+1;
    LinkedList<Event> distribution = new LinkedList();

    class Event {

        String type;
        double rate_start;
        double rate_stop;

        public Event(String type, double rate_start, double rate_stop) {
            this.type = type;
            this.rate_start = rate_start;
            this.rate_stop = rate_stop;
        }

    }

    public HistoryGenerator(String _title) {
        title=_title;
    }

    class Transposed {

        int start;
        int amount;

        public Transposed(int start, int amount) {
            this.start = start;
            this.amount = amount;
        }

    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
   
    public void go(){
        event_rate();
        this.geom_chance = find_ratio(0,1,this.event_per_node,this.max_evets,20);
        try {
            generate();
        } catch (IOException ex) {
            Logger.getLogger(HistoryGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    double get_geom_sum(double base, double ratio, double members) {
        double sum = base;
        for (int i = 1; i < members; i++) {
            sum = sum + base * Math.pow(ratio, i);
        }
        return sum;
    }

    int geom_dist(double rate, int max) {
        
        int ret = 1;
        while ((rn.nextDouble() < rate) && (ret < max)) {
            ret++;
        }
        return ret;
    }
    void parse(String s){
        String[] splitted = s.split(" ");
        switch (splitted[0]) {
            case "duplication":
                if(splitted[1].equals("0")){
                    duplication = false;
                }else{
                    duplication = true;
                    dup_rate = Double.valueOf(splitted[1]);
                }
                break;
            case "inversion":
                if(splitted[1].equals("0")){
                    inversion = false;
                }else{
                    inversion = true;
                    inv_rate = Double.valueOf(splitted[1]);
                }
                break;
            case "deletion":
                if(splitted[1].equals("0")){
                    deletion = false;
                }else{
                    deletion = true;
                    del_rate = Double.valueOf(splitted[1]);
                    max_deleted = Integer.valueOf(splitted[2]);
                }
                break;
            case "insertion":
                if(splitted[1].equals("0")){
                    insertion = false;
                }else{
                    insertion = true;
                    ins_rate = Double.valueOf(splitted[1]);
                    max_inserted = Integer.valueOf(splitted[2]);
                }
                break;
            case "transposition":
                if(splitted[1].equals("0")){
                    transposition = false;
                }else{
                    transposition = true;
                    trans_rate = Double.valueOf(splitted[1]);
                }
                break;
            case "node_numb":
                node_numb = Integer.parseInt(splitted[1]);
                break;
            case "gene_numb":
                gene_numb =Integer.parseInt(splitted[1]);
                next_gene_numb = gene_numb +1;
                break;
            case "len_rate":
                len_rate = Double.valueOf(splitted[1]);
                break;
            case "title":
                title = splitted[1];
                
                
        }
    }
    
    void settitle(String s){
        title=s;
    }

     void event_rate() {
        double total_rate = 0;
        if (this.deletion) {
            total_rate = total_rate + this.del_rate;
        }
        if (this.duplication) {
            total_rate = total_rate + this.dup_rate;
        }
        if (this.inversion) {
            total_rate = total_rate + this.inv_rate;
        }
        if (this.insertion) {
            total_rate = total_rate + this.ins_rate;
        }
        if (this.transposition) {
            total_rate = total_rate + this.trans_rate;
        }
        double mapping = 1 / total_rate;
        double begin = 0;
        double end;
        if (this.deletion) {
            end = this.del_rate * mapping;
            this.distribution.add(new Event("deletion", begin, end));
            begin = end;
        }
        if (this.duplication) {
            end = begin + this.dup_rate * mapping;
            this.distribution.add(new Event("duplication", begin, end));
            begin = end;
        }
        if (this.transposition) {
            end = begin + this.trans_rate * mapping;
            this.distribution.add(new Event("transposition", begin, end));
            begin = end;
        }
        if (this.inversion) {
            end = begin + this.inv_rate * mapping;
            this.distribution.add(new Event("inversion", begin, end));
            begin = end;
        }
        if (this.insertion) { Random rn = new Random();
            end = begin + this.ins_rate * mapping;
            this.distribution.add(new Event("insertion", begin, end));
            begin = end;
        }
    }

    String get_event() {
        
        double pick = rn.nextDouble();
        for (Event e : this.distribution) {
            if (pick < e.rate_stop) {
                return e.type;
            }
        }
        System.out.println("neplatne vybrany prvok"+pick);
        return "duplication";
        
    }

    boolean legal_trans_pos(int i, ArrayList surv) {
        return true;
        /*
        if (i <= 0) {
            return true;
        }
        if (i >= surv.size()) {
            return true;
        }
        if (!surv.get(i - 1).equals(surv.get(i))) {
            return true;
        }
        */
        //return false;
    }

    double find_ratio(double lower_bound, double upper_bound, double expected_freq,int members, int itterations_left) {
        double ratio = lower_bound + (upper_bound - lower_bound) / 2;
        if (itterations_left == 0) {
            return ratio;
        }
        double sum = get_geom_sum(1, ratio, members);
        if (sum == expected_freq) {
            return ratio;
        }
        if (sum < expected_freq) {
            return find_ratio(ratio, upper_bound, expected_freq,members, itterations_left - 1);
        } else {
            return find_ratio(lower_bound, ratio, expected_freq,members, itterations_left - 1);
        }

    }

    void generate() throws IOException {
        //inicializacia + prvy riadok
        File file = new File(title + ".history");
        file.delete();
        file.createNewFile();
        String line = "testnode0 n0 root 0 root";
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        ArrayList genes = new ArrayList();
        for (int i = 1; i < this.gene_numb+1; i++) {
            line = line.concat(" " + i);
            genes.add(i);
        }
        line = line.concat(" #");
        for (int i = 1; i < this.gene_numb+1; i++) {
            line = line.concat(" -1");
        }
        bw.write(line);
        bw.newLine();
        
        
        for (int i = 1; i <= this.node_numb; i++) {
            
            line = "testnode" + i + " n" + i + " n" + (i - 1) + " " + i + " special";
            int event_num = 1;
            
            //generate start point 
            Set starts = new TreeSet();
                int picked = rn.nextInt(genes.size());
                starts.add(picked);
            
            ArrayList new_genes = new ArrayList();
            ArrayList<Integer> event_survivor = new ArrayList();
            ArrayList gene_anc = new ArrayList();
            int survivor_num = 0;
            

            Queue<Transposed> to_translocate = new LinkedList();
            for (int j = 0; j < genes.size(); j++) {
                if (starts.contains(j)) {
                    survivor_num++;
                    int amount;
                    int max_last = genes.size() + 1;        //genes.size - j           
                    switch (get_event()) {
                        case "inversion":
                            //inverzia
                            amount = geom_dist(this.len_rate, max_last - j - 1);
                                for (int k = j + amount -1 ; k >= j; k--) {
                                    new_genes.add(((int) genes.get(k))*-1);
                                    gene_anc.add(k);
                                    event_survivor.add(survivor_num);
                                }
                            
                            j = j + amount - 1;
                            break;
                        case "duplication":
                            //moznost hodit jednu vetvu duplikacie zachovat a druhu hodit na  lubovolne miesto ako trasnlokaciu
                            //momentale hodi zduplikovane casti hned vedla seba
                            amount = geom_dist(this.len_rate, max_last - j - 1);
                            for (int l = 0; l < 2; l++) {
                                for (int k = j; k < j + amount; k++) {
                                    new_genes.add(genes.get(k));
                                    gene_anc.add(k);
                                    event_survivor.add(survivor_num);
                                }
                            }
                            j = j + amount - 1;
                            break;
                        case "insertion":
                            //neinsertujem na uplny zaciatok , uplny koniec ano,insertujem ZA gen j kotry len skopirujem
                            amount = geom_dist(this.len_rate, this.max_inserted);
                            new_genes.add(genes.get(j));
                            gene_anc.add(j);
                            event_survivor.add(survivor_num-1);
                            for (int k = j; k < j + amount; k++) {
                                new_genes.add(this.next_gene_numb);
                                this.next_gene_numb++;
                                gene_anc.add(-1);
                                event_survivor.add(survivor_num);
                            }
                            break;
                        case "transposition":
                            amount = geom_dist(this.len_rate, max_last - j-1);
                            to_translocate.add(new Transposed(j, amount));
                            j = j + amount - 1;
                            break;
                        case "deletion":
                            amount = geom_dist(this.len_rate, max_last - j-1);
                            amount = Math.min(amount, max_deleted);
                            amount = Math.min(amount, genes.size()-1);
                            if(amount == 0){
                                new_genes.add(genes.get(j));
                                gene_anc.add(j);
                                event_survivor.add(survivor_num);
                            }else{
                                j = j + amount - 1;
                            }
                            break;
                    }
                    survivor_num++;
                } else {
                    new_genes.add(genes.get(j));
                    gene_anc.add(j);
                    event_survivor.add(survivor_num);
                }
            }
            
            
            while (!to_translocate.isEmpty()) {
                Transposed t = to_translocate.poll();
                int selected = rn.nextInt(new_genes.size() + 1);
                while (!legal_trans_pos(selected, event_survivor)) {
                    selected = rn.nextInt(new_genes.size() + 1);
                }
                //idem od zadu aby som mohol vzdy vlozit na miesto selected a dalsi gen ho posunul
                for (int l = t.start + t.amount - 1; l >= t.start; l--) {
                    new_genes.add(selected, genes.get(l));
                    gene_anc.add(selected, l);
                }
            }
            //dokoncit vystup
            for (Object a:new_genes) {
                int casted = (int) a;
                line = line.concat(" "+ casted);
            }
            line = line.concat(" #");
            for (Object a:gene_anc) {
                int casted = (int) a;
                line = line.concat(" "+casted);
            }
            genes=new_genes;
            bw.write(line);
            bw.newLine();

        }
        bw.close();
        fw.close();
    }
    

}
public class Main {
     public static void main(String[] args) throws IOException {
       String settings_file =args[0];
       File f = new File(settings_file);

       HistoryGenerator hisge = new HistoryGenerator(f.getName().replaceAll(".generator", ""));
       int times = 1;
       if(args.length>1){
           times = Integer.parseInt(args[1]);
       }
       Scanner file_reader = new Scanner(f);
       while(file_reader.hasNextLine()){
           String line = file_reader.nextLine();
           hisge.parse(line);
       }
       String title = hisge.title.replaceAll(".generator", "");
       title = hisge.title.replaceAll("#.*", "");
           hisge.next_gene_numb = hisge.gene_numb +1;
           for(int i=1;i<=times;i++){
           hisge.settitle(title+"#"+i);
           hisge.go();
           }
      
    }
    
}
