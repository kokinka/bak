import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import static javafx.scene.paint.Color.BLACK;


public class FXSelectableDrawFactory extends DrawFactory {
    //private Pane pane;
    private Group group;
    private Stage geneSettings;
    private ComboBox pickgene;
    private Color paint = BLACK;
    private double width = 1;

    public FXSelectableDrawFactory(Group group, Stage geneSettings, ComboBox pickgene) {
        //this.pane = pane;
        this.group = group;
        this.geneSettings = geneSettings;
        this.pickgene = pickgene;
    }

    @Override
    public void setLineColor(Color paint) {
        this.paint = paint;
    }

    @Override
    public void drawGeneLine(double x1, double y1, double x2, double y2, int gene) {
        if (!Settings.isTransparent(gene)) {
            Line line = new Line(x1, y1, x2, y2);
            this.setLineColor((Color) Settings.gene_color(gene));
            this.setLineWidth(Settings.gene_width(gene));
            line.setStroke(paint);
            line.setStrokeWidth(width);
            if (gene < 0) {
                line.setStrokeDashOffset(5);
            } else {
                line.setStrokeDashOffset(0);
            }

            line.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (!event.isSecondaryButtonDown())
                        return;

                    pickgene.setValue(gene);
                    geneSettings.showAndWait();

                    event.consume();

                }
            });
            group.getChildren().add(line);

        }


    }

    @Override
    public void drawRectangle(double x1, double y1, double x2, double y2, boolean isCircular) {
        this.setLineColor(BLACK);
        this.setLineWidth(Settings.line_size);
        Rectangle rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        rect.setStrokeDashOffset(0);
        rect.setFill(null);
        rect.setStroke(paint);
        rect.setStrokeWidth(width);
        if (isCircular) {
            rect.setArcHeight((y2 - y1) / 2);
            rect.setArcWidth((x2 - x1) / 2);
        }
        group.getChildren().add(rect);

    }

    @Override
    public void setLineWidth(double width) {
        this.width = width;

    }

    @Override
    public void clear() {
        if (group.getChildren().size() > 1)
            group.getChildren().remove(1, group.getChildren().size());
    }
}
