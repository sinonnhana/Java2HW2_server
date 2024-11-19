package org.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import static org.example.demo.Game.SetupBoard;

public class ApplicationLauncher extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException {
        launchApp(stage);
    }

    public static void launchApp(Stage stage) throws IOException {
        int[] size = getBoardSizeFromUser();
        Controller.game = new Game(SetupBoard(size[0], size[1]));

        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLauncher.class.getResource("board.fxml"));
        VBox root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        controller.createGameBoard();

        Scene scene = new Scene(root);
        stage.setTitle("连连看游戏");
        stage.setScene(scene);
        stage.show();




    }

    private static int[] getBoardSizeFromUser() {
        return new int[]{9, 6};  // 默认棋盘大小
    }
}
