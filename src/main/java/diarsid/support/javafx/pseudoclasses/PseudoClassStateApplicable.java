package diarsid.support.javafx.pseudoclasses;

import javafx.scene.Node;

public interface PseudoClassStateApplicable {

    default void apply(PseudoClassState state) {
        if ( this instanceof Node ) {
            Node thisNode = (Node) this;
            thisNode.pseudoClassStateChanged(state.pseudoClass, state.active);
        }
    }
}
