package diarsid.support.javafx.css.pseudoclasses;

import javafx.css.PseudoClass;
import javafx.scene.Node;

public class PseudoClassAppliedTo<T extends Node> extends PseudoClassState {

    final T node;

    public PseudoClassAppliedTo(PseudoClassState state, T node) {
        super(state.pseudoClass, state.active);
        this.node = node;
    }

    public PseudoClassAppliedTo(PseudoClass pseudoClass, boolean active, T node) {
        super(pseudoClass, active);
        this.node = node;
    }

    public void revertNodePseudoClass() {
        this.node.pseudoClassStateChanged(super.pseudoClass, ! super.active);
    }
}
