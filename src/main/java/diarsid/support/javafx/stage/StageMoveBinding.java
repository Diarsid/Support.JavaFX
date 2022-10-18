package diarsid.support.javafx.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import diarsid.desktop.ui.geometry.Point;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

public class StageMoveBinding {

    public interface Move extends Point {

        double startX();

        double startY();

        public interface Changeable extends Move {

            void setIgnore(boolean isToIgnore);

            boolean isIgnored();

            void changeX(double x);

            void changeY(double y);

            double finalX();

            double finalY();

            void ignoreX();

            void ignoreY();

            boolean isIgnoredX();

            boolean isIgnoredY();
        }

        public interface Callback {

            void accept(Move stageMove, Move mouseMove);
        }

        public interface Interceptor {

            void intercept(Move.Changeable stageMove, Move mouseMove);


            default void moveChangedByPreviousInterceptor() {
                // no impl
            }
        }
    }

    static final class ChangeableMoveImpl implements Move.Changeable {

        double startX;
        double startY;

        double initialX;
        double initialY;

        double changedX;
        double changedY;

        boolean xChanged;
        boolean yChanged;

        boolean xIgnored;
        boolean yIgnored;

        void startAt(double x, double y) {
            this.startX = x;
            this.startY = y;
            this.xIgnored = false;
            this.yIgnored = false;
        }

        void set(double x, double y) {
            this.initialX = x;
            this.initialY = y;
            this.xChanged = false;
            this.yChanged = false;
            this.xIgnored = false;
            this.yIgnored = false;
        }

        @Override
        public double startX() {
            return this.startX;
        }

        @Override
        public double startY() {
            return this.startY;
        }

        @Override
        public double x() {
            return this.initialX;
        }

        @Override
        public double y() {
            return this.initialY;
        }

        @Override
        public void setIgnore(boolean isToIgnore) {
            this.xIgnored = true;
            this.yIgnored = true;
        }

        @Override
        public boolean isIgnored() {
            return this.xIgnored && this.yIgnored;
        }

        @Override
        public void changeX(double x) {
            this.changedX = x;
            this.xChanged = true;
        }

        @Override
        public void changeY(double y) {
            this.changedY = y;
            this.yChanged = true;
        }

        @Override
        public double finalX() {
            if ( this.xIgnored ) {
                return this.initialX;
            }

            if ( this.xChanged ) {
                return this.changedX;
            }
            else {
                return this.initialX;
            }
        }

        @Override
        public double finalY() {
            if ( this.yIgnored ) {
                return this.initialY;
            }

            if ( this.yChanged ) {
                return this.changedY;
            }
            else {
                return this.initialY;
            }
        }

        @Override
        public void ignoreX() {
            this.xIgnored = true;
        }

        @Override
        public void ignoreY() {
            this.yIgnored = true;
        }

        @Override
        public boolean isIgnoredX() {
            return this.xIgnored;
        }

        @Override
        public boolean isIgnoredY() {
            return this.yIgnored;
        }
    }

    static final class MouseMoveImpl implements Move {

        double startX;
        double startY;
        double x;
        double y;

        @Override
        public double startX() {
            return this.startX;
        }

        @Override
        public double startY() {
            return this.startY;
        }

        @Override
        public double x() {
            return this.x;
        }

        @Override
        public double y() {
            return this.y;
        }
    }

    private final Stage stage;
    private final BooleanProperty isStageMovable;
    private final List<Move.Callback> beforeMoveCallbacks;
    private final List<Move.Callback> afterMoveCallbacks;
    private final List<Move.Interceptor> moveInterceptors;
    private final AtomicReference<EventType<MouseEvent>> lastEvent;
    private final AtomicReference<MouseButton> buttonPressed;
    private final ChangeableMoveImpl stageMove;
    private final MouseMoveImpl mouseMove;
    private final List<Node> boundNodes;
    private double xInitialDelta;
    private double yInitialDelta;
    
    public StageMoveBinding(Stage stage) {
        this.stage = stage;
        this.isStageMovable = new SimpleBooleanProperty(true);
        this.beforeMoveCallbacks = new ArrayList<>();
        this.afterMoveCallbacks = new ArrayList<>();
        this.moveInterceptors = new ArrayList<>();
        this.lastEvent = new AtomicReference<>();
        this.buttonPressed = new AtomicReference<>();
        this.stageMove = new ChangeableMoveImpl();
        this.mouseMove = new MouseMoveImpl();
        this.boundNodes = new ArrayList<>();
    }

    public StageMoveBinding(Stage stage, Move.Interceptor moveInterceptors) {
        this.stage = stage;
        this.isStageMovable = new SimpleBooleanProperty(true);
        this.beforeMoveCallbacks = new ArrayList<>();
        this.afterMoveCallbacks = new ArrayList<>();
        this.moveInterceptors = new ArrayList<>();
        this.moveInterceptors.add(moveInterceptors);
        this.lastEvent = new AtomicReference<>();
        this.buttonPressed = new AtomicReference<>();
        this.stageMove = new ChangeableMoveImpl();
        this.mouseMove = new MouseMoveImpl();
        this.boundNodes = new ArrayList<>();
    }

    Stage stage() {
        return this.stage;
    }

    public BooleanProperty isMovable() {
        return this.isStageMovable;
    }
    
    public void bindTo(Node node) {
        this.boundNodes.add(node);

        node.addEventHandler(MOUSE_PRESSED, this::onMousePressed);
        node.addEventHandler(MOUSE_DRAGGED, this::onMouseDragged);
        node.addEventHandler(MOUSE_RELEASED, this::onMouseReleased);
    }

    public boolean isMoving() {
        return this.lastEvent.get() == MOUSE_DRAGGED;
    }

    public void intercept(Move.Interceptor interceptor) {
        this.moveInterceptors.add(interceptor);
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        if ( ! this.isStageMovable.get() ) {
            return;
        }

        MouseButton button = mouseEvent.getButton();
        this.buttonPressed.set(button);
        if ( button.equals(PRIMARY) ) {
            this.xInitialDelta = this.stage.getX() - mouseEvent.getScreenX();
            this.yInitialDelta = this.stage.getY() - mouseEvent.getScreenY();

            this.stageMove.startAt(
                    mouseEvent.getScreenX() + this.xInitialDelta,
                    mouseEvent.getScreenY() + this.yInitialDelta);

            this.mouseMove.startX = mouseEvent.getScreenX();
            this.mouseMove.startY = mouseEvent.getScreenY();

            this.lastEvent.set(MOUSE_PRESSED);

            mouseEvent.consume();
        }
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if ( ! this.isStageMovable.get() ) {
            return;
        }

        if ( ! this.buttonPressed.get().equals(PRIMARY) ) {
            return;
        }

        this.stageMove.set(
                mouseEvent.getScreenX() + this.xInitialDelta,
                mouseEvent.getScreenY() + this.yInitialDelta);

        this.mouseMove.x = mouseEvent.getScreenX();
        this.mouseMove.y = mouseEvent.getScreenY();

        if ( MOUSE_PRESSED.equals(this.lastEvent.get()) ) {
            this.lastEvent.set(MOUSE_DRAGGED);
            this.beforeMoveCallbacks.forEach(callback -> {
                callback.accept(this.stageMove, this.mouseMove);
            });
        }
        this.lastEvent.set(MOUSE_DRAGGED);

        boolean moveChanged = false;
        for ( Move.Interceptor interceptor : this.moveInterceptors ) {
            if ( moveChanged ) {
                interceptor.moveChangedByPreviousInterceptor();
            }
            interceptor.intercept(this.stageMove, this.mouseMove);
            if ( ! moveChanged && (this.stageMove.yChanged && this.stageMove.xChanged) ) {
                moveChanged = true;
            }
            if ( this.stageMove.isIgnored() ) {
                break;
            }
        }

        if ( ! this.stageMove.isIgnoredX() ) {
            this.stage.setX(this.stageMove.finalX());
        }
        if ( ! this.stageMove.isIgnoredY() ) {
            this.stage.setY(this.stageMove.finalY());
        }

        mouseEvent.consume();
    }

    private void onMouseReleased(MouseEvent mouseEvent) {
        if ( ! this.isStageMovable.get() ) {
            return;
        }

        if ( ! this.buttonPressed.get().equals(PRIMARY) ) {
            return;
        }

        this.stageMove.set(
                this.stage.getX(),
                this.stage.getY());

        this.mouseMove.x = mouseEvent.getScreenX();
        this.mouseMove.y = mouseEvent.getScreenY();

        if ( MOUSE_DRAGGED.equals(this.lastEvent.get()) ) {
            this.afterMoveCallbacks.forEach(callback -> {
                callback.accept(this.stageMove, this.mouseMove);
            });
        }

        mouseEvent.consume();
        this.lastEvent.set(MOUSE_RELEASED);
    }
    
    public void afterMove(Move.Callback callback) {
        this.afterMoveCallbacks.add(callback);
    }

    public void beforeMove(Move.Callback callback) {
        this.beforeMoveCallbacks.add(callback);
    }
}
