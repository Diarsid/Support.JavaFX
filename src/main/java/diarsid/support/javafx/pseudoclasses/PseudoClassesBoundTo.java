package diarsid.support.javafx.pseudoclasses;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.css.PseudoClass;

public class PseudoClassesBoundTo<T> {

    final Map<T, Map<PseudoClass, Boolean>> pseudoClassesByT;

    public PseudoClassesBoundTo() {
        this.pseudoClassesByT = new HashMap<>();
    }

    public void add(T t, String pseudoClassName, boolean active) {
        add(t, PseudoClass.getPseudoClass(pseudoClassName), active);
    }

    public void add(T t, PseudoClass pseudoClass, boolean active) {
        Map<PseudoClass, Boolean> pseudoClasses = pseudoClassesByT.get(t);

        if ( pseudoClasses == null ) {
            pseudoClasses = new HashMap<>();
            pseudoClassesByT.put(t, pseudoClasses);
        }

        pseudoClasses.put(pseudoClass, active);
    }

    public PseudoClassState get(T t, PseudoClass pseudoClass) {
        Map<PseudoClass, Boolean> pseudoClasses = pseudoClassesByT.get(t);

        if ( pseudoClasses == null ) {
            return new PseudoClassState(pseudoClass, false);
        }

        Boolean existingState = pseudoClasses.get(pseudoClass);

        if ( existingState == null ) {
            existingState = false;
        }

        return new PseudoClassState(pseudoClass, existingState);
    }

    public void forEach(BiConsumer<T, Map<PseudoClass, Boolean>> forEach) {
        pseudoClassesByT.forEach(forEach);
    }
}
