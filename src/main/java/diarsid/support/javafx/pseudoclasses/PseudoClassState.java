package diarsid.support.javafx.pseudoclasses;

import javafx.css.PseudoClass;

public class PseudoClassState {

    public final PseudoClass pseudoClass;
    public final boolean active;

    public PseudoClassState(PseudoClass pseudoClass, boolean active) {
        this.pseudoClass = pseudoClass;
        this.active = active;
    }
}
