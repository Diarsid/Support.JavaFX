package diarsid.support.javafx;

import java.util.function.Function;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

public class Labeled<T> extends Label {

    private final Function<T, String> toString;
    private final ObjectProperty<T> property;

    public Labeled(T t, Function<T, String> toString) {
        super(toString.apply(t));
        this.toString = toString;
        this.property = new SimpleObjectProperty<>(t);
        this.bindTextToProperty();
    }

    public Labeled(ObjectProperty<T> tProperty, Function<T, String> toString) {
        super(toString.apply(tProperty.get()));
        this.toString = toString;
        this.property = new SimpleObjectProperty<>();
        this.property.bind(tProperty);
        this.bindTextToProperty();
    }

    private void bindTextToProperty() {
        this.property.addListener((prop, oldV, newV) -> {
            super.setText(this.toString.apply(newV));
        });
    }

    public ObjectProperty<T> property() {
        return this.property;
    }
}
