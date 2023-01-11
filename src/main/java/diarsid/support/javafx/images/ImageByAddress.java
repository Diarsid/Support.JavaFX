package diarsid.support.javafx.images;

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

import diarsid.support.model.Named;

import static diarsid.support.javafx.PropertiesUtil.bindMapping;

public class ImageByAddress implements Named {

    private final String name;
    private final ObjectProperty<Image> image;
    private final StringProperty address;

    public ImageByAddress(String name, String address) {
        this.name = name;
        this.address = new SimpleStringProperty(address);

        this.image = new SimpleObjectProperty<>();
        this.image.set(new Image("file:" + address, false));

        bindMapping(
                this.address,
                (s) -> new Image("file:" + s),
                this.image);
    }

    @Override
    public String name() {
        return this.name;
    }

    public ReadOnlyObjectProperty<Image> image() {
        return this.image;
    }

    public StringProperty address() {
        return this.address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageByAddress)) return false;
        ImageByAddress that = (ImageByAddress) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
