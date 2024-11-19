package org.example.demo;

import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.util.Objects;

public class Controller {

    @FXML
    private Label scoreLabel;

    @FXML
    private GridPane gameBoard;

    public static Game game;

    int[] position = new int[3];

    private final IntegerProperty score = new SimpleIntegerProperty(0);

    @FXML
    public void initialize() {
        scoreLabel.textProperty().bind(score.asString());
    }

    public void createGameBoard() {

        gameBoard.getChildren().clear();

        for (int row = 0; row < game.row; row++) {
            for (int col = 0; col < game.col; col++) {
                Button button = new Button();
                button.setPrefSize(40, 40);
                ImageView imageView = addContent(game.board[row][col]);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                int finalRow = row;
                int finalCol = col;
                button.setOnAction( _ -> handleButtonPress(finalRow, finalCol));
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonPress(int row, int col) {
        System.out.println("Button pressed at: " + row + ", " + col);
        if(position[0] == 0){
            position[1] = row;
            position[2] = col;
            position[0] = 1;
        }else{
            boolean change = game.judge(position[1], position[2], row, col);
            position[0] = 0;
            if(change){
                System.out.println("delete: (" + position[1] + "," + position[2] + "),(" + row + "," + col + ")");
                // TODO: handle the grid deletion logic
                if (game.board[row][col] != 0) {
                    score.set(score.get() + 1);
                    game.board[position[1]][position[2]] = 0;
                    game.board[row][col] = 0;

                    updateButtonImage(position[1], position[2], game.board[position[1]][position[2]]);
                    updateButtonImage(row, col, game.board[row][col]);

                    disableAllButtons();

                    disableAllButtons(); // 例如禁用所有按钮
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));

                    // 设置在暂停结束后要执行的操作
                    pause.setOnFinished(event -> {
                        // 这里写上你想在等待2秒后执行的操作
                         enableAllButtons();

                    });

                    // 开始暂停
                    pause.play();






                }
            }
        }
    }

    private void disableAllButtons() {
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button) {
                node.setDisable(true); // 设置按钮不可用
            }
        }
    }

    private void enableAllButtons() {
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button) {
                node.setDisable(false); // 设置按钮不可用
            }
        }
    }

    private void updateButtonImage(int row, int col, int content) {
        Button button = (Button) getNodeByRowColumnIndex(row, col, gameBoard);
        if (button != null) {
            ImageView imageView = addContent(content);
            imageView.setFitWidth(30);
            imageView.setFitHeight(30);
            imageView.setPreserveRatio(true);
            button.setGraphic(imageView);
        }
    }

    private Node getNodeByRowColumnIndex(int row, int col, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer columnIndex = GridPane.getColumnIndex(node);

            if (rowIndex != null && columnIndex != null && rowIndex == row && columnIndex == col) {
                return node;
            }
        }
        return null;
    }

    @FXML
    private void handleReset() {

    }

    public ImageView addContent(int content){
        return switch (content) {
            case 0 -> new ImageView(imageCarambola);
            case 1 -> new ImageView(imageApple);
            case 2 -> new ImageView(imageMango);
            case 3 -> new ImageView(imageBlueberry);
            case 4 -> new ImageView(imageCherry);
            case 5 -> new ImageView(imageGrape);
            case 6 -> new ImageView(imageKiwi);
            case 7 -> new ImageView(imageOrange);
            case 8 -> new ImageView(imagePeach);
            case 9 -> new ImageView(imagePear);
            case 10 -> new ImageView(imagePineapple);
            case 11 -> new ImageView(imageWatermelon);
            default -> new ImageView(imageCarambola);
        };
    }

    public static Image imageApple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/apple.png")).toExternalForm());
    public static Image imageMango = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/mango.png")).toExternalForm());
    public static Image imageBlueberry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/blueberry.png")).toExternalForm());
    public static Image imageCherry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/cherry.png")).toExternalForm());
    public static Image imageGrape = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/grape.png")).toExternalForm());
    public static Image imageCarambola = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/carambola.png")).toExternalForm());
    public static Image imageKiwi = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/kiwi.png")).toExternalForm());
    public static Image imageOrange = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/orange.png")).toExternalForm());
    public static Image imagePeach = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/peach.png")).toExternalForm());
    public static Image imagePear = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pear.png")).toExternalForm());
    public static Image imagePineapple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pineapple.png")).toExternalForm());
    public static Image imageWatermelon = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/watermelon.png")).toExternalForm());

}
