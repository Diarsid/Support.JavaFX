package diarsid.support.javafx.css.pseudoclasses;

import javafx.scene.Node;

public interface PseudoClassStateApplicable {

    default void apply(PseudoClassState state) {
        if ( this instanceof Node ) {
            Node thisNode = (Node) this;
            thisNode.pseudoClassStateChanged(state.pseudoClass, state.active);
        }
    }
}
