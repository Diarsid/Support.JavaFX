package diarsid.support.javafx.components;

import java.util.List;

import javafx.scene.image.Image;

public interface ImagesHolder {

    List<String> properties();

    void setImage(String property, Image image);

    void removeImage(String property);

    boolean hasImage(String property);

    boolean isAllowed(String property);
}
