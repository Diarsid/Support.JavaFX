package diarsid.support.javafx;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class FilesNativeIconImageExtractor {

    private final FileSystemView fileSystemView;
    private final Map<String, Image> filesImages;

    public FilesNativeIconImageExtractor() {
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.filesImages = new HashMap<>();
    }

    public Image getFrom(File file) {
        ImageIcon icon = (ImageIcon) this.fileSystemView.getSystemIcon(file);
        java.awt.Image awtImage = icon.getImage();
        BufferedImage buffImage = new BufferedImage(awtImage.getWidth(null), awtImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = buffImage.createGraphics();
        bGr.drawImage(awtImage, 0, 0, null);
        bGr.dispose();
        Image fxIcon = SwingFXUtils.toFXImage(buffImage, null);
        return fxIcon;
    }
}


