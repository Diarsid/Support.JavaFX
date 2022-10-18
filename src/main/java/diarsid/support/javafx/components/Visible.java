package diarsid.support.javafx.components;

import java.util.Optional;
import javafx.scene.Node;
import javafx.scene.Parent;

public interface Visible {

    Node node();

    default boolean isParent() {
        return this.node() instanceof Parent;
    }

    default Optional<Parent> parent() {
        Node node = this.node();

        if ( node instanceof Parent ) {
            return Optional.of((Parent) node);
        }
        else {
            return Optional.empty();
        }
    }
}
