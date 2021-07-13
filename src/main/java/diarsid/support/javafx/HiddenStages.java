package diarsid.support.javafx;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static java.lang.Double.MAX_VALUE;

public class HiddenStages {

    private final Map<Stage, Stage> hiddenStagesForStages;

    public HiddenStages() {
        this.hiddenStagesForStages = new HashMap<>();
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

    public Stage newHiddenStageFor(Stage stage) {
        synchronized ( this.hiddenStagesForStages ) {
            Stage newHiddenStage = this.createHiddenStage();
            this.hiddenStagesForStages.put(stage, newHiddenStage);
            return newHiddenStage;
        }
    }

    void closeAllHiddenStages() {
        Platform.runLater(() -> this.hiddenStagesForStages.values().forEach(Stage::close));
    }
}
