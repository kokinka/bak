import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class SceneGestures {

    private static class DragContext {

        double mouseAnchorX;
        double mouseAnchorY;

        double translateAnchorX;
        double translateAnchorY;
    }

        private static final double MAX_SCALE = 10.0d;
        private static final double MIN_SCALE = .1d;

        private DragContext sceneDragContext = new DragContext();

        ZoomablePane pane;

        public SceneGestures( ZoomablePane pane) {
            this.pane = pane;
        }

        public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
            return onMousePressedEventHandler;
        }

        public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
            return onMouseDraggedEventHandler;
        }

        public EventHandler<ScrollEvent> getOnScrollEventHandler() {
            return onScrollEventHandler;
        }

        private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {

                if( !event.isPrimaryButtonDown())
                    return;

                sceneDragContext.mouseAnchorX = event.getSceneX();
                sceneDragContext.mouseAnchorY = event.getSceneY();

                sceneDragContext.translateAnchorX = pane.getTranslateX();
                sceneDragContext.translateAnchorY = pane.getTranslateY();

            }

        };

        private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {

                if( !event.isPrimaryButtonDown())
                    return;

                pane.setTranslateX(sceneDragContext.translateAnchorX + event.getSceneX() - sceneDragContext.mouseAnchorX);
                pane.setTranslateY(sceneDragContext.translateAnchorY + event.getSceneY() - sceneDragContext.mouseAnchorY);

                event.consume();
            }
        };

    private EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {

            double delta = 1.1;

            double scale = pane.getScale(); // currently we only use Y, same value is used for X
            double oldScale = scale;

            if (event.getDeltaY() < 0)
                scale /= delta;
            else
                scale *= delta;

            scale = clamp( scale, MIN_SCALE, MAX_SCALE);

            double f = (scale / oldScale)-1;

            double dx = (event.getSceneX() - (pane.getBoundsInParent().getWidth()/2 + pane.getBoundsInParent().getMinX()));
            double dy = (event.getSceneY() - (pane.getBoundsInParent().getHeight()/2 + pane.getBoundsInParent().getMinY()));

            pane.setScale( scale);

            // note: pivot value must be untransformed, i. e. without scaling
            pane.setPivot(f*dx, f*dy);

            event.consume();

        }

    };


    public static double clamp( double value, double min, double max) {

        if( Double.compare(value, min) < 0)
            return min;

        if( Double.compare(value, max) > 0)
            return max;

        return value;
    }


}
