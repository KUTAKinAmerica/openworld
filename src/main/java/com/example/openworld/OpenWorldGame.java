package com.example.openworld;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class OpenWorldGame extends Application {
    private static final int WORLD_WIDTH = 2000;
    private static final int WORLD_HEIGHT = 2000;

    private Pane gameRoot = new Pane();
    private Pane uiRoot = new Pane();

    private Rectangle player;
    private double playerX = 400;
    private double playerY = 300;
    private double health = 1.0;

    private double cameraX = 0;
    private double cameraY = 0;

    private Set<KeyCode> keysPressed = new HashSet<>();

    private Label nameLabel = new Label("Player");
    private ProgressBar healthBar = new ProgressBar(1.0);
    private Canvas minimap = new Canvas(200, 150);

    private BorderPane root = new BorderPane();
    private ComboBox<String> resolutionDropdown = new ComboBox<>();
    private int WIDTH = 800;
    private int HEIGHT = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane stack = new StackPane();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        stack.getChildren().add(canvas);
        root.setCenter(stack);

        player = new Rectangle(40, 40, Color.BLUE);
        gameRoot.getChildren().add(player);

        VBox ui = new VBox(10);
        ui.setTranslateX(10);
        ui.setTranslateY(10);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        healthBar.setPrefWidth(200);
        ui.getChildren().addAll(nameLabel, healthBar);
        uiRoot.getChildren().add(ui);

        minimap.setTranslateX(WIDTH - 210);
        minimap.setTranslateY(10);
        uiRoot.getChildren().add(minimap);

        stack.getChildren().addAll(gameRoot, uiRoot);

        MenuBar menuBar = new MenuBar();
        Menu gameMenu = new Menu("Game");
        MenuItem newGame = new MenuItem("New Game");
        MenuItem exitGame = new MenuItem("Exit");
        exitGame.setOnAction(e -> primaryStage.close());
        newGame.setOnAction(e -> loadGame());
        gameMenu.getItems().addAll(newGame, exitGame);

        Menu settingsMenu = new Menu("Settings");
        MenuItem graphics = new MenuItem("Graphics");
        graphics.setOnAction(e -> showGraphicsSettings(primaryStage, canvas));
        settingsMenu.getItems().addAll(graphics);

        menuBar.getMenus().addAll(gameMenu, settingsMenu);
        root.setTop(menuBar);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));

        primaryStage.setTitle("Open World Game");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> saveGame());
        primaryStage.show();

        // Обновляем размеры UI-элементов при изменении размера окна
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> adjustUI(newWidth.doubleValue(), scene.getHeight()));
        scene.heightProperty().addListener((obs, oldHeight, newHeight) -> adjustUI(scene.getWidth(), newHeight.doubleValue()));

        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now) {
                update();
                render(gc, canvas);
            }
        };
        timer.start();
    }

    private void update() {
        double speed = 5;
        if (keysPressed.contains(KeyCode.W)) playerY -= speed;
        if (keysPressed.contains(KeyCode.S)) playerY += speed;
        if (keysPressed.contains(KeyCode.A)) playerX -= speed;
        if (keysPressed.contains(KeyCode.D)) playerX += speed;

        // Ограничение игрока в пределах карты
        playerX = Math.max(0, Math.min(playerX, WORLD_WIDTH - player.getWidth()));
        playerY = Math.max(0, Math.min(playerY, WORLD_HEIGHT - player.getHeight()));

        // Плавная камера
        double targetCamX = playerX - WIDTH / 2.0 + player.getWidth() / 2.0;
        double targetCamY = playerY - HEIGHT / 2.0 + player.getHeight() / 2.0;
        cameraX += (targetCamX - cameraX) * 0.1;
        cameraY += (targetCamY - cameraY) * 0.1;

        // Ограничение камеры
        cameraX = Math.max(0, Math.min(cameraX, WORLD_WIDTH - WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, WORLD_HEIGHT - HEIGHT));

        player.setTranslateX(playerX - cameraX);
        player.setTranslateY(playerY - cameraY);
    }

    private void render(GraphicsContext gc, Canvas canvas) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.GREEN);
        gc.fillRect(-cameraX, -cameraY, WORLD_WIDTH, WORLD_HEIGHT);

        gc.setFill(Color.RED);
        gc.fillRect(playerX - cameraX, playerY - cameraY, player.getWidth(), player.getHeight());

        GraphicsContext miniGC = minimap.getGraphicsContext2D();
        miniGC.setFill(Color.DARKGRAY);
        miniGC.fillRect(0, 0, 200, 150);

        double scaleX = 200.0 / WORLD_WIDTH;
        double scaleY = 150.0 / WORLD_HEIGHT;

        miniGC.setFill(Color.BLUE);
        miniGC.fillOval(playerX * scaleX, playerY * scaleY, 5, 5);

        healthBar.setProgress(health);
    }

    private void showGraphicsSettings(Stage primaryStage, Canvas canvas) {
        Stage settingsStage = new Stage();
        VBox settingsLayout = new VBox(10);
        settingsLayout.setStyle("-fx-padding: 20px;");

        resolutionDropdown.getItems().addAll("800x600", "1024x768", "1280x720", "1920x1080");
        resolutionDropdown.setValue("800x600");

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> {
            String selectedResolution = resolutionDropdown.getValue();
            if (selectedResolution != null) {
                String[] parts = selectedResolution.split("x");
                int newWidth = Integer.parseInt(parts[0]);
                int newHeight = Integer.parseInt(parts[1]);
                WIDTH = newWidth;
                HEIGHT = newHeight;
                primaryStage.setWidth(newWidth);
                primaryStage.setHeight(newHeight);
                canvas.setWidth(newWidth);
                canvas.setHeight(newHeight);
            }
            settingsStage.close();
        });

        settingsLayout.getChildren().addAll(new Label("Resolution:"), resolutionDropdown, applyButton);
        Scene settingsScene = new Scene(settingsLayout);
        settingsStage.setScene(settingsScene);
        settingsStage.setTitle("Graphics Settings");
        settingsStage.show();
    }

    private void saveGame() {
        GameState state = new GameState();
        state.playerX = this.playerX;
        state.playerY = this.playerY;

        try (FileWriter writer = new FileWriter("save.json")) {
            Gson gson = new Gson();
            gson.toJson(state, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGame() {
        try (FileReader reader = new FileReader("save.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<GameState>() {}.getType();
            GameState state = gson.fromJson(reader, type);
            this.playerX = state.playerX;
            this.playerY = state.playerY;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class GameState {
        double playerX;
        double playerY;
    }

    // Метод для адаптивного позиционирования UI-элементов
    private void adjustUI(double newWidth, double newHeight) {
        nameLabel.setTranslateX(newWidth * 0.02);
        nameLabel.setTranslateY(newHeight * 0.02);

        healthBar.setTranslateX(newWidth * 0.02);
        healthBar.setTranslateY(newHeight * 0.05);

        minimap.setTranslateX(newWidth - 210);
        minimap.setTranslateY(10);
    }
}
//