package diarsid.support.javafx.mouse;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diarsid.support.concurrency.threads.IncrementThreadsNaming;
import diarsid.support.concurrency.threads.NamedThreadFactory;
import diarsid.support.objects.references.Possible;
import diarsid.support.objects.references.References;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

import static diarsid.support.concurrency.threads.ThreadsUtil.shutdownAndWait;
import static diarsid.support.javafx.mouse.ClickType.DOUBLE_CLICK;
import static diarsid.support.javafx.mouse.ClickType.SEQUENTIAL_CLICK;
import static diarsid.support.javafx.mouse.ClickType.USUAL_CLICK;

public class ClickTypeDetector implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(ClickTypeDetector.class);

    private static final long NOT_CLICKED = -1;

    public static class Builder {

        private String name;
        private Node node;
        private ClickTypeDurations clickTypeDurations;
        private final Possible<BiConsumer<ClickType, MouseEvent>> callbackOnAll;
        private final Map<ClickType, Consumer<MouseEvent>> callbacksByType;

        private Builder() {
            this.callbackOnAll = References.simplePossibleButEmpty();
            this.callbacksByType = new HashMap<>();
        }

        public static ClickTypeDetector.Builder createFor(Node node) {
            Builder builder = new Builder();
            builder.node = node;
            return builder;
        }

        public ClickTypeDetector.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public ClickTypeDetector.Builder with(ClickTypeDurations clickTypeDurations) {
            this.clickTypeDurations = clickTypeDurations;
            return this;
        }

        public ClickTypeDetector.Builder withDoOnAll(BiConsumer<ClickType, MouseEvent> callback) {
            this.callbackOnAll.resetTo(callback);
            return this;
        }

        public ClickTypeDetector.Builder withDoOn(ClickType type, Consumer<MouseEvent> callback) {
            this.callbacksByType.put(type, callback);
            return this;
        }

        public ClickTypeDetector build() {
            if ( isNull(this.clickTypeDurations) ) {
                this.clickTypeDurations = new ClickTypeDurations(
                        DOUBLE_CLICK.msAfterLastClick(),
                        SEQUENTIAL_CLICK.msAfterLastClick());
            }
            return new ClickTypeDetector(this);
        }
    }

    private final Node node;
    private final String name;
    private final ClickTypeDurations clickTypeDurations;
    private final Possible<BiConsumer<ClickType, MouseEvent>> callbackOnAll;
    private final Map<ClickType, Consumer<MouseEvent>> callbacksByType;
    private final ScheduledExecutorService async;
    private Future<?> asyncReactOnClick;

    private long timeOfLastClick;

    ClickTypeDetector(ClickTypeDetector.Builder builder) {
        this.node = builder.node;
        this.name = builder.name != null ? builder.name : randomUUID().toString();
        this.clickTypeDurations = builder.clickTypeDurations;
        this.timeOfLastClick = NOT_CLICKED;
        this.callbackOnAll = builder.callbackOnAll;
        this.callbacksByType = new HashMap<>(builder.callbacksByType);
        this.async = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(new IncrementThreadsNaming(format(
                "%s[%s]", this.getClass().getSimpleName(), this.name))));

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
            this.reactOn(USUAL_CLICK, mouseEvent);
        }
        else {
            long timeBetweenClicks = timeOfCurrentClick - timeOfLastClickCopy;
            ClickType clickType = this.clickTypeDurations.defineClickType(timeBetweenClicks);

            boolean prevReactionCancelled = false;
            if ( nonNull(this.asyncReactOnClick) ) {
                prevReactionCancelled = this.asyncReactOnClick.cancel(true);
            }

            if ( prevReactionCancelled && clickType.is(DOUBLE_CLICK) ) {
                this.reactOn(clickType, mouseEvent);
            }
            else {
                this.scheduleReactionOn(clickType, mouseEvent);
            }
        }
    }

    private void scheduleReactionOn(ClickType clickType, MouseEvent mouseEvent) {
        if ( nonNull(this.asyncReactOnClick) ) {
            this.asyncReactOnClick.cancel(true);
        }

        this.asyncReactOnClick = this.async.schedule(
                () -> this.reactOn(clickType, mouseEvent),
                DOUBLE_CLICK.msAfterLastClick(),
                MILLISECONDS);
    }

    private void reactOn(ClickType clickType, MouseEvent mouseEvent) {
        Consumer<MouseEvent> callback = this.callbacksByType.get(clickType);
        if ( nonNull(callback) ) {
            try {
                callback.accept(mouseEvent);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if ( this.callbackOnAll.isPresent() ) {
            try {
                this.callbackOnAll.get().accept(clickType, mouseEvent);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {
        if ( nonNull(this.asyncReactOnClick) ) {
            this.asyncReactOnClick.cancel(true);
        }
        shutdownAndWait(this.async);
    }
}
