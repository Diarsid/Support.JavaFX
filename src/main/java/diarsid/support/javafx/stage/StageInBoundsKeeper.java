package diarsid.support.javafx.stage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import diarsid.desktop.ui.geometry.Rectangle;
import diarsid.support.javafx.geometry.Screen;
import diarsid.support.objects.references.Possible;

import static diarsid.desktop.ui.geometry.Rectangle.Side.BOTTOM;
import static diarsid.desktop.ui.geometry.Rectangle.Side.LEFT;
import static diarsid.desktop.ui.geometry.Rectangle.Side.RIGHT;
import static diarsid.desktop.ui.geometry.Rectangle.Side.TOP;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;

public class StageInBoundsKeeper {

    private final Rectangle bounds;
    private final Stage stage;
    private final Insets zeroInsets = Insets.EMPTY;
    private final Possible<Insets> insets;

    public StageInBoundsKeeper(StageMoveBinding stageMoveBinding, Rectangle bounds) {
        this.bounds = bounds;
        this.stage = stageMoveBinding.stage();
        this.insets = simplePossibleButEmpty();

        this.stage.xProperty().addListener((property, oldV, newV) -> {
            if ( stageMoveBinding.isMoving() ) {
                return;
            }
            this.onChange((double) newV, this.stage.getY(), this.stage.getWidth(), this.stage.getHeight());
        });

        this.stage.yProperty().addListener((property, oldV, newV) -> {
            if ( stageMoveBinding.isMoving() ) {
                return;
            }
            this.onChange(this.stage.getX(), (double) newV, this.stage.getWidth(), this.stage.getHeight());
        });

        this.stage.heightProperty().addListener((property, oldV, newV) -> {
            this.onChange(this.stage.getX(), this.stage.getY(), this.stage.getWidth(), (double) newV);
        });

        this.stage.widthProperty().addListener((property, oldV, newV) -> {
            this.onChange(this.stage.getX(), this.stage.getY(), (double) newV, this.stage.getHeight());
        });

        StageMoveBinding.Move.Interceptor interceptor = (stageAnchorMove, mouseMove) -> {
            Insets currentInsets = this.insets.or(this.zeroInsets);

            EnumSet<Screen.Side> collisions = this.bounds.findCollisions(
                    stageAnchorMove.x() + currentInsets.getLeft(),
                    stageAnchorMove.y() + currentInsets.getTop(),
                    this.stage.getWidth() - currentInsets.getLeft() - currentInsets.getRight(),
                    this.stage.getHeight() - currentInsets.getTop() - currentInsets.getBottom());

            if ( collisions.isEmpty() ) {
                return;
            }

            if ( collisions.contains(TOP) || collisions.contains(BOTTOM) ) {
                stageAnchorMove.ignoreY();
            }

            if ( collisions.contains(LEFT) || collisions.contains(RIGHT) ) {
                stageAnchorMove.ignoreX();
            }
        };

        stageMoveBinding.intercept(interceptor);
    }

    public StageInBoundsKeeper(Stage stage, Rectangle bounds) {
        this.bounds = bounds;
        this.stage = stage;
        this.insets = simplePossibleButEmpty();

        this.stage.xProperty().addListener((property, oldV, newV) -> {
            this.onChange((double) newV, this.stage.getY(), this.stage.getWidth(), this.stage.getHeight());
        });

        this.stage.yProperty().addListener((property, oldV, newV) -> {
            this.onChange(this.stage.getX(), (double) newV, this.stage.getWidth(), this.stage.getHeight());
        });

        this.stage.heightProperty().addListener((property, oldV, newV) -> {
            this.onChange(this.stage.getX(), this.stage.getY(), this.stage.getWidth(), (double) newV);
        });

        this.stage.widthProperty().addListener((property, oldV, newV) -> {
            this.onChange(this.stage.getX(), this.stage.getY(), (double) newV, this.stage.getHeight());
        });
    }

    public void setStageInsets(Insets insets) {
        this.insets.resetTo(insets);
    }

    private void onChange(double stageX, double stageY, double stageWidth, double stageHeight) {
        Insets currentInsets = this.insets.or(this.zeroInsets);

        double x = stageX + currentInsets.getLeft();
        double y = stageY + currentInsets.getTop();
        double width = stageWidth - currentInsets.getLeft() - currentInsets.getRight();
        double height = stageHeight - currentInsets.getTop() - currentInsets.getBottom();

        if ( this.bounds.contains(x, y, width, height) ) {
            return;
        }

        EnumSet<Rectangle.Side> collisions = this.bounds.findCollisions(x, y, width, height);

        if ( collisions.isEmpty() ) {
            return;
        }

        List<Runnable> collisionFixes = new ArrayList<>();
        for ( Rectangle.Side side : collisions ) {
            switch ( side ) {
                case TOP:
                    collisionFixes.add(() -> {
                        this.stage.setY(this.bounds.anchor().y() - currentInsets.getTop());
                    });
                    break;
                case BOTTOM:
                    collisionFixes.add(() -> {
                        this.stage.setY(this.bounds.oppositeAnchorY() - stageHeight + currentInsets.getBottom());
                    });
                    break;
                case LEFT:
                    collisionFixes.add(() -> {
                        this.stage.setX(this.bounds.anchor().x() - currentInsets.getLeft());
                    });
                    break;
                case RIGHT:
                    collisionFixes.add(() -> {
                        this.stage.setX(this.bounds.oppositeAnchorX() - stageWidth + currentInsets.getRight());
                    });
                    break;
                default:
                    side.throwUnsupported();
            }
        }

        Platform.runLater(() -> {
            for ( Runnable fix : collisionFixes ) {
                fix.run();
            }
        });
    }
}
