package diarsid.support.javafx;

import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;


import diarsid.support.objects.references.impl.PresentListenable;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

import static diarsid.support.objects.references.impl.References.listenablePresent;

public class DoubleClickDetector {

    private final static long NOT_CLICKED = -1;

    private final Node node;
    private final PresentListenable<Integer> doubleClickMillisThreshold;
    private final Consumer<MouseEvent> doOnDoubleClick;

    private long timeOfPreviousClick;

    public static class Builder {

        private Node node;
        private int msBetweenClicks;
        private Consumer<MouseEvent> doOnDoubleClick;

        private Builder() {
        }

        public static DoubleClickDetector.Builder createFor(Node node) {
            Builder builder = new Builder();
            builder.node = node;
            return builder;
        }

        public DoubleClickDetector.Builder withMillisBetweenClicks(int msBetweenClicks) {
            this.msBetweenClicks = msBetweenClicks;
            return this;
        }

        public DoubleClickDetector.Builder withDoOnDoubleClick(Consumer<MouseEvent> doOnDoubleClick) {
            this.doOnDoubleClick = doOnDoubleClick;
            return this;
        }

        public DoubleClickDetector build() {
            return new DoubleClickDetector(this);
        }
    }

    DoubleClickDetector(DoubleClickDetector.Builder builder) {
        this.node = builder.node;
        this.doubleClickMillisThreshold = listenablePresent(builder.msBetweenClicks, "");
        this.timeOfPreviousClick = NOT_CLICKED;
        this.doOnDoubleClick = builder.doOnDoubleClick;

        this.node.addEventHandler(MOUSE_CLICKED, this::consumeMouseClickedEvent);
    }

    private void consumeMouseClickedEvent(MouseEvent mouseEvent) {
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }

        long timeOfPreviousClickCopy = this.timeOfPreviousClick;
        long timeOfCurrentClick = System.currentTimeMillis();
        this.timeOfPreviousClick = timeOfCurrentClick;
        long currentDoubleClickMillisThreshold = this.doubleClickMillisThreshold.get().longValue();

        if ( timeOfPreviousClickCopy == NOT_CLICKED ) {
            // do nothing
        }
        else {
            long timeBetweenClicks = timeOfCurrentClick - timeOfPreviousClickCopy;
            if ( timeBetweenClicks <= currentDoubleClickMillisThreshold ) {
                this.doOnDoubleClick.accept(mouseEvent);
            }
            else {
                System.out.println("not a double click");
            }
        }
    }
}
