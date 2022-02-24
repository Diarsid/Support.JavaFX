package diarsid.support.javafx;

import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import static java.lang.System.currentTimeMillis;

import static javafx.scene.input.MouseEvent.DRAG_DETECTED;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

public class ClickOrDragDetector {

    private final Node node;
    private final Consumer<MouseEvent> clickListener;
    private final DragListener dragListener;
    private final long pressedDurationThreshold;

    private boolean wasDragged;
    private MouseEvent possibleDragStartMousePress;
    private long timePressed;
    private long timeReleased;

    private ClickOrDragDetector(ClickOrDragDetector.Builder builder) {
        this.node = builder.node;
        this.clickListener = builder.onClickNotDrag;
        this.dragListener = builder.onDragNotClick;
        this.pressedDurationThreshold = builder.pressedDurationThreshold;

        this.node.addEventFilter(MOUSE_PRESSED, this::onMousePressedAtFilter);

        this.node.addEventHandler(MOUSE_PRESSED, this::onMousePressed);
        this.node.addEventHandler(DRAG_DETECTED, this::onDragDetected);
        this.node.addEventHandler(MOUSE_DRAGGED, this::onMouseDragged);
        this.node.addEventHandler(MOUSE_RELEASED, this::onMouseReleased);

        this.node.setOnDragOver(dragEvent -> System.out.println("DRAGGING CONTENT"));
    }

    public interface DragListener {

        void onPreClicked(MouseEvent mouseEvent);

        void onDragStart(MouseEvent mouseEvent);

        void onDragging(MouseEvent mouseEvent);

        void onDragStopped(MouseEvent mouseEvent);
    }

    public static class Builder {

        private Node node;
        private Consumer<MouseEvent> onClickNotDrag;
        private DragListener onDragNotClick;
        private long pressedDurationThreshold;

        private Builder() {
        }

        public static ClickOrDragDetector.Builder createFor(Node node) {
            ClickOrDragDetector.Builder builder = new Builder();
            builder.node = node;
            return builder;
        }

        public ClickOrDragDetector.Builder withPressedDurationThreshold(long durationTreshold) {
            this.pressedDurationThreshold = durationTreshold;
            return this;
        }

        public ClickOrDragDetector.Builder withOnClickNotDrag(Consumer<MouseEvent> onClickNotDrag) {
            this.onClickNotDrag = onClickNotDrag;
            return this;
        }

        public ClickOrDragDetector.Builder withOnDragNotClick(DragListener dragListener) {
            this.onDragNotClick = dragListener;
            return this;
        }

        public ClickOrDragDetector build() {
            return new ClickOrDragDetector(this);
        }
    }

    private void onMousePressedAtFilter(MouseEvent mouseEvent) {
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }

        this.dragListener.onPreClicked(mouseEvent);
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }

        this.timePressed = currentTimeMillis();
        this.possibleDragStartMousePress = mouseEvent;
    }

    private void onDragDetected(MouseEvent mouseEvent) {
        this.wasDragged = true;
        this.dragListener.onDragStart(this.possibleDragStartMousePress);
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }
        this.wasDragged = true;
//        mouseEvent.setDragDetect(true);

        this.dragListener.onDragging(mouseEvent);
    }

    private void onMouseReleased(MouseEvent mouseEvent) {
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }

        this.timeReleased = currentTimeMillis();
        this.testOnClickConditionsAndPropagate(mouseEvent);
        this.clear();
        mouseEvent.consume();
    }

    private void testOnClickConditionsAndPropagate(MouseEvent mouseEvent) {
//        mouseEvent.setDragDetect(false);
        if ( this.wasClickedNotDragged() ) {
            this.clickListener.accept(mouseEvent);
        }
        else if ( this.wasDraggedNotClicked() ) {
            this.dragListener.onDragStopped(mouseEvent);
        }
        mouseEvent.consume();
    }

    private void clear() {
        this.wasDragged = false;
        this.possibleDragStartMousePress = null;
        this.timePressed = 0;
        this.timeReleased = 0;
    }

    private boolean wasClickedNotDragged() {
        if ( this.wasDragged ) {
            return false;
        }
        if ( this.mousePressedDuration() > this.pressedDurationThreshold) {
            return false;
        }
        return true;
    }

    private boolean wasDraggedNotClicked() {
        return this.wasDragged;
    }

    private long mousePressedDuration() {
        return this.timeReleased - this.timePressed;
    }
}
