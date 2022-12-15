package diarsid.support.javafx.components;

import javafx.beans.property.BooleanProperty;

import static diarsid.support.javafx.PropertiesUtil.revert;

public interface Movable {

    BooleanProperty movePermission();

    default boolean canMove() {
        return this.movePermission().get();
    }

    default boolean canNotMove() {
        return ! this.movePermission().get();
    }

    default void setMovePermitted(boolean movable) {
        this.movePermission().set(movable);
    }

    default void revertPermit() {
        revert(this.movePermission());
    }
}
