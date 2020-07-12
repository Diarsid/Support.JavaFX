package diarsid.support.javafx;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

import static diarsid.support.javafx.ClickType.DOUBLE_CLICK;
import static diarsid.support.javafx.ClickType.USUAL_CLICK;

public class ClickTypeDetector {

    private final static long NOT_CLICKED = -1;

    private final Node node;
    private final Map<ClickType, Consumer<MouseEvent>> callbacksByType;
    private final TreeMap<Integer, ClickType> clickTypesByMillisAfterLastClick;

    private long timeOfLastClick;

    public static class Builder {

        private Node node;
        private final TreeMap<Integer, ClickType> clickTypesByMillisAfterLastClick;
        private final Map<ClickType, Consumer<MouseEvent>> callbacksByType;

        private Builder() {
            this.clickTypesByMillisAfterLastClick = new TreeMap<>();
            this.callbacksByType = new HashMap<>();
        }

        public static ClickTypeDetector.Builder createFor(Node node) {
            Builder builder = new Builder();
            builder.node = node;
            return builder;
        }

        public ClickTypeDetector.Builder withMillisAfterLastClickForType(ClickType type, int msAfterLastClick) {
            this.clickTypesByMillisAfterLastClick.put(msAfterLastClick, type);
            return this;
        }

        public ClickTypeDetector.Builder withDoOn(ClickType type, Consumer<MouseEvent> callback) {
            this.callbacksByType.put(type, callback);
            return this;
        }

        public ClickTypeDetector build() {
            this.clickTypesByMillisAfterLastClick.put(0, DOUBLE_CLICK);
            stream(ClickType.values()).forEach(this::addDefaultIfAbsent);
            return new ClickTypeDetector(this);
        }

        private void addDefaultIfAbsent(ClickType type) {
            if ( this.clickTypesByMillisAfterLastClick.containsValue(type) ) {
                return;
            }

            this.clickTypesByMillisAfterLastClick.put(type.msAfterLastClick(), type);
        }
    }

    ClickTypeDetector(ClickTypeDetector.Builder builder) {
        this.node = builder.node;
        this.timeOfLastClick = NOT_CLICKED;
        this.clickTypesByMillisAfterLastClick = new TreeMap<>(builder.clickTypesByMillisAfterLastClick);
        this.callbacksByType = new HashMap<>(builder.callbacksByType);

        this.node.addEventHandler(MOUSE_CLICKED, this::consumeMouseClickedEvent);
    }

    private void consumeMouseClickedEvent(MouseEvent mouseEvent) {
        if ( mouseEvent.isSecondaryButtonDown() ) {
            return;
        }

        long timeOfLastClickCopy = this.timeOfLastClick;
        long timeOfCurrentClick = System.currentTimeMillis();
        this.timeOfLastClick = timeOfCurrentClick;

        if ( timeOfLastClickCopy == NOT_CLICKED ) {

            System.out.println("[CLICK TYPE] type: " + USUAL_CLICK + " time: " + 0);
            this.reactOn(USUAL_CLICK, mouseEvent);
        }
        else {
            long timeBetweenClicks = timeOfCurrentClick - timeOfLastClickCopy;

            ClickType clickType = this.clickTypesByMillisAfterLastClick
                    .floorEntry((int) timeBetweenClicks)
                    .getValue();

            System.out.println("[CLICK TYPE] type: " + clickType + " time: " + timeBetweenClicks);

            this.reactOn(clickType, mouseEvent);
        }
    }

    private void reactOn(ClickType clickType, MouseEvent mouseEvent) {
        Consumer<MouseEvent> callback = this.callbacksByType.get(clickType);
        if ( nonNull(callback) ) {
            callback.accept(mouseEvent);
        }
        else {
            System.out.println("no action for " + clickType);
        }
    }
}
