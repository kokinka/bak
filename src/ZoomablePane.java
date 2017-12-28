import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.StackPane;

public class ZoomablePane extends StackPane {
    DoubleProperty scale = new SimpleDoubleProperty(1.0);
    public ZoomablePane() {
        scaleXProperty().bind(scale);
        scaleYProperty().bind(scale);
    }

    public double getScale() {
        return scale.get();
    }

    public void setScale( double scale) {
        this.scale.set(scale);
    }

    public void setPivot( double x, double y) {
        setTranslateX(getTranslateX()-x);
        setTranslateY(getTranslateY()-y);
    }
}
