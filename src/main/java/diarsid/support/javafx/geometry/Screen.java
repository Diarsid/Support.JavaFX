package diarsid.support.javafx.geometry;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;

import diarsid.desktop.ui.geometry.Anchor;
import diarsid.desktop.ui.geometry.RealRectangleAreas;
import diarsid.desktop.ui.geometry.Size;
import diarsid.support.javafx.PlatformActions;
import diarsid.support.objects.CommonEnum;

import static diarsid.support.javafx.geometry.Screen.Type.PHYSICAL;
import static diarsid.support.javafx.geometry.Screen.Type.SYSTEM;

public class Screen extends RealRectangleAreas {

    public static enum Type implements CommonEnum<Type> {
        PHYSICAL,
        SYSTEM
    }

    public final Type type;

    private Screen(Type type, Anchor anchor, Size size) {
        super(anchor, size, size.width()/20);
        this.type = type;
    }

    public static Screen screenOf(Type type) {
        if ( Platform.isFxApplicationThread() ) {
            return createScreenOf(type);
        }
        else {
            PlatformActions.awaitStartup();
            return PlatformActions.doGet(() -> {
                return createScreenOf(type);
            });
        }
    }

    private static Screen createScreenOf(Type type) {
        Screen screen;
        Rectangle2D bounds;

        switch (type) {
            case PHYSICAL:
                bounds = javafx.stage.Screen.getPrimary().getBounds();
                screen = new Screen(
                        PHYSICAL,
                        Anchor.anchor(0, 0),
                        Size.size(bounds.getWidth(), bounds.getHeight()));
                break;
            case SYSTEM:
                bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                screen = new Screen(
                        SYSTEM,
                        Anchor.anchor(bounds.getMinX(), bounds.getMinY()),
                        Size.size(bounds.getWidth(), bounds.getHeight()));
                break;
            default:
                throw type.unsupported();
        }

        return screen;
    }

    public static void main(String[] args) {
        Screen screen = screenOf(PHYSICAL);
        System.out.println(screen.width());
    }
}
