module diarsid.support.javafx {

    requires java.desktop;
    requires javafx.controls;
    requires javafx.swing;
    requires diarsid.filesystem;
    requires diarsid.support;
    requires diarsid.desktop.ui;

    exports diarsid.support.javafx;
    exports diarsid.support.javafx.geometry;
    exports diarsid.support.javafx.images;
    exports diarsid.support.javafx.mouse;
    exports diarsid.support.javafx.stage;
    exports diarsid.support.javafx.css.pseudoclasses;
}
