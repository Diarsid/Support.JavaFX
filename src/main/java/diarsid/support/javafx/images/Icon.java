package diarsid.support.javafx.images;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import diarsid.support.concurrency.threads.NamedThreadSource;
import diarsid.support.javafx.components.Visible;
import diarsid.support.model.Named;
import diarsid.support.objects.references.Possible;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static javafx.scene.input.MouseEvent.MOUSE_EXITED;
import static javafx.scene.input.MouseEvent.MOUSE_MOVED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

import static diarsid.support.concurrency.threads.ThreadsUtil.shutdownAndWait;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;

public final class Icon implements Visible, Named, Closeable {

    public static class InstantBrightnessChange implements Consumer<ImageView> {

        public final DoubleProperty value;
        private final ColorAdjust brightness;

        public InstantBrightnessChange(double value) {
            this(new SimpleDoubleProperty(value));
        }

        public InstantBrightnessChange(DoubleProperty value) {
            this.value = value;
            this.brightness = new ColorAdjust();
            this.brightness.setBrightness(this.value.get());
            this.brightness.brightnessProperty().bind(this.value);
        }

        @Override
        public void accept(ImageView imageView) {
            Effect effect = imageView.getEffect();
            if ( nonNull(effect) ) {
                this.brightness.setInput(effect);
            }
            imageView.setEffect(this.brightness);
        }
    }

    private static final Function<String, Image> PATH_TO_IMAGE = (path) -> new Image("file:" + path, false);

    private final String name;
    private final ExecutorService async;
    private final ReadOnlyObjectProperty<Image> image;
    private final ImageView imageView;
    private final Possible<Effect> defaultCssEffect;
    private final Consumer<ImageView> onEntered;
    private final Consumer<ImageView> onPressed;
    private final Label label;
    private final Consumer<String> onInvoked;
    private boolean isHovered;

    public Icon(
            ImageByAddress imageByAddress,
            NamedThreadSource namedThreadSource,
            Consumer<String> onInvoked,
            Consumer<ImageView> onEntered,
            Consumer<ImageView> onPressed) {
        this(imageByAddress.name(), namedThreadSource, imageByAddress.image(), onInvoked, onEntered, onPressed);
    }

    public Icon(
            String name,
            NamedThreadSource namedThreadSource,
            ReadOnlyObjectProperty<Image> image,
            Consumer<String> onInvoked,
            Consumer<ImageView> onEntered,
            Consumer<ImageView> onPressed) {
        this.name = name;
        this.async = namedThreadSource.newNamedFixedThreadPool(
                this.getClass().getSimpleName() + ".[" + this.name + "]",
                1);
        this.image = image;

        this.onEntered = onEntered;
        this.onPressed = onPressed;

        this.label = new Label();
        this.label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.label.setTooltip(new Tooltip(name));

        DoubleProperty width = this.label.minWidthProperty();
        DoubleProperty height = this.label.minHeightProperty();

        this.label.maxWidthProperty().bind(width);
        this.label.maxHeightProperty().bind(height);

        this.imageView = new ImageView();

        this.imageView.fitWidthProperty().bind(width);
        this.imageView.fitHeightProperty().bind(height);
        this.imageView.setPreserveRatio(true);

        this.imageView.getStyleClass().add("icon");
        this.imageView.getStyleClass().add("icon-" + this.name);
        this.imageView.getStyleClass().add(this.name);

        this.imageView.setImage(this.image.get());
        this.imageView.imageProperty().bind(this.image);

        this.label.setGraphic(this.imageView);

        this.defaultCssEffect = simplePossibleButEmpty();
        this.isHovered = false;

        this.label.hoverProperty().addListener(((observable, oldValue, newValue) -> {
            this.getInitialEffectAtFirstRun();
            if ( (! oldValue) && newValue ) {
                if ( this.onEntered != null ) {
                    this.onEntered.accept(this.imageView);
                }
            }
            else {
                this.imageView.setEffect(this.defaultCssEffect.or(null));
            }
        }));

        this.label.addEventHandler(MOUSE_MOVED, event -> {
            Effect effect = this.imageView.getEffect();
            if ( isNull(effect) || this.defaultCssEffect.equalsTo(effect) ) {
                if ( this.onEntered != null ) {
                    this.onEntered.accept(this.imageView);
                }
            }
        });

        this.label.addEventHandler(MOUSE_PRESSED, event -> {
            if ( this.onPressed != null ) {
                this.onPressed.accept(this.imageView);
            }
            this.click(event);
        });

        this.label.addEventHandler(MOUSE_RELEASED, event -> {
            if ( this.onEntered != null ) {
                this.onEntered.accept(this.imageView);
            }
        });

        this.label.addEventHandler(MOUSE_EXITED, event -> {
            this.imageView.setEffect(this.defaultCssEffect.or(null));
        });

        this.onInvoked = onInvoked;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Node node() {
        return this.label;
    }

    @Override
    public void close() {
        shutdownAndWait(this.async);
    }

    void click(MouseEvent event) {
        if ( event.isPrimaryButtonDown() ) {
            this.onInvoked.accept(this.name);
        }
        event.consume();
    }

    private synchronized void getInitialEffectAtFirstRun() {
        if ( ! this.isHovered ) {
            this.isHovered = true;
            Effect initialEffect =  this.imageView.getEffect();
            if ( nonNull(initialEffect) ) {
                this.defaultCssEffect.resetTo(initialEffect);
            }
        }
    }
}
