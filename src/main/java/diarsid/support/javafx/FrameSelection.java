package diarsid.support.javafx;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import static java.util.Objects.isNull;

public class FrameSelection {

    private final Rectangle selection;

    private double x;
    private double y;
    private Bounds bounds;

    public FrameSelection() {
        this.selection = new Rectangle();
        this.selection.setHeight(0);
        this.selection.setWidth(0);
        this.selection.setVisible(false);
        this.selection.toBack();
        this.selection.mouseTransparentProperty().set(true);
        this.selection.getStyleClass().add("frame-selection");
    }

    public void start(MouseEvent mouseEvent, Bounds bounds) {
        this.bounds = bounds;

        this.x = mouseEvent.getSceneX();
        this.y = mouseEvent.getSceneY();

        this.selection.relocate(this.x, this.y);
        this.selection.setHeight(1);
        this.selection.setWidth(1);
        this.selection.setVisible(true);
        this.selection.toFront();
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

        if ( mouseX <= this.x && mouseY <= this.y ) {
            newWidth = this.x - mouseX;
            newHeight = this.y - mouseY;
            newX = mouseX;
            newY = mouseY;
        }
        else if ( mouseX > this.x && mouseY > this.y ) {
            newWidth = mouseX - this.x;
            newHeight = mouseY - this.y;
            newX = this.x;
            newY = this.y;
        }
        else if ( mouseX <= this.x && mouseY > this.y ) {
            newWidth = this.x - mouseX;
            newHeight = mouseY - this.y;
            newX = mouseX;
            newY = this.y;
        }
        else if ( mouseX > this.x && mouseY <= this.y ) {
            newWidth = mouseX - this.x;
            newHeight = this.y - mouseY;
            newX = this.x;
            newY = mouseY;
        }
        else {
            throw new IllegalStateException();
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

        this.selection.relocate(newX, newY);
        this.selection.setWidth(newWidth);
        this.selection.setHeight(newHeight);
    }

    public void stop(MouseEvent mouseEvent) {
        this.selection.setHeight(0);
        this.selection.setWidth(0);
        this.selection.toBack();
        this.selection.setVisible(false);

        this.bounds = null;

        this.x = -1;
        this.y = -1;
    }

    public boolean isIntersectedWith(Node node) {
        if ( isNull(this.bounds) ) {
            return false;
        }
        Bounds boundsS = this.selection.localToScreen(this.selection.getBoundsInLocal());
        Bounds boundsR = node.localToScreen(node.getBoundsInLocal());

        return boundsS.intersects(boundsR);
    }

    public Rectangle rectangle() {
        return this.selection;
    }
}
