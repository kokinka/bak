import javafx.scene.paint.Color;

public class NoDrawFactory extends  DrawFactory {
    @Override
    public void setLineColor(Color paint) {}

    @Override
    public void drawGeneLine(double x1, double y1, double x2, double y2, int gene) {}

    @Override
    public void drawRectangle(double x1, double y1, double x2, double y2, boolean isCircular) {}

    @Override
    public void setLineWidth(double width) {}

    @Override
    public void clear() {}
}
