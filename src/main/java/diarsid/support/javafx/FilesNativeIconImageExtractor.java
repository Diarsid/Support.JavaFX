package diarsid.support.javafx;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import diarsid.files.Extension;
import diarsid.files.Extensions;
import diarsid.support.objects.CommonEnum;

import static java.util.Objects.nonNull;

public class FilesNativeIconImageExtractor {

    public static enum PathCache implements CommonEnum<PathCache> {
        USE, NO_USE
    }

    public static enum ExtensionCache implements CommonEnum<ExtensionCache> {
        USE, NO_USE
    }

    private final FileSystemView fileSystemView;
    private final ConcurrentHashMap<String, Image> filesImages;
    private final ConcurrentHashMap<Extension, Image> extensionImages;
    private final Extensions extensions;

    public FilesNativeIconImageExtractor(Extensions extensions) {
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.filesImages = new ConcurrentHashMap<>();
        this.extensionImages = new ConcurrentHashMap<>();
        this.extensions = extensions;
    }

    public Image getFrom(File file, PathCache pathCache, ExtensionCache extensionCache) {
        String path = file.toString();

        if ( pathCache.is(PathCache.USE) ) {
            Image cached = this.filesImages.get(path);
            if ( nonNull(cached) ) {
                return cached;
            }
        }

        Optional<Extension> optExtension = Optional.empty();
        if ( extensionCache.is(ExtensionCache.USE) ) {
            optExtension = this.extensions.getFor(file.getName());
            if ( optExtension.isPresent() ) {
                Extension extension = optExtension.get();
                Image cached = this.extensionImages.get(extension);
                if ( nonNull(cached) ) {
                    return cached;
                }
            }
        }

        ImageIcon icon = (ImageIcon) this.fileSystemView.getSystemIcon(file);
        java.awt.Image awtImage = icon.getImage();
        BufferedImage buffImage = new BufferedImage(awtImage.getWidth(null), awtImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = buffImage.createGraphics();
        bGr.drawImage(awtImage, 0, 0, null);
        bGr.dispose();
        Image fxIcon = SwingFXUtils.toFXImage(buffImage, null);

        this.filesImages.put(path, fxIcon);
        if ( optExtension.isPresent() ) {
            this.extensionImages.put(optExtension.get(), fxIcon);
        }

        return fxIcon;
    }
}


