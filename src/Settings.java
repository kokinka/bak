
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
class Settings {

    public static double time_diff = 0.3;
    public static int width = 800;
    public static int height = 800;
    public static int real_height = 800;
    public static double scale = 1;
    public static int line_gap = 8;
    public static int line_size = 2;
    public static int node_gap = 20;
    public static int chromosome_gap = 15;
    public static double scale_x;
    public static double saturation = 0.99;
    public static double brightness = 0.75;
    public static String title;
    public static boolean redraw = true;
    static private Color[] gene_col;
    public static int optimized = 2; //0 - delete, 1-leave transparent,2 leave unhighlighted
    static public boolean default_draw = true; //exp
    static public boolean default_transparent =false ;
    static public boolean default_highlighted =true;
    /*private*/ static TreeMap<Integer, GeneMeta> gene_meta = new TreeMap<Integer, GeneMeta>();
    static Paint nonhighlighted = Color.LIGHTGRAY;
    public static boolean selectable_genes = true;

    private static List<Integer> genes;

    public static void loadXML(String file_name) {
        File f= new File(file_name);
        Settings.loadXML(f);
    }
    public static void exportXML(File f){
           Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element root = dom.createElement("Settings");
        dom.appendChild(root);
        Element globals = getGlobalsXML(dom);
        root.appendChild(globals);
        if (gene_meta != null) {
            Element genes = dom.createElement("Genes");
            for (GeneMeta a : gene_meta.values()) {
                Element gene = a.getXML(dom);
                genes.appendChild(gene);
            }
            root.appendChild(genes);
        }


        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(dom);
            StreamResult result = new StreamResult(f);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void exportXML() {
    exportXML(new File(Settings.title + ".xml"));
    }

    private static Element getGlobalsXML(Document dom) {
        Element settings = dom.createElement("Globals");

        Element timedif = dom.createElement("TimeDiff");
        Text timedif_text = dom.createTextNode("" + Settings.time_diff);
        timedif.appendChild(timedif_text);
        settings.appendChild(timedif);

        Element width = dom.createElement("Width");
        Text width_text = dom.createTextNode("" + Settings.width);
        width.appendChild(width_text);
        settings.appendChild(width);

        Element height = dom.createElement("Height");
        Text height_text = dom.createTextNode("" + Settings.height);
        height.appendChild(height_text);
        settings.appendChild(height);

        Element lineGap = dom.createElement("LineGap");
        Text lineGap_text = dom.createTextNode("" + Settings.line_gap);
        lineGap.appendChild(lineGap_text);
        settings.appendChild(lineGap);

        Element line_size = dom.createElement("LineSize");
        Text line_size_text = dom.createTextNode("" + Settings.line_size);
        line_size.appendChild(line_size_text);
        settings.appendChild(line_size);

        Element node_gap = dom.createElement("NodeGap");
        Text node_gap_text = dom.createTextNode("" + Settings.node_gap);
        node_gap.appendChild(node_gap_text);
        settings.appendChild(node_gap);

        Element chromosome_gap = dom.createElement("ChromosomeGap");
        Text chromosome_gap_text = dom.createTextNode("" + Settings.chromosome_gap);
        chromosome_gap.appendChild(chromosome_gap_text);
        settings.appendChild(chromosome_gap);

        Element saturation = dom.createElement("Saturation");
        Text saturation_text = dom.createTextNode("" + Settings.saturation);
        saturation.appendChild(saturation_text);
        settings.appendChild(saturation);

        Element brightness = dom.createElement("Brightness");
        Text brightness_text = dom.createTextNode("" + Settings.brightness);
        brightness.appendChild(brightness_text);
        settings.appendChild(brightness);

        Element title = dom.createElement("Title");
        Text title_text = dom.createTextNode("" + Settings.title);
        title.appendChild(title_text);
        settings.appendChild(title);

        Element optimized = dom.createElement("Optimized");
        Text text_optimized = dom.createTextNode("" + Settings.optimized);
        optimized.appendChild(text_optimized);
        settings.appendChild(optimized);

        Element highlighted = dom.createElement("Highlighted");
        Text text_highlighted = dom.createTextNode(""+default_highlighted);
        highlighted.appendChild(text_highlighted);
        settings.appendChild(highlighted);

        Element transparent = dom.createElement("Transparent");
        Text text_transparent = dom.createTextNode(""+default_transparent);
        transparent.appendChild(text_transparent);
        settings.appendChild(transparent);

        Element draw = dom.createElement("Draw");
        Text text_draw = dom.createTextNode(""+default_draw);
        draw.appendChild(text_draw);
        settings.appendChild(draw);

        Element non_highlighted = dom.createElement("Nonhighlighted");
        Text text_nonhighlighted = dom.createTextNode(""+Settings.nonhighlighted);
        non_highlighted.appendChild(text_nonhighlighted);
        settings.appendChild(non_highlighted);

        return settings;
    }

    private static void setGlobals(Node globals) {
        NodeList nl = globals.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node process = nl.item(i);
            switch (process.getNodeName()) {
                case "TimeDiff":
                    Settings.time_diff = Double.parseDouble(process.getTextContent());
                    break;
                case "Width":
                    Settings.width = Integer.parseInt(process.getTextContent());
                    break;
                case "Height":
                    Settings.height = Integer.parseInt(process.getTextContent());
                    break;
                case "LineGap":
                    Settings.line_gap = Integer.parseInt(process.getTextContent());
                    break;
                case "LineSize":
                    Settings.line_size = Integer.parseInt(process.getTextContent());
                    break;
                case "NodeGap":
                    Settings.node_gap = Integer.parseInt(process.getTextContent());
                    break;
                case "ChromosomeGap":
                    Settings.chromosome_gap = Integer.parseInt(process.getTextContent());
                    break;
                case "Saturation":
                    Settings.saturation = Double.parseDouble(process.getTextContent());
                    break;
                case "Brightness":
                    Settings.brightness = Double.parseDouble(process.getTextContent());
                    break;
                case "Title":
                    Settings.title = process.getTextContent();
                    break;
                case "Optimized":
                    Settings.optimized = Integer.parseInt(process.getTextContent());
                    break;
                case "Highlighted":
                    Settings.default_highlighted = Boolean.parseBoolean(process.getTextContent());
                    break;
                case "Transparent":
                     Settings.default_transparent = Boolean.parseBoolean(process.getTextContent());
                    break ;
                case "Draw":
                        Settings.default_draw = Boolean.parseBoolean(process.getTextContent());
                    break;
                case "Nonhighlighted":
                        Settings.nonhighlighted = Paint.valueOf(process.getTextContent());
                        break;

            }
        }
    }

    static void loadXML(File file) {
        Document dom = null;
        try {

            FileReader XML_File = null;
            try {
                XML_File = new FileReader(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            }
            BufferedReader reader = new BufferedReader(XML_File);
            StringBuffer result = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line.trim());
            }
            String s = result.toString();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(s.getBytes());
            dom = dBuilder.parse(is);
            dom.getDocumentElement().normalize();
        } catch (SAXException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        Node genes = dom.getElementsByTagName("Genes").item(0);
        if (genes != null) {
            NodeList gene = genes.getChildNodes();
            if (gene.getLength() > 0) {
                for (int i = 0; i < gene.getLength(); i++) {
                    Node to_process = gene.item(i);
                    GeneMeta m;
                    m = new GeneMeta(to_process);
                    Settings.gene_meta.put(m.getGene_num(), m);
                }
            }
        }
        Node globals = dom.getElementsByTagName("Globals").item(0);
        setGlobals(globals);
    }

        public static void calcColors(){
        gene_col = new Color[genes.size() + 1];
        double hue = (float) 360 / (genes.size() + 1);
        for (int i = 0; i < genes.size() + 1; i++) {

            //if(array.contains(i)){
            //if(true){
            gene_col[i] = Color.hsb((i - 1) * hue, Settings.saturation, Settings.brightness);
            //}else{
            //     this.gene_col[i] = Color.DARKGREY;
            //}
            //this.gene_col[i] = Color.hsb((i - 1) * hue, Settings.saturation, Settings.brightness);
        }
    }

    static boolean is_draw(int a) {
        a = Math.abs(a);
        if(Settings.gene_meta.containsKey(a)){
            return Settings.gene_meta.get(a).isDraw();
        }else{
            return default_draw;
        }

    }

    static int gene_width(int a) {
        a = Math.abs(a);
        Integer to_return;
         if(Settings.gene_meta.containsKey(a)){
            to_return = Settings.gene_meta.get(a).getLine_width();
            if(to_return == null){
                return Settings.line_size;
            }
            return to_return;
        }else{
            return Settings.line_size ;
        }
    }
    static String gene_name(int a){
       a = Math.abs(a);
       if(Settings.gene_meta.containsKey(a)){
            return Settings.gene_meta.get(a).getName();
        }else{
            return null;
        }
    }
    static GeneMeta createGeneMeta(int a){
         a = Math.abs(a);
         GeneMeta m = new GeneMeta(a);
         m.setDraw(default_draw);
         m.setHighlighted(default_highlighted);
         m.setTransparent(default_transparent);
         return m;
    }
    static void set_gene_name(int a,String s){
       a = Math.abs(a);
       GeneMeta m;
       if(!Settings.gene_meta.containsKey(a)){
            m = createGeneMeta(a);
        }else{
           m = Settings.gene_meta.get(a);
       }
       m.setName(s);
       Settings.gene_meta.put(a, m);
    }
    static void set_gene_col(int a,Color c){
              a = Math.abs(a);
       GeneMeta m;
       if(!Settings.gene_meta.containsKey(a)){
            m = createGeneMeta(a);
        }else{
           m = Settings.gene_meta.get(a);
       }
       m.setGene_paint((Paint)c);
       Settings.gene_meta.put(a, m);
    }
    static void set_gene_width(int a,int c){
              a = Math.abs(a);
       GeneMeta m;
       if(!Settings.gene_meta.containsKey(a)){
            m = createGeneMeta(a);
        }else{
           m = Settings.gene_meta.get(a);
       }
       m.setLine_width(c);
       Settings.gene_meta.put(a, m);
    }
    static void set_gene_draw(int a,boolean value){
       a = Math.abs(a);
       GeneMeta m;
       if(!Settings.gene_meta.containsKey(a)){
            m = createGeneMeta(a);
        }else{
           m = Settings.gene_meta.get(a);
       }
       m.setDraw(value);
       Settings.gene_meta.put(a, m);
    }
        static void set_gene_trans(int a,boolean value){
               a = Math.abs(a);
       GeneMeta m;
       if(!Settings.gene_meta.containsKey(a)){
            m = createGeneMeta(a);
        }else{
           m = Settings.gene_meta.get(a);
       }
       m.setTransparent(value);
       Settings.gene_meta.put(a, m);
    }
        static void set_gene_highlighted(int a,boolean value){
               a = Math.abs(a);
       GeneMeta m;
       if(!Settings.gene_meta.containsKey(a)){
            m = createGeneMeta(a);
        }else{
           m = Settings.gene_meta.get(a);
       }
       m.setHighlighted(value);
       Settings.gene_meta.put(a, m);
    }


    static Paint gene_color(int gene) {
        gene = Math.abs(gene);
        Paint p;
        if(isHighlighted(gene)){
        if(Settings.gene_meta.containsKey(gene)){

            p = Settings.gene_meta.get(gene).getGene_paint();
            if(p == null){
                p = gene_col[genes.indexOf(gene)];
            }
        }else{
           p = gene_col[genes.indexOf(gene)];
        }
        }else{
            p = Settings.nonhighlighted;
        }
        return p;
    }

    static boolean isTransparent(int gene) {
            gene = Math.abs(gene);
            if(Settings.gene_meta.containsKey(gene)){
                return Settings.gene_meta.get(gene).isTransparent();
            }
            return default_transparent;
    }

    public static boolean isHighlighted(int gene) {
        gene = Math.abs(gene);
        if(Settings.gene_meta.containsKey(gene)){
            return Settings.gene_meta.get(gene).isHighlighted();
        }
        return default_highlighted;
    }

    static void setGenesSet(Set<Integer> _genes) {
        genes = new ArrayList<Integer> ();
        Iterator<Integer> iter = _genes.iterator();
        while(iter.hasNext()){
            Settings.genes.add(Math.abs(iter.next()));
        }
    }

    static void clearGeneMeta() {
        gene_meta = new TreeMap<Integer, GeneMeta>();
    }

    static void removeMeta(int value) {
        int gene = Math.abs(value);
        gene_meta.remove(gene);
    }

}
