package diarsid.support.javafx.geometry;

import javafx.geometry.Rectangle2D;

import diarsid.desktop.ui.geometry.Anchor;
import diarsid.desktop.ui.geometry.RealRectangleAreas;
import diarsid.desktop.ui.geometry.Size;
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
        Rectangle2D bounds;
        switch ( type ) {
            case PHYSICAL:
                bounds = javafx.stage.Screen.getPrimary().getBounds();
                return new Screen(
                        PHYSICAL,
                        Anchor.anchor(0, 0),
                        Size.size(bounds.getWidth(), bounds.getHeight()));
            case SYSTEM:
                bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                return new Screen(
                        SYSTEM,
                        Anchor.anchor(bounds.getMinX(), bounds.getMinY()),
                        Size.size(bounds.getWidth(), bounds.getHeight()));
            default:
                throw type.unsupported();
        }
    }
}
