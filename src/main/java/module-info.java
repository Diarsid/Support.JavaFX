module diarsid.support.javafx {

    requires java.desktop;
    requires javafx.controls;
    requires javafx.swing;
    requires diarsid.filesystem;
    requires diarsid.support;

    exports diarsid.support.javafx;
    exports diarsid.support.javafx.pseudoclasses;
}
