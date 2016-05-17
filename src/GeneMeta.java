
import javafx.scene.paint.Paint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
class GeneMeta {

    private boolean draw;
    private boolean transparent;
    private boolean highlighted;
    private Integer gene_num;
    private String name;
    private Integer line_width;
    private Paint gene_paint;

    public Integer getGene_num() {
        return gene_num;
    }

    public GeneMeta(Node node) {
        this.highlighted = true;
        this.transparent = false;
        this.draw = true;
        this.name = null;
        this.line_width = null;
        this.gene_paint = null;
        this.gene_num = -1;
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node processing = nl.item(i);
            switch (processing.getNodeName()) {
                case "Draw":
                    this.draw = Boolean.parseBoolean(processing.getTextContent());
                    break;
                case "Transparent":
                    this.transparent = Boolean.parseBoolean(processing.getTextContent());
                    break;
                case "Highlighted":
                    this.highlighted = Boolean.parseBoolean(processing.getTextContent());
                    break;
                case "GeneNumber":
                    if (!processing.getTextContent().equals("null")) {
                        this.gene_num = Integer.parseInt(processing.getTextContent());
                    }
                    break;
                case "Name":
                    this.name = processing.getTextContent();
                    break;
                case "LineWidth":
                    if (!processing.getTextContent().equals("null")) {
                        this.line_width = Integer.parseInt(processing.getTextContent());
                    }
                    break;
                case "GenePaint":
                    if (!processing.getTextContent().equals("null")) {
                        this.gene_paint = Paint.valueOf(processing.getTextContent());// processing.getTextContent();
                    }
                    break;
            }
        }
    }

    public GeneMeta(Integer _num) {
        this.highlighted = true;
        this.transparent = false;
        this.draw = true;
        this.name = null;
        this.line_width = null;
        this.gene_paint = null;
        this.gene_num = _num;
    }

    public Element getXML(Document dom) {
        Element gene = dom.createElement("Gene");

        Element drawElement = dom.createElement("Draw");
        Text draw_text = dom.createTextNode("" + this.draw);
        drawElement.appendChild(draw_text);
        gene.appendChild(drawElement);

        Element transparentElement = dom.createElement("Transparent");
        Text transparent_text = dom.createTextNode("" + this.transparent);
        transparentElement.appendChild(transparent_text);
        gene.appendChild(transparentElement);

        Element highlitedElement = dom.createElement("Highlighted");
        Text highlited_text = dom.createTextNode("" + this.highlighted);
        highlitedElement.appendChild(highlited_text);
        gene.appendChild(highlitedElement);

        Element gene_numElement = dom.createElement("GeneNumber");
        Text gene_num_text = dom.createTextNode("" + this.gene_num);
        gene_numElement.appendChild(gene_num_text);
        gene.appendChild(gene_numElement);

        Element nameElement = dom.createElement("Name");
        Text name_text = dom.createTextNode("" + this.name);
        nameElement.appendChild(name_text);
        gene.appendChild(nameElement);

        Element line_widthElement = dom.createElement("LineWidth");
        Text line_width_text = dom.createTextNode("" + this.line_width);
        line_widthElement.appendChild(line_width_text);
        gene.appendChild(line_widthElement);

        Element gene_paintElement = dom.createElement("GenePaint");
        Text gene_paint_text = dom.createTextNode("" + this.gene_paint);
        gene_paintElement.appendChild(gene_paint_text);
        gene.appendChild(gene_paintElement);

        return gene;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLine_width() {
        return line_width;
    }

    public void setLine_width(Integer line_width) {
        this.line_width = line_width;
    }

    public Paint getGene_paint() {
        return gene_paint;
    }

    public void setGene_paint(Paint gene_paint) {
        this.gene_paint = gene_paint;
    }

}
