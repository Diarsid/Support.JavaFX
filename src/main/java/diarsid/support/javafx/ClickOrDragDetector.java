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
    private final long pressedDurationTreshold;

    private boolean isPressed;
    private boolean wasDragged;
    private MouseEvent possibleDragStartMousePress;
    private long timePressed;
    private long timeReleased;
    private long dragCounter;

    private ClickOrDragDetector(ClickOrDragDetector.Builder builder) {
        this.node = builder.node;
        this.clickListener = builder.onClickNotDrag;
        this.dragListener = builder.onDragNotClick;
        this.pressedDurationTreshold = builder.pressedDurationThreshold;

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
        this.isPressed = true;
        this.possibleDragStartMousePress = mouseEvent;
//        mouseEvent.setDragDetect(true);
//        mouseEvent.consume();
    }

    private void onDragDetected(MouseEvent mouseEvent) {
        System.out.println("MOUSE_DRAG_DETECTED");

        this.wasDragged = true;
        this.dragCounter++;
        this.dragListener.onDragStart(this.possibleDragStartMousePress);
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        System.out.println("MOUSE_DRAGGED");
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }
        mouseEvent.setDragDetect(true);

        if ( this.wasDragged || this.dragCounter == 0 ) {
            this.dragListener.onDragStart(this.possibleDragStartMousePress);
            this.dragCounter++;
        }

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
        mouseEvent.setDragDetect(false);
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
        this.isPressed = false;
        this.possibleDragStartMousePress = null;
        this.timePressed = 0;
        this.timeReleased = 0;
        this.dragCounter = 0;
    }

    private boolean wasClickedNotDragged() {
        if ( this.wasDragged ) {
            return false;
        }
        if ( this.mousePressedDuration() > this.pressedDurationTreshold ) {
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
