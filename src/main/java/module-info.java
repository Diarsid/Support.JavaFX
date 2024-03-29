module diarsid.support.javafx {

    requires java.desktop;
    requires javafx.controls;
    requires javafx.swing;
    requires org.slf4j;
    requires diarsid.filesystem;
    requires diarsid.support;
    requires diarsid.desktop.ui;

    exports diarsid.support.javafx;
    exports diarsid.support.javafx.components;
    exports diarsid.support.javafx.controls;
    exports diarsid.support.javafx.css.pseudoclasses;
    exports diarsid.support.javafx.geometry;
    exports diarsid.support.javafx.images;
    exports diarsid.support.javafx.mouse;
    exports diarsid.support.javafx.stage;
}
