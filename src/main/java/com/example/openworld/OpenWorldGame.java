package com.example.openworld;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenWorldGame extends Application {

    // Увеличенный размер мира
    private static final int WORLD_WIDTH = 4000;  // Было 2000
    private static final int WORLD_HEIGHT = 3000; // Было 1500

    private static final int PLAYER_SIZE = 40;
    private static final int PLAYER_SPEED = 5;

    private double playerX = WORLD_WIDTH / 2.0;
    private double playerY = WORLD_HEIGHT / 2.0;

    private double camX = 0;
    private double camY = 0;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private double windowWidth;
    private double windowHeight;

    private final List<GameObject> objects = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        var screenBounds = Screen.getPrimary().getVisualBounds();
        windowWidth = screenBounds.getWidth();
        windowHeight = screenBounds.getHeight();

        Canvas canvas = new Canvas(windowWidth, windowHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, windowWidth, windowHeight);

        scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            windowWidth = newVal.doubleValue();
            canvas.setWidth(windowWidth);
        });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            windowHeight = newVal.doubleValue();
            canvas.setHeight(windowHeight);
        });

        // Добавляем больше объектов, чтобы заполнить увеличенный мир
        objects.add(new GameObject(new double[]{
                0, 0,
                30, 0,
                30, 60,
                0, 60
        }, 500, 400, Color.DARKGREEN));

        objects.add(new GameObject(new double[]{
                0, 0,
                40, 0,
                40, 50,
                0, 50
        }, 800, 700, Color.DARKGREEN));

        objects.add(new GameObject(new double[]{
                0, 0,
                50, 0,
                50, 80,
                0, 80
        }, 1200, 300, Color.DARKGREEN));

        objects.add(new GameObject(new double[]{
                0, 0,
                60, 0,
                60, 40,
                0, 40
        }, 1600, 900, Color.DARKGREEN));

        objects.add(new GameObject(new double[]{
                0, 0,
                30, 0,
                30, 70,
                0, 70
        }, 400, 1100, Color.DARKGREEN));

        // Новые объекты в расширенном мире
        objects.add(new GameObject(new double[]{
                0, 0,
                50, 0,
                50, 90,
                0, 90
        }, 2500, 1500, Color.DARKGREEN));

        objects.add(new GameObject(new double[]{
                0, 0,
                70, 0,
                70, 60,
                0, 60
        }, 3000, 2000, Color.DARKGREEN));

        objects.add(new GameObject(new double[]{
                0, 0,
                40, 0,
                40, 80,
                0, 80
        }, 3500, 2500, Color.DARKGREEN));

        objects.add(new GameObject(new double[]{
                0, 0,
                60, 0,
                60, 70,
                0, 70
        }, 3800, 2800, Color.DARKGREEN));

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        }.start();

        primaryStage.setTitle("JavaFX Open World Game — Увеличенный мир");
        primaryStage.setScene(scene);
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(windowWidth);
        primaryStage.setHeight(windowHeight);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        canvas.requestFocus();
    }

    private void update() {
        double nextX = playerX;
        double nextY = playerY;

        if (pressedKeys.contains(KeyCode.W)) {
            nextY -= PLAYER_SPEED;
        }
        if (pressedKeys.contains(KeyCode.S)) {
            nextY += PLAYER_SPEED;
        }
        if (pressedKeys.contains(KeyCode.A)) {
            nextX -= PLAYER_SPEED;
        }
        if (pressedKeys.contains(KeyCode.D)) {
            nextX += PLAYER_SPEED;
        }

        if (!isColliding(nextX, nextY)) {
            if (nextX >= 0 && nextX <= WORLD_WIDTH - PLAYER_SIZE &&
                    nextY >= 0 && nextY <= WORLD_HEIGHT - PLAYER_SIZE) {
                playerX = nextX;
                playerY = nextY;
            }
        }

        double targetCamX = playerX - windowWidth / 2.0;
        double targetCamY = playerY - windowHeight / 2.0;

        targetCamX = clamp(targetCamX, 0, WORLD_WIDTH - windowWidth);
        targetCamY = clamp(targetCamY, 0, WORLD_HEIGHT - windowHeight);

        double lerpFactor = 0.1;
        camX += (targetCamX - camX) * lerpFactor;
        camY += (targetCamY - camY) * lerpFactor;
    }

    private boolean isColliding(double x, double y) {
        Polygon playerPoly = createPlayerPolygon(x, y);

        for (GameObject obj : objects) {
            Shape intersection = Shape.intersect(playerPoly, obj.getPolygon());
            if (intersection.getBoundsInLocal().getWidth() != -1) {
                return true;
            }
        }
        return false;
    }

    private Polygon createPlayerPolygon(double x, double y) {
        return new Polygon(
                x, y,
                x + PLAYER_SIZE, y,
                x + PLAYER_SIZE, y + PLAYER_SIZE,
                x, y + PLAYER_SIZE
        );
    }

    private void render(GraphicsContext gc) {
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(0, 0, windowWidth, windowHeight);

        double scaleX = windowWidth / WORLD_WIDTH;
        double scaleY = windowHeight / WORLD_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        gc.save();
        gc.scale(scale, scale);

        gc.setStroke(Color.GRAY);
        for (int x = 0; x < WORLD_WIDTH; x += 100) {
            gc.strokeLine(x - camX, 0 - camY, x - camX, WORLD_HEIGHT - camY);
        }
        for (int y = 0; y < WORLD_HEIGHT; y += 100) {
            gc.strokeLine(0 - camX, y - camY, WORLD_WIDTH - camX, y - camY);
        }

        for (GameObject obj : objects) {
            obj.render(gc, camX, camY);
        }

        gc.setFill(Color.BLUE);
        gc.fillRect(playerX - camX, playerY - camY, PLAYER_SIZE, PLAYER_SIZE);

        gc.restore();
    }

    private double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    private static class GameObject {
        private final Polygon polygon;
        private final Color color;
        private final double offsetX;
        private final double offsetY;

        public GameObject(double[] points, double offsetX, double offsetY, Color color) {
            this.polygon = new Polygon(points);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.color = color;
            this.polygon.setTranslateX(offsetX);
            this.polygon.setTranslateY(offsetY);
        }

        public Polygon getPolygon() {
            return polygon;
        }

        public void render(GraphicsContext gc, double camX, double camY) {
            gc.setFill(color);

            double[] points = polygon.getPoints().stream().mapToDouble(Double::doubleValue).toArray();

            double tx = offsetX - camX;
            double ty = offsetY - camY;

            double[] xPoints = new double[points.length / 2];
            double[] yPoints = new double[points.length / 2];
            for (int i = 0, j = 0; i < points.length; i += 2, j++) {
                xPoints[j] = points[i] + tx;
                yPoints[j] = points[i + 1] + ty;
            }

            gc.fillPolygon(xPoints, yPoints, xPoints.length);
            gc.setStroke(Color.BLACK);
            gc.strokePolygon(xPoints, yPoints, xPoints.length);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
