package diarsid.support.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
    
    public interface MoveCallback {

        void accept(Move stageMove, Move mouseMove);
        
    }

    public interface Move {

        double startX();

        double startY();

        double x();

        double y();

        public interface Changeable extends Move {

            void setIgnore(boolean isToIgnore);

            boolean isIgnored();

            void setX(double x);

            void setY(double y);

            double finalX();

            double finalY();
        }
    }

    public interface MoveInterceptor {

        void intercept(Move.Changeable stageMove, Move mouseMove);
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
        boolean ignored;

        void startAt(double x, double y) {
            this.startX = x;
            this.startY = y;
            this.ignored = false;
        }

        void set(double x, double y) {
            this.initialX = x;
            this.initialY = y;
            this.xChanged = false;
            this.yChanged = false;
            this.ignored = false;
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
            this.ignored = isToIgnore;
        }

        @Override
        public boolean isIgnored() {
            return this.ignored;
        }

        @Override
        public void setX(double x) {
            this.changedX = x;
            this.xChanged = true;
        }

        @Override
        public void setY(double y) {
            this.changedY = y;
            this.yChanged = true;
        }

        @Override
        public double finalX() {
            if ( this.xChanged ) {
                return this.changedX;
            }
            else {
                return this.initialX;
            }
        }

        @Override
        public double finalY() {
            if ( this.yChanged ) {
                return this.changedY;
            }
            else {
                return this.initialY;
            }
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
    private final List<MoveCallback> beforeMoveCallbacks;
    private final List<MoveCallback> afterMoveCallbacks;
    private final AtomicReference<EventType<MouseEvent>> lastEvent;
    private final AtomicReference<MouseButton> buttonPressed;
    private final ChangeableMoveImpl stageMove;
    private final MouseMoveImpl mouseMove;
    private final MoveInterceptor moveInterceptor;
    private double xInitialDelta;
    private double yInitialDelta;
    
    public StageMoveBinding(Stage stage) {
        this.stage = stage;
        this.isStageMovable = new SimpleBooleanProperty(true);
        this.beforeMoveCallbacks = new ArrayList<>();
        this.afterMoveCallbacks = new ArrayList<>();
        this.lastEvent = new AtomicReference<>();
        this.buttonPressed = new AtomicReference<>();
        this.stageMove = new ChangeableMoveImpl();
        this.mouseMove = new MouseMoveImpl();
        this.moveInterceptor = null;
    }

    public StageMoveBinding(Stage stage, MoveInterceptor moveInterceptor) {
        this.stage = stage;
        this.isStageMovable = new SimpleBooleanProperty(true);
        this.beforeMoveCallbacks = new ArrayList<>();
        this.afterMoveCallbacks = new ArrayList<>();
        this.lastEvent = new AtomicReference<>();
        this.buttonPressed = new AtomicReference<>();
        this.stageMove = new ChangeableMoveImpl();
        this.mouseMove = new MouseMoveImpl();
        this.moveInterceptor = moveInterceptor;
    }

    public BooleanProperty isMovable() {
        return this.isStageMovable;
    }
    
    public void bindTo(Node node) {

        node.addEventHandler(MOUSE_PRESSED, (mouseEvent) -> {
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
        });
        
        node.addEventHandler(MOUSE_DRAGGED, (mouseEvent) -> {
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
                this.beforeMoveCallbacks.forEach(callback -> {
                    callback.accept(this.stageMove, this.mouseMove);
                });
            }

            if ( this.moveInterceptor != null ) {
                this.moveInterceptor.intercept(this.stageMove, this.mouseMove);
            }

            if ( ! this.stageMove.ignored ) {
                this.stage.setX(this.stageMove.finalX());
                this.stage.setY(this.stageMove.finalY());
            }

            mouseEvent.consume();
            this.lastEvent.set(MOUSE_DRAGGED);
        });
        
        node.addEventHandler(MOUSE_RELEASED, (mouseEvent) -> {
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
        });
    }
    
    public void afterMove(MoveCallback callback) {
        this.afterMoveCallbacks.add(callback);
    }

    public void beforeMove(MoveCallback callback) {
        this.beforeMoveCallbacks.add(callback);
    }
}
