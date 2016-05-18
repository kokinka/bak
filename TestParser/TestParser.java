/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author david
 */
public class TestParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
       String s = args[0];
       int i=1;
       File out= new File(s+".data");
       out.delete();
       out.createNewFile();
       FileWriter fw = new FileWriter(out);
        BufferedWriter bw = new BufferedWriter(fw);
       File generator = new File(s+".generator");
       Scanner generatr= new Scanner(generator);
       int node_numb = 0;
       int gene_numb = 0;
       double p =0;
       while(generatr.hasNextLine()){
           String line = generatr.nextLine();
           String[] parsed = line.split(" ");
           if(parsed[0].equals("node_numb")){
               node_numb = Integer.parseInt(parsed[1]);
           }
           if(parsed[0].equals("gene_numb")){
               gene_numb = Integer.parseInt(parsed[1]);
           }
            if(parsed[0].equals("len_rate")){
               p = Double.parseDouble(parsed[1]);
           }
      
       }
        String lenrate = (String) String.format("%.2f", p);
       File f = new File(s+"#"+i+".grepped");
       bw.write("udalosti,geny,p,poradie,velkost Univerza,pocet subsetov,greedy,greedy-cas,ilp,ilp-cas");
       bw.newLine();
       double ilp_tot = 0;
       double greed_tot = 0;
       double ilp_time_tot = 0;
       double greed_time_tot = 0;
       double universe_size_tot = 0;
       double subset_amount_tot = 0;
       while(f.exists()){
           Scanner in = new Scanner(f);
           Double load_user = in.nextDouble();
           Double load_sys = in.nextDouble();
           int universe_size = in.nextInt();
           int subset_amount = in.nextInt();
           int greedy = in.nextInt();
           Double greedy_user = in.nextDouble();
           Double greedy_sys = in.nextDouble();
           int ilp = in.nextInt();
           Double ilp_export_user = in.nextDouble();
           Double ilp_export_sys =in.nextDouble();
           Double cplex_user = in.nextDouble();
           Double cplex_sys = in.nextDouble();
           Double ilp_time =ilp_export_sys + ilp_export_user + cplex_sys + cplex_user ;// - (load_sys + load_user);
           universe_size_tot = universe_size_tot + universe_size;
           subset_amount_tot = subset_amount_tot + subset_amount;
           Double greedy_time =greedy_sys + greedy_user ;// - (load_sys + load_user);
           String output = String.format(",%d,%d,%d,%.4f,%d,%.4f",universe_size,subset_amount,greedy,greedy_time,ilp,ilp_time);
           bw.write(node_numb+","+gene_numb+","+lenrate+","+i+output);
           bw.newLine();
           ilp_tot = ilp_tot+ilp;
           greed_tot = greed_tot + greedy;
           ilp_time_tot = ilp_time_tot+ilp_time;
           greed_time_tot = greed_time_tot + greedy_time;
           
           i++;
           f = new File(s+"#"+i+".grepped");
       }
       i--;
       String output = String.format(",%.2f,%.2f,%.2f,%.4f,%.2f,%.4f",universe_size_tot/i,subset_amount_tot/i,greed_tot/i,greed_time_tot/i,ilp_tot/i,ilp_time_tot/i);
           bw.write(node_numb+","+gene_numb+","+lenrate+",avg"+output);
           bw.newLine();
       bw.close();
       fw.close();
    }
    
}
