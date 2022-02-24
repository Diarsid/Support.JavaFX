package diarsid.support.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

public class PropertiesUtil {

    public static void revert(BooleanProperty booleanProperty) {
        boolean value = booleanProperty.get();
        booleanProperty.set( ! value );
    }

    public static void increment(IntegerProperty integerProperty) {
        integerProperty.set(integerProperty.get() + 1);
    }

    public static void decrement(IntegerProperty integerProperty) {
        integerProperty.set(integerProperty.get() - 1);
    }

    public static <T, R> void bindMapping(Property<T> source, Function<T, R> mapSourceToTarget, Property<R> target) {
        Property<R> rBinding = new SimpleObjectProperty<>(mapSourceToTarget.apply(source.getValue()));

        source.addListener((prop, oldValue, newValue) -> {
            rBinding.setValue(mapSourceToTarget.apply(newValue));
        });

        target.bind(rBinding);
    }

    public static <T, R> void bindMapping(ReadOnlyProperty<T> source, Function<T, R> mapSourceToTarget, Property<R> target) {
        Property<R> rBinding = new SimpleObjectProperty<>(mapSourceToTarget.apply(source.getValue()));

        source.addListener((prop, oldValue, newValue) -> {
            rBinding.setValue(mapSourceToTarget.apply(newValue));
        });

        target.bind(rBinding);
    }

    public static <T, R> void bindMultiMapping(
            Property<R> target,
            Function<List<T>, R> mapSourcesToTarget,
            ReadOnlyProperty<T>... sources) {
        List<T> values = new ArrayList<>();

        for ( ReadOnlyProperty<T> source : sources ) {
            values.add(source.getValue());
        }

        Property<R> rBinding = new SimpleObjectProperty<>(mapSourcesToTarget.apply(values));

        ChangeListener<T> listener = (prop, oldValue, newValue) -> {
            R r;
            synchronized ( values ) {
                for ( ReadOnlyProperty<T> source : sources ) {
                    values.add(source.getValue());
                }
                r = mapSourcesToTarget.apply(values);
            }
            rBinding.setValue(r);
        };

        for ( ReadOnlyProperty<T> source : sources ) {
            source.addListener(listener);
        }

        target.bind(rBinding);
    }

    public static <T0, T1, R> void bindJoin2Mapping(
            Property<R> target,
            BiFunction<T0, T1, R> mapSourcesToTarget,
            ReadOnlyProperty<T0> t0Source,
            ReadOnlyProperty<T1> t1Source) {
        R initial = mapSourcesToTarget.apply(
                t0Source.getValue(),
                t1Source.getValue());

        Property<R> rBinding = new SimpleObjectProperty<>(initial);

        t0Source.addListener((prop, oldValue, newValue) -> {
            T1 t1 = t1Source.getValue();
            R r = mapSourcesToTarget.apply(newValue, t1);
            rBinding.setValue(r);
        });

        t1Source.addListener((prop, oldValue, newValue) -> {
            T0 t0 = t0Source.getValue();
            R r = mapSourcesToTarget.apply(t0, newValue);
            rBinding.setValue(r);
        });

        target.bind(rBinding);
    }
}
