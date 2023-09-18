package diarsid.support.javafx.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import diarsid.desktop.ui.geometry.Point;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;

public class StageMoving {

    public static final String ANY_MOVE = "ANY_MOVE";
    public static final String MOVE_BY_MOUSE = "MOVE_BY_MOUSE";

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

            void accept(String behavior, Move stageMove, Move mouseMove);
        }

        public interface Interceptor {

            void intercept(String behavior, Move.Changeable stageMove, Move mouseMove);


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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChangeableMoveImpl)) return false;
            ChangeableMoveImpl that = (ChangeableMoveImpl) o;
            return Double.compare(that.startX, startX) == 0 &&
                    Double.compare(that.startY, startY) == 0 &&
                    Double.compare(that.initialX, initialX) == 0 &&
                    Double.compare(that.initialY, initialY) == 0 &&
                    Double.compare(that.changedX, changedX) == 0 &&
                    Double.compare(that.changedY, changedY) == 0 &&
                    xChanged == that.xChanged &&
                    yChanged == that.yChanged &&
                    xIgnored == that.xIgnored &&
                    yIgnored == that.yIgnored;
        }

        @Override
        public int hashCode() {
            return Objects.hash(startX, startY, initialX, initialY, changedX, changedY, xChanged, yChanged, xIgnored, yIgnored);
        }

        @Override
        public String toString() {
            return "ChangeableMoveImpl{" +
                    "startX=" + startX +
                    ", startY=" + startY +
                    ", initialX=" + initialX +
                    ", initialY=" + initialY +
                    ", changedX=" + changedX +
                    ", changedY=" + changedY +
                    ", xChanged=" + xChanged +
                    ", yChanged=" + yChanged +
                    ", xIgnored=" + xIgnored +
                    ", yIgnored=" + yIgnored +
                    '}';
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MouseMoveImpl)) return false;
            MouseMoveImpl mouseMove = (MouseMoveImpl) o;
            return Double.compare(mouseMove.startX, startX) == 0 &&
                    Double.compare(mouseMove.startY, startY) == 0 &&
                    Double.compare(mouseMove.x, x) == 0 &&
                    Double.compare(mouseMove.y, y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(startX, startY, x, y);
        }

        @Override
        public String toString() {
            return "MouseMoveImpl{" +
                    "startX=" + startX +
                    ", startY=" + startY +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    private final Stage stage;
    private final BooleanProperty isStageMovable;
    private final List<Move.Callback> beforeMoveCallbacks;
    private final List<Move.Callback> afterMoveCallbacks;
    private final Map<String, List<Move.Interceptor>> moveInterceptorsByMove;
    private final AtomicBoolean isMoving;
    private final AtomicReference<EventType<MouseEvent>> lastEvent;
    private final AtomicReference<MouseButton> buttonPressed;
    private final ChangeableMoveImpl stageMove;
    private final MouseMoveImpl mouseMove;
    private final List<Node> boundNodes;
    private double xInitialDelta;
    private double yInitialDelta;
    
    public StageMoving(Stage stage) {
        this.stage = stage;
        this.isStageMovable = new SimpleBooleanProperty(true);
        this.beforeMoveCallbacks = new ArrayList<>();
        this.afterMoveCallbacks = new ArrayList<>();
        this.moveInterceptorsByMove = new HashMap<>();
        this.moveInterceptorsByMove.put(ANY_MOVE, new ArrayList<>());
        this.isMoving = new AtomicBoolean(false);
        this.lastEvent = new AtomicReference<>();
        this.buttonPressed = new AtomicReference<>();
        this.stageMove = new ChangeableMoveImpl();
        this.mouseMove = new MouseMoveImpl();
        this.boundNodes = new ArrayList<>();
    }

    public StageMoving(Stage stage, Move.Interceptor moveInterceptor) {
        this(stage);
        this.moveInterceptorsByMove.get(ANY_MOVE).add(moveInterceptor);
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

    public boolean isMovingNow() {
        return this.isMoving.get();
    }

    public void intercept(Move.Interceptor interceptor) {
        this.moveInterceptorsByMove.get(ANY_MOVE).add(interceptor);
    }

    public void intercept(Move.Interceptor interceptor, String behavior) {
        List<Move.Interceptor> interceptors = this.moveInterceptorsByMove.get(behavior);

        if ( isNull(interceptors) ) {
            interceptors = new ArrayList<>();
            this.moveInterceptorsByMove.put(behavior, interceptors);
        }

        interceptors.add(interceptor);
    }

    public void intercept(Move.Interceptor interceptor, String... behaviors) {
        for ( String behavior : behaviors ) {
            this.intercept(interceptor, behavior);
        }
    }

    private void onMousePressed(MouseEvent mouseEvent) {
        double x = mouseEvent.getScreenX();
        double y = mouseEvent.getScreenY();

        if ( ! this.isStageMovable.get() ) {
            return;
        }

        MouseButton button = mouseEvent.getButton();
        this.buttonPressed.set(button);
        if ( button.equals(PRIMARY) ) {
            this.xInitialDelta = this.stage.getX() - x;
            this.yInitialDelta = this.stage.getY() - y;

            this.stageMove.startAt(
                    x + this.xInitialDelta,
                    y + this.yInitialDelta);

            this.mouseMove.startX = x;
            this.mouseMove.startY = y;

            this.lastEvent.set(MOUSE_PRESSED);

            mouseEvent.consume();
        }
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if ( ! this.isStageMovable.get() ) {
            return;
        }

        if ( this.buttonPressed.get() == null ) {
            this.buttonPressed.set(mouseEvent.getButton());
        }

        if ( ! this.buttonPressed.get().equals(PRIMARY) ) {
            return;
        }

        double x = mouseEvent.getScreenX();
        double y = mouseEvent.getScreenY();

        this.stageMove.set(
                x + this.xInitialDelta,
                y + this.yInitialDelta);

        this.mouseMove.x = x;
        this.mouseMove.y = y;

        if ( MOUSE_PRESSED.equals(this.lastEvent.get()) ) {
            this.lastEvent.set(MOUSE_DRAGGED);
            this.isMoving.set(true);
            this.beforeMoveCallbacks.forEach(callback -> {
                callback.accept(MOVE_BY_MOUSE, this.stageMove, this.mouseMove);
            });
        }
        this.lastEvent.set(MOUSE_DRAGGED);
        this.isMoving.set(true);

        boolean moveChanged = false;
        boolean moveNotIgnored = true;

        List<Move.Interceptor> defaultInterceptors = this.moveInterceptorsByMove.get(ANY_MOVE);

        if ( nonEmpty(defaultInterceptors) ) {
            for ( Move.Interceptor interceptor : defaultInterceptors ) {
                if ( moveChanged ) {
                    interceptor.moveChangedByPreviousInterceptor();
                }
                interceptor.intercept(MOVE_BY_MOUSE, this.stageMove, this.mouseMove);
                if ( ! moveChanged && (this.stageMove.yChanged && this.stageMove.xChanged) ) {
                    moveChanged = true;
                }
                if ( this.stageMove.isIgnored() ) {
                    moveNotIgnored = false;
                    break;
                }
            }
        }

        List<Move.Interceptor> mouseInterceptors = this.moveInterceptorsByMove.getOrDefault(MOVE_BY_MOUSE, emptyList());

        if ( moveNotIgnored && nonEmpty(mouseInterceptors) ) {
            for ( Move.Interceptor interceptor : mouseInterceptors ) {
                if ( moveChanged ) {
                    interceptor.moveChangedByPreviousInterceptor();
                }
                interceptor.intercept(MOVE_BY_MOUSE, this.stageMove, this.mouseMove);
                if ( ! moveChanged && (this.stageMove.yChanged && this.stageMove.xChanged) ) {
                    moveChanged = true;
                }
                if ( this.stageMove.isIgnored() ) {
                    break;
                }
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

        if ( this.buttonPressed.get() == null ) {
            this.buttonPressed.set(mouseEvent.getButton());
        }

        if ( ! this.buttonPressed.get().equals(PRIMARY) ) {
            return;
        }

        double x = mouseEvent.getScreenX();
        double y = mouseEvent.getScreenY();

        this.stageMove.set(
                this.stage.getX(),
                this.stage.getY());

        this.mouseMove.x = x;
        this.mouseMove.y = y;

        if ( MOUSE_DRAGGED.equals(this.lastEvent.get()) ) {
            this.afterMoveCallbacks.forEach(callback -> {
                callback.accept(MOVE_BY_MOUSE, this.stageMove, this.mouseMove);
            });
        }

        mouseEvent.consume();
        this.isMoving.set(false);
        this.lastEvent.set(MOUSE_RELEASED);
    }
    
    public void afterMove(Move.Callback callback) {
        this.afterMoveCallbacks.add(callback);
    }

    public void beforeMove(Move.Callback callback) {
        this.beforeMoveCallbacks.add(callback);
    }

    public void move(double x, double y) {
        this.move(x, y, ANY_MOVE);
    }

    public void move(double x, double y, String behavior) {
        if ( ! this.isStageMovable.get() ) {
            return;
        }

        this.isMoving.set(true);
        double startX = this.stage.getX();
        double startY = this.stage.getY();

        this.stageMove.startAt(startX, startY);

        this.mouseMove.startX = startX;
        this.mouseMove.startY = startY;

        this.stageMove.set(x, y);

        this.mouseMove.x = x;
        this.mouseMove.y = y;

        this.beforeMoveCallbacks.forEach(callback -> {
            callback.accept(behavior, this.stageMove, this.mouseMove);
        });

        boolean moveChanged = false;
        boolean moveNotIgnored = true;

        List<Move.Interceptor> defaultInterceptors = this.moveInterceptorsByMove.get(ANY_MOVE);

        if ( nonEmpty(defaultInterceptors) ) {
            for ( Move.Interceptor interceptor : defaultInterceptors ) {
                if ( moveChanged ) {
                    interceptor.moveChangedByPreviousInterceptor();
                }
                interceptor.intercept(behavior, this.stageMove, this.mouseMove);
                if ( ! moveChanged && (this.stageMove.yChanged || this.stageMove.xChanged) ) {
                    moveChanged = true;
                }
                if ( this.stageMove.isIgnored() ) {
                    moveNotIgnored = false;
                    break;
                }
            }
        }

        if ( moveNotIgnored && ! behavior.equals(ANY_MOVE) ) {
            List<Move.Interceptor> behaviorInterceptors = this.moveInterceptorsByMove.getOrDefault(behavior, emptyList());

            if ( nonEmpty(behaviorInterceptors) ) {
                for ( Move.Interceptor interceptor : behaviorInterceptors ) {
                    if ( moveChanged ) {
                        interceptor.moveChangedByPreviousInterceptor();
                    }
                    interceptor.intercept(behavior, this.stageMove, this.mouseMove);
                    if ( ! moveChanged && (this.stageMove.yChanged || this.stageMove.xChanged) ) {
                        moveChanged = true;
                    }
                    if ( this.stageMove.isIgnored() ) {
                        break;
                    }
                }
            }
        }

        if ( ! this.stageMove.isIgnoredX() ) {
            this.stage.setX(this.stageMove.finalX());
        }
        if ( ! this.stageMove.isIgnoredY() ) {
            this.stage.setY(this.stageMove.finalY());
        }

        this.stageMove.set(
                this.stage.getX(),
                this.stage.getY());

        this.isMoving.set(false);

        this.afterMoveCallbacks.forEach(callback -> {
            callback.accept(behavior, this.stageMove, this.mouseMove);
        });
    }

//    public void move(double x, double y, String... moves) {
//        this.move(x, y, asList(moves));
//    }

//    public void move(double x, double y, List<String> moves) {
//        if ( ! this.isStageMovable.get() ) {
//            return;
//        }
//
//        if ( moves.isEmpty() ){
//            this.move(x, y);
//        }
//
//        this.isMoving.set(true);
//        double startX = this.stage.getX();
//        double startY = this.stage.getY();
//
//        this.stageMove.startAt(startX, startY);
//
//        this.mouseMove.startX = startX;
//        this.mouseMove.startY = startY;
//
//        this.stageMove.set(x, y);
//
//        this.mouseMove.x = x;
//        this.mouseMove.y = y;
//
//        this.beforeMoveCallbacks.forEach(callback -> {
//            callback.accept(this.stageMove, this.mouseMove);
//        });
//
//        boolean moveChanged = false;
//        boolean moveNotIgnored = true;
//
//        List<Move.Interceptor> defaultInterceptors = this.moveInterceptorsByMove.get(ALL_MOVES);
//
//        if ( nonEmpty(defaultInterceptors) ) {
//            for ( Move.Interceptor interceptor : defaultInterceptors ) {
//                if ( moveChanged ) {
//                    interceptor.moveChangedByPreviousInterceptor();
//                }
//                interceptor.intercept(this.stageMove, this.mouseMove);
//                if ( ! moveChanged && (this.stageMove.yChanged || this.stageMove.xChanged) ) {
//                    moveChanged = true;
//                }
//                if ( this.stageMove.isIgnored() ) {
//                    moveNotIgnored = false;
//                    break;
//                }
//            }
//        }
//
//        movesIterating: for ( String move : moves ) {
//            if ( moveNotIgnored && ! move.equals(ALL_MOVES) ) {
//                List<Move.Interceptor> behaviorInterceptors = this.moveInterceptorsByMove.getOrDefault(move, emptyList());
//
//                if ( nonEmpty(behaviorInterceptors) ) {
//                    for ( Move.Interceptor interceptor : behaviorInterceptors ) {
//                        if ( moveChanged ) {
//                            interceptor.moveChangedByPreviousInterceptor();
//                        }
//                        interceptor.intercept(this.stageMove, this.mouseMove);
//                        if ( ! moveChanged && (this.stageMove.yChanged || this.stageMove.xChanged) ) {
//                            moveChanged = true;
//                        }
//                        if ( this.stageMove.isIgnored() ) {
//                            break movesIterating;
//                        }
//                    }
//                }
//            }
//        }
//
//        if ( ! this.stageMove.isIgnoredX() ) {
//            this.stage.setX(this.stageMove.finalX());
//        }
//        if ( ! this.stageMove.isIgnoredY() ) {
//            this.stage.setY(this.stageMove.finalY());
//        }
//
//        this.stageMove.set(
//                this.stage.getX(),
//                this.stage.getY());
//
//        this.isMoving.set(false);
//
//        this.afterMoveCallbacks.forEach(callback -> {
//            callback.accept(this.stageMove, this.mouseMove);
//        });
//    }
}
