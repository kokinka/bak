
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
class SVGDrawFactory extends DrawFactory {

    private SVGGraphics2D g2d;
    private float width;

    public SVGDrawFactory() {
        DOMImplementation domImpl
                = GenericDOMImplementation.getDOMImplementation();
        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        // Create an instance of the SVG Generator.
        g2d = new SVGGraphics2D(document);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fill(new Rectangle(0, 0, Settings.width, Settings.height));
        this.width = Settings.line_size;

    }

    @Override
    public void setLineColor(Color paint) {
        g2d.setColor(new java.awt.Color((float) paint.getRed(), (float) paint.getGreen(), (float) paint.getBlue()));
    }

    @Override
    public void drawGeneLine(double x1, double y1, double x2, double y2, int gene) {
         if(!Settings.isTransparent(gene)){
             this.setLineColor((Color) Settings.gene_color(gene));
             this.setLineWidth(Settings.gene_width(gene));
        if (gene < 0) {
            Stroke dashed = new BasicStroke((float) this.width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            g2d.setStroke(dashed);
        } else {
            Stroke normal;
            normal = new BasicStroke((float) this.width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
            g2d.setStroke(normal);
        }
        int _x1 = (int) x1;
        int _y1 = (int) y1;
        int _x2 = (int) x2;
        int _y2 = (int) y2;
        g2d.drawLine(_x1, _y1, _x2, _y2);
         }
    }

    @Override
    public void setLineWidth(double width) {
        this.width = (float) width;

    }
    
    public void export() {
        export(new File(Settings.title+".svg"));
    }
    public void export(File f){
        OutputStream os = null;
        try {
            os = new FileOutputStream(f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SVGDrawFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        Writer w = new OutputStreamWriter(os);
        try {
            g2d.stream(w);
        } catch (SVGGraphics2DIOException ex) {
            Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void clear() {
        DOMImplementation domImpl
                = GenericDOMImplementation.getDOMImplementation();
        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        // Create an instance of the SVG Generator.
        g2d = new SVGGraphics2D(document);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fill(new Rectangle(0, 0, Settings.width, Settings.height));
        this.width = Settings.line_size;
    }

}