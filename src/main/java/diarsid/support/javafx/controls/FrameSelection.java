package diarsid.support.javafx.controls;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import static diarsid.support.javafx.controls.FrameSelection.MouseCorner.BOTTOM_LEFT;
import static diarsid.support.javafx.controls.FrameSelection.MouseCorner.BOTTOM_RIGHT;
import static diarsid.support.javafx.controls.FrameSelection.MouseCorner.TOP_LEFT;
import static diarsid.support.javafx.controls.FrameSelection.MouseCorner.TOP_RIGHT;

public class FrameSelection extends Rectangle {

    enum MouseCorner {
        TOP_LEFT,       TOP_RIGHT,

        BOTTOM_LEFT,    BOTTOM_RIGHT
    }

    private double x;
    private double y;
    private double mouseX;
    private double mouseY;
    private MouseCorner mouseCorner;
    private Bounds bounds;

    public FrameSelection() {
        super.setHeight(0);
        super.setWidth(0);
        super.setVisible(false);
        super.toBack();
        super.mouseTransparentProperty().set(true);
        super.getStyleClass().add("frame-selection");
    }

    public void start(MouseEvent mouseEvent, Bounds bounds) {
        this.bounds = bounds;

        this.x = mouseEvent.getSceneX();
        this.y = mouseEvent.getSceneY();

        super.relocate(this.x, this.y);
        super.setHeight(1);
        super.setWidth(1);
        super.setVisible(true);
        super.toFront();
    }

    public void scrolled(double mouseSceneX, double mouseSceneY, double scrollX, double scrollY) {
        if ( scrollX == 0 && scrollY == 0 ) {
            return;
        }

        if ( isNull(this.mouseCorner) ) {
            return;
        }

        if ( scrollX == 0 ) {
            double newHeight;
            double newY;

            switch ( this.mouseCorner ) {
                case TOP_LEFT:
                    break;
                case TOP_RIGHT:
                    break;
                case BOTTOM_LEFT:
                case BOTTOM_RIGHT:
                    double boundsY = this.bounds.getMinY();
                    double maxHeightInBounds = mouseSceneY - boundsY;
                    if ( scrollY < 0 ) {
                        newHeight = super.getHeight() - scrollY;
                        newY = this.y + scrollY;
                    }
                    else if ( scrollY > 0 ) {
                        newHeight = super.getHeight() + scrollY;
                        newY = this.y;
                    }
                    else {
                        throw new IllegalArgumentException();
                    }

                    if ( newHeight > maxHeightInBounds ) {
                        newHeight = maxHeightInBounds;
                    }


                    if ( newY < boundsY ) {
                        newY = boundsY;
                    }
                    this.y = newY;

                    super.relocate(this.x, newY);
                    super.setHeight(newHeight);
                    break;
            }


        }
        else if ( scrollY == 0 ) {

        }

    }

    public void dragged(MouseEvent mouseEvent) {
        if ( isNull(this.bounds) ) {
            return;
        }

        double mouseX = mouseEvent.getSceneX();
        double mouseY = mouseEvent.getSceneY();

        double newWidth;
        double newHeight;
        double newX;
        double newY;
        MouseCorner newCorner;

        if ( mouseX <= this.x && mouseY <= this.y ) {
            newWidth = this.x - mouseX;
            newHeight = this.y - mouseY;
            newX = mouseX;
            newY = mouseY;
            newCorner = TOP_LEFT;
        }
        else if ( mouseX > this.x && mouseY > this.y ) {
            newWidth = mouseX - this.x;
            newHeight = mouseY - this.y;
            newX = this.x;
            newY = this.y;
            newCorner = BOTTOM_RIGHT;
        }
        else if ( mouseX <= this.x && mouseY > this.y ) {
            newWidth = this.x - mouseX;
            newHeight = mouseY - this.y;
            newX = mouseX;
            newY = this.y;
            newCorner = BOTTOM_LEFT;
        }
        else if ( mouseX > this.x && mouseY <= this.y ) {
            newWidth = mouseX - this.x;
            newHeight = this.y - mouseY;
            newX = this.x;
            newY = mouseY;
            newCorner = TOP_RIGHT;
        }
        else {
            System.out.println("[SELECTION] [drag] unexpected");
            throw new IllegalArgumentException();
        }

        if ( newX < this.bounds.getMinX() ) {
            newX = this.bounds.getMinX();
            newWidth = this.x - this.bounds.getMinX();
        }
        if ( newY < this.bounds.getMinY() ) {
            newY = this.bounds.getMinY();
            newHeight = this.y - this.bounds.getMinY();
        }
        if ( newX + newWidth > this.bounds.getMaxX() ) {
            newWidth = this.bounds.getMaxX() - newX;
        }
        if ( newY + newHeight > this.bounds.getMaxY() ) {
            newHeight = this.bounds.getMaxY() - newY;
        }

        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseCorner = newCorner;

        super.relocate(newX, newY);
        super.setWidth(newWidth);
        super.setHeight(newHeight);
    }

    public void stop(MouseEvent mouseEvent) {
        super.setHeight(0);
        super.setWidth(0);
        super.toBack();
        super.setVisible(false);

        this.bounds = null;

        this.x = -1;
        this.y = -1;

        this.mouseX = -1;
        this.mouseY = -1;
        this.mouseCorner = null;
    }

    public boolean isIntersectedWith(Node node) {
        if ( isNull(this.bounds) ) {
            return false;
        }
        Bounds boundsS = super.localToScreen(super.getBoundsInLocal());
        Bounds boundsR = node.localToScreen(node.getBoundsInLocal());

        return boundsS.intersects(boundsR);
    }
}
