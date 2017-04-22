
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;

import javafx.scene.canvas.GraphicsContext;
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
class FXDrawFactory extends DrawFactory {

    private GraphicsContext gc;

    public FXDrawFactory(GraphicsContext _gc) {
        this.gc = _gc;
    }

    @Override
    public void setLineColor(Color paint) {
        gc.setStroke(paint);
    }

    @Override
    public void drawGeneLine(double x1, double y1, double x2, double y2, int gene) {
        if(!Settings.isTransparent(gene)){
        this.setLineColor((Color)
                Settings.gene_color(gene));
        this.setLineWidth(Settings.gene_width(gene));
        if (gene < 0) {
            gc.setLineDashes(5);
        } else {
            gc.setLineDashes(null);
        }
        gc.strokeLine(x1, y1, x2, y2);
        }

    }

    @Override public void drawRectangle(double x1, double y1, double x2, double y2, boolean isCircular) {
        this.setLineColor(BLACK);
        this.setLineWidth(Settings.line_size);
        gc.setLineDashes(null);
        if(!isCircular){
            gc.strokeRect(x1,y1,x2-x1,y2-y1);
        } else {
            gc.strokeRoundRect(x1,y1,x2-x1,y2-y1, (x2-x1)/2, (y2-y1)/2);
        }
    }

    @Override
    public void setLineWidth(double width) {
        gc.setLineWidth(width);
    }

    @Override
    public void clear() {
        gc.setFill(WHITE);
        gc.fillRect(0, 0, Settings.width, Settings.height);
    }

}