package diarsid.support.javafx.stage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static java.lang.Double.MAX_VALUE;
import static java.util.Objects.nonNull;

public class HiddenStages {

    private final Map<Stage, Stage> hiddenStagesForStages;

    public HiddenStages() {
        this.hiddenStagesForStages = new ConcurrentHashMap<>();
    }

    private Stage createHiddenStage() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setMaxWidth(0);
        stage.setMaxHeight(0);
        stage.setX(MAX_VALUE);
        stage.setY(MAX_VALUE);

        Scene scene = new Scene(new Label());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        return stage;
    }

    public Stage newHiddenStage() {
        Stage newStage = new Stage();
        Stage newHiddenStage = this.createHiddenStage();

        this.hiddenStagesForStages.put(newStage, newHiddenStage);

        newStage.initOwner(newHiddenStage);

        return newStage;
    }

    void closeHidden(Stage stage) {
        Stage hiddenStage = this.hiddenStagesForStages.get(stage);
        if ( nonNull(hiddenStage) ) {
            Platform.runLater(hiddenStage::close);
        }
    }

    void closeAllHiddenStages() {
        Platform.runLater(() -> this.hiddenStagesForStages.values().forEach(Stage::close));
    }
}
