/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

/**
 *
 * @author david
 */
public class ToGenerator {

    public static void main(String[] args) throws IOException {
       int[] pole10 = {10,20,30,40,50,100};
       int[] pole100viac = {10,50,100,150,200,500,1000};
       int[] nodes = {100,200};
       double[] sance = {0.7,0.8,0.9,0.95,0.97};
       String[] typy = {"all","del","ins","trans","dup","inv","nodelnoins"};
       String[] ine = {"all","nodelnoins"};
       TreeMap<Integer,int[]> map = new TreeMap();
       TreeMap<Integer,String[]> ktore = new TreeMap();
       map.put(10, pole10);
       map.put(100,pole100viac);
       map.put(200,pole100viac);
       ktore.put(10, typy);
       ktore.put(50, typy);
       ktore.put(100, typy);
       ktore.put(500, typy);

       
       for(int uu:map.keySet()){
       for(int a:map.get(uu)){
           for(double r:sance){
               String[] cerpa = ine;
               if(ktore.containsKey(a)){
                   cerpa = ktore.get(a);
               }
               for(String s:cerpa){
                   String str=s+""+a+"x"+uu+"x"+r+".generator";
                   File f = new File(str);
                   f.delete();
                   f.createNewFile();
                    FileWriter fw = new FileWriter(f.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                   String del="0 10";
                   String ins="0 10";
                   String trans="0";
                   String dup="0";
                   String inv="0";
                   switch(s){
                       case "all":
                           del ="1 50";
                           ins ="1 50";                                                     
                       case "nodelnoins":
                           trans="1";
                           dup="1";
                           inv="1"; 
                           break;
                       case "del":
                           del ="1 50";
                           break;
                       case "ins":
                           ins = "1 50";
                           break;
                       case "trans":
                           trans ="1";
                           break;
                       case "dup":
                           dup = "1";
                           break;
                       case "inv":
                           inv = "1";
                           break;
                   }
                   bw.write("node_numb "+uu);
                   bw.newLine();
                   bw.write("gene_numb "+a);
                   bw.newLine();
                   int sanc = (int) (r*100);
                   bw.write("len_rate "+sanc);
                   bw.newLine();
                   bw.write("duplication "+dup);
                   bw.newLine();
                   bw.write("inversion "+inv);
                   bw.newLine();
                   bw.write("deletion "+del);
                   bw.newLine();
                   bw.write("insertion "+ins);
                   bw.newLine();
                   bw.write("transposition "+trans);
                   bw.close();
                   fw.close();                   
               }               
           }
       }
       }  
    }
    
}
