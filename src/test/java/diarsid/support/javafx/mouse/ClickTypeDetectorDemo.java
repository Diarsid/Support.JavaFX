package diarsid.support.javafx.mouse;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.support.javafx.PlatformActions;

import static diarsid.support.javafx.mouse.ClickType.DOUBLE_CLICK;
import static diarsid.support.javafx.mouse.ClickType.SEQUENTIAL_CLICK;
import static diarsid.support.javafx.mouse.ClickType.USUAL_CLICK;

public class ClickTypeDetectorDemo {


    public static void main(String[] args) {
        PlatformActions.awaitStartup();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(false);
            stage.setMinWidth(120);
            stage.setMinHeight(120);
            stage.setResizable(true);

            HBox hBox = new HBox();
            hBox.setMinWidth(120);
            hBox.setMinHeight(120);
            hBox.setStyle("-fx-background-color: lightskyblue");

            ClickTypeDetector detector = ClickTypeDetector
                    .Builder
                    .createFor(hBox)
                    .withName("hbox")
                    .withDoOn(USUAL_CLICK, (event) -> {
                        System.out.println("USUAL");
                    })
                    .withDoOn(SEQUENTIAL_CLICK, (event) -> {
                        System.out.println("SEQUENTIAL");
                    })
                    .withDoOn(DOUBLE_CLICK, (event) -> {
                        System.out.println("DOUBLE");
                    })
                    .build();


            Scene scene = transparentScene(hBox);
            showTransparentStage(stage, scene);
        });

    }

    private static Scene transparentScene(Parent root) {
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private static void showTransparentStage(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }
}
