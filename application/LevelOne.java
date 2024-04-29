package application;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

//FOR OPTION
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.util.*;

public class LevelOne extends Application {

    private static final Random RAND = new Random();
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 680;
    private static final int PLAYER_SIZE = 60;
    private static final int MAX_BOMBS = 10;
    private static final int EXPLOSION_W = 128;
    private static final int EXPLOSION_ROWS = 3;
    private static final int EXPLOSION_COL = 3;
    private static final int EXPLOSION_H = 128;
    private static final int EXPLOSION_STEPS = 15;

    private static GraphicsContext gc;
    private Rocket player;
    private List<Shot> shots;
    private List<Universe> universes;
    private List<Bomb> bombs;
    private List<BossH> bossH;
    private List<Boss> boss;

    private boolean showPowerUpSelection = false; // Combined flag
    private double mouseX;
    private double mouseY;
    private int score;
    private int MAX_HITPOINTS = 1; // Changes L
    private int MAX_SHOTS = 15;
    private int DMG = 1; // Changes L
    private boolean gameOver = false;
    private boolean gameFinished = false;
    private boolean powerUpAvailable = false;
    private boolean powerUpChosen = false;
    private Timeline timeline;
    private boolean powerUpMenuTriggered = false;

    private List<String> redCirclePowerUps = new ArrayList<>(); // List to store power-ups from red circles

    static final Image PLAYER_IMG = new Image("file:src/images/player.png");
    static final Image EXPLOSION_IMG = new Image("file:src/images/explosion.png");
    static final Image[] BOMBS_IMG = {
            new Image("file:src/images/1.png"),
            new Image("file:src/images/2.png")
    };

    // Boss Sprites
    static final Image BOSS_H_IMG = new Image("file:src/images/head.png");
    static final Image BOSS_IMG = new Image("file:src/images/body.png");

    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(canvas);

        Button menuButton = new Button("Pause");
        menuButton.getStyleClass().add("pause-button");
        menuButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pauseGame();
                showMenuOptions(stage);
            }
        });

        HBox menuContainer = new HBox();
        menuContainer.getChildren().add(menuButton);
        menuContainer.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        menuContainer.setPadding(new javafx.geometry.Insets(10));
        StackPane.setAlignment(menuContainer, javafx.geometry.Pos.TOP_RIGHT);

        stackPane.getChildren().add(menuContainer);

        timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Scene scene = new Scene(stackPane, WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
        scene.setCursor(Cursor.MOVE);

        scene.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        scene.setOnMouseClicked(e -> {
            if (shots.size() < MAX_SHOTS)
                shots.add(player.shoot());
            if (gameOver && score >= 150) {
                gameOver = false;
                setup();
                score = 0;
            } else if (gameOver && score < 150) {
                gameOver = false;
                setup();
            }
            if (gameFinished) {
                gameFinished = false;
                LevelTwo leveltwo = new LevelTwo();
                try {
                    leveltwo.start(stage);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    pauseGame();
                    showMenuOptions(stage);
                    break;
                default:
                    break;
            }
        });

        stage.setScene(scene);
        stage.setTitle("The Void");
        stage.show();

        setup();
    }

    // Setup method
    private static final Duration TRIANGLE_SPAWN_INTERVAL = Duration.seconds(7); // Spawn triangle every 3 seconds
    private Timeline triangleSpawnTimeline;

    private void setup() {
        universes = new ArrayList<>();
        shots = new ArrayList<>();
        bombs = new ArrayList<>();
        boss = new ArrayList<>();
        bossH = new ArrayList<>();
        player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 140;
        // IntStream.range(0, MAX_BOMBS).mapToObj(i ->
        // this.newBomb()).forEach(Bombs::add);
        triangleSpawnTimeline = new Timeline(new KeyFrame(TRIANGLE_SPAWN_INTERVAL, e -> createTriangleFormation()));
        triangleSpawnTimeline.setCycleCount(Timeline.INDEFINITE);
        triangleSpawnTimeline.play();
        createTriangleFormation();
    }

    // Merge Sort
    private static void mergeSort(List<String> list, int left, int right) {
        if (left < right) {
            int middle = (left + right) / 2;

            // Sort first and second halves
            mergeSort(list, left, middle);
            mergeSort(list, middle + 1, right);

            // Merge the sorted halves
            merge(list, left, middle, right);
        }
    }

    private static void merge(List<String> list, int left, int middle, int right) {
        // Sizes of the two subarrays to be merged
        int n1 = middle - left + 1;
        int n2 = right - middle;

        // Create temporary arrays
        List<String> L = new ArrayList<>();
        List<String> R = new ArrayList<>();

        // Copy data to temporary arrays
        for (int i = 0; i < n1; ++i)
            L.add(list.get(left + i));
        for (int j = 0; j < n2; ++j)
            R.add(list.get(middle + 1 + j));

        // Merge the temporary arrays

        // Initial indexes of first and second subarrays
        int i = 0, j = 0;

        // Initial index of merged subarray
        int k = left;
        while (i < n1 && j < n2) {
            if (L.get(i).compareTo(R.get(j)) <= 0) {
                list.set(k, L.get(i));
                i++;
            } else {
                list.set(k, R.get(j));
                j++;
            }
            k++;
        }

        // Copy remaining elements of L[] if any
        while (i < n1) {
            list.set(k, L.get(i));
            i++;
            k++;
        }

        // Copy remaining elements of R[] if any
        while (j < n2) {
            list.set(k, R.get(j));
            j++;
            k++;
        }
    }
    // Merge Sort

    // Radix Sort
    private void radixSort(List<Bomb> bombs) {
        // Find the maximum hitpoints
        int maxHitpoints = 0;
        for (Bomb bomb : bombs) {
            if (bomb.hitpoints > maxHitpoints) {
                maxHitpoints = bomb.hitpoints;
            }
        }

        // Perform Radix Sort
        for (int exp = 1; maxHitpoints / exp > 0; exp *= 10) {
            countingSort(bombs, exp);
        }

        // Find the index of the bomb with the recently reduced hitpoints
        int indexOfReducedHitpointsBomb = bombs.size() - 1;
        while (indexOfReducedHitpointsBomb > 0 && bombs.get(indexOfReducedHitpointsBomb).hitpoints >= bombs
                .get(indexOfReducedHitpointsBomb - 1).hitpoints) {
            indexOfReducedHitpointsBomb--;
        }

        // Swap positions of the bomb with the highest hitpoints with the bomb whose
        // hitpoints were recently reduced
        if (indexOfReducedHitpointsBomb < bombs.size() - 1) {
            Bomb highestHitpointsBomb = bombs.remove(bombs.size() - 1);
            bombs.add(indexOfReducedHitpointsBomb, highestHitpointsBomb);
        }
    }

    private void countingSort(List<Bomb> bombs, int exp) {
        int n = bombs.size();

        // Initialize count array
        int[] count = new int[10];
        Arrays.fill(count, 0);

        // Store count of occurrences in count[]
        for (Bomb bomb : bombs) {
            count[(bomb.hitpoints / exp) % 10]++;
        }

        // Change count[i] so that count[i] now contains actual position of this digit
        // in output[]
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        Bomb[] output = new Bomb[n];
        for (int i = n - 1; i >= 0; i--) {
            output[count[(bombs.get(i).hitpoints / exp) % 10] - 1] = bombs.get(i);
            count[(bombs.get(i).hitpoints / exp) % 10]--;
        }

        // Copy the output array to bombs, updating positions
        for (int i = 0; i < n; i++) {
            bombs.set(i, output[i]);
        }
    }

    private void pauseGame() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void resumeGame() {
        if (timeline != null) {
            timeline.play();
        }
    }

    private void showMenuOptions(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setHeaderText("Game Paused");
        alert.setContentText("Choose an option:");

        ButtonType resumeButton = new ButtonType("Resume");
        ButtonType mainMenuButton = new ButtonType("Return to Level Selection");

        alert.getButtonTypes().setAll(resumeButton, mainMenuButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == resumeButton) {
                resumeGame(); // Resume the game
            } else if (result.get() == mainMenuButton) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LevelSelection.fxml"));
                    Parent parent = fxmlLoader.load();
                    Scene mainMenuScene = new Scene(parent);

                    stage.setScene(mainMenuScene);
                    stage.show();
                } catch (IOException ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Navigation Error");
                    errorAlert.setContentText("Unable to load LevelSelection.fxml");
                    errorAlert.showAndWait();
                }
            }
        }
    }

    private void run(GraphicsContext gc) {
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 60, 20);

        // Define colors for the boxes
        Color redColor = Color.RED;
        Color blueColor = Color.BLUE;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(15));
        gc.fillText("Red Circle Power-Ups:", 60, 40);

        int redColumnX = 50; // X coordinate for the red column
        int blueColumnX = 180; // X coordinate for the blue column
        int redYOffset = 60; // Y offset for the red column
        int blueYOffset = 60; // Y offset for the blue column

        mergeSort(redCirclePowerUps, 0, redCirclePowerUps.size() - 1);

        for (String powerUp : redCirclePowerUps) {
            // Check if the power-up string contains "STRONG"
            if (powerUp.contains("STRONG")) {
                // Draw red box in the left column
                gc.setFill(redColor);
                gc.fillRect(redColumnX, redYOffset - 12, 10, 10);

                // Draw power-up string next to the red box
                gc.setFill(Color.WHITE);
                gc.fillText(powerUp, redColumnX + 20, redYOffset);

                // Update the y-offset for the red column
                redYOffset += 20;
            }
            // Check if the power-up string contains "FASTER"
            else if (powerUp.contains("FASTER")) {
                // Draw blue box in the right column
                gc.setFill(blueColor);
                gc.fillRect(blueColumnX, blueYOffset - 12, 10, 10);

                // Draw power-up string next to the blue box
                gc.setFill(Color.WHITE);
                gc.fillText(powerUp, blueColumnX + 20, blueYOffset);

                // Update the y-offset for the blue column
                blueYOffset += 20;
            }
        }

        // Debug print
        // System.out.println("Current redCirclePowerUps size: " +
        // redCirclePowerUps.size());

        // Check if power-up is available and handle power-up selection
        if (score > 0 && score % 35 == 0 && !showPowerUpSelection && score < 152) {
            // Show level 35 power-up options
            showLevelPowerUpOptions();
            score++;
        }

        bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
            e.drawRedCircle(gc); // Draw red circle if exists
            e.updateRedCircle(); // Update red circle position

            // Check collision with red circle and handle power-up selection if the flag is
            // false
            if (!powerUpMenuTriggered && e.getRedCircle() != null && e.getRedCircle().isActive()
                    && e.getRedCircle().collide(player)) {
                e.getRedCircle().deactivate(); // Deactivate red circle
                // Show red circle power-up options
                showRedCirclePowerUpOptions();
                powerUpMenuTriggered = true; // Set the flag to true
            }
        });

        // all boss defeated and boss spawn
        boolean allBossesDefeated = boss.isEmpty() && bossH.isEmpty();

        if (score >= 150 && score < 151 && allBossesDefeated) {
            createBossFormation();
            score++;

        }
        if (score >= 150) {

            Font font = Font.font("Palatino Linotype", 25);
            gc.setFill(Color.web("#200000"));
            gc.fillRect(0, 0, WIDTH, HEIGHT);
            gc.setFont(font);
            gc.setFill(Color.RED);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Oroboros, the All Devouring", WIDTH / 2, 35);
        }

        if (score > 850) {
            // Stop the game
            timeline.stop();

            // Display "LEVEL CLEARED" screen
            Font font = Font.font("Palatino Linotype", 35);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);
            gc.setFont(font);
            gc.setFill(Color.web("#FFFF6E"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(" GUARDIAN SLAIN ", WIDTH / 2, HEIGHT / 2.5);
            Font font1 = Font.font("Palatino Linotype", 15);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, 0);
            gc.setFont(font1);
            gc.setFill(Color.web("#FFFF6E"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("\n\n\nYou feel the anger of the Gods quake the universe", WIDTH / 2, HEIGHT / 2.5);

            gameFinished = true;
        }

        // Resume the game if power-up selection is done
        if (powerUpChosen) {
            timeline.play();
        }

        // DRAW UNIVERSE
        universes.forEach(Universe::draw);

        // Increase hp of enemy for every 50 points , Max hp is 3 // Changes L
        if (score < 150 && score % 50 == 0) {
            // Increase the maximum hitpoints for bombs
            MAX_HITPOINTS++;
            score++;
        }

        // Update and draw player
        player.update();
        player.draw();

        // Update player's position based on the mouse coordinates
        player.posX = (int) (mouseX - player.size / 2);
        player.posY = (int) (mouseY - player.size / 2);

        // Keep the player within the screen bounds
        if (player.posX < 0)
            player.posX = 0;
        if (player.posX > WIDTH - PLAYER_SIZE)
            player.posX = WIDTH - PLAYER_SIZE;
        if (player.posY < 0)
            player.posY = 0;
        if (player.posY > HEIGHT - PLAYER_SIZE)
            player.posY = HEIGHT - PLAYER_SIZE;

        // Update and draw bombs
        bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
            e.drawRedCircle(gc); // Draw red circle if exists
            e.updateRedCircle(); // Update red circle position

            // Check collision with red circle and handle power-up selection
            if (e.getRedCircle() != null && e.getRedCircle().isActive() && e.getRedCircle().collide(player)) {
                e.getRedCircle().deactivate(); // Deactivate red circle
                showPowerUpOptions(); // Trigger power-up selection
            }
        });

        // Check collision with red circle and handle power-up selection
        for (int i = bombs.size() - 1; i >= 0; i--) {
            Bomb bomb = bombs.get(i);
            RedCircle redCircle = bomb.getRedCircle();
            if (redCircle != null && redCircle.collide(player)) {
                redCircle.deactivate(); // Deactivate red circle
                showPowerUpOptions(); // Trigger power-up selection
            }
        }

        // Update and draw boss
        boss.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });
        // Update and draw boss head
        bossH.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });
        // Boss movements
        for (BossH bossH : bossH) {
            // Calculate the offset between the player and the boss
            int offsetX = (player.posX - (bossH.posX + 180)) / 20;

            // Update boss position based on the player's position
            bossH.posX += offsetX;
        }
        for (Boss boss : boss) {
            // Calculate the offset between the player and the boss
            int offsetX = (player.posX - (boss.posX + 190)) / 22;

            // Update boss position based on the player's position
            boss.posX += offsetX;
        }
        // Update and draw shots // Changes L
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            if (shot.posY < 0 || shot.toRemove) {
                shots.remove(i);
                continue;
            }
            shot.update();
            shot.draw();
            for (Bomb bomb : bombs) {
                if (shot.collide(bomb) && !bomb.exploding) {
                    bomb.hit(); // Decrease bomb's hitpoints
                    shot.toRemove = true;
                }
            }

            for (Boss boss : boss) {
                if (shot.collide(boss) && !boss.exploding) {
                    boss.hit(); // Decrease bomb's hitpoints
                    shot.toRemove = true;
                }
            }
            for (BossH bossH : bossH) {
                if (shot.collide(bossH) && !bossH.exploding) {
                    bossH.hit(); // Decrease bomb's hitpoints
                    shot.toRemove = true;
                }
            }
        }

        // Check if the game is over
        gameOver = player.destroyed;

        if (gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over\nYour Score is: " + score + "\nClick to play again", WIDTH / 2, HEIGHT / 2.5);
        }

        // Add new universes
        if (RAND.nextInt(10) > 2) {
            universes.add(new Universe());
        }

        // Remove universes that have gone past the screen
        universes.removeIf(universe -> universe.posY > HEIGHT);
    }

    private Bomb newBomb() {
        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]);
    }

    // boss
    private BossH newBossH() {
        return new BossH(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, BOSS_H_IMG);
    }

    private Boss newBoss() {
        return new Boss(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, BOSS_IMG);
    }

    private static int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    private void showLevelPowerUpOptions() {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Choose Power-Up");
            dialog.setHeaderText("Select a power-up:");

            // Create buttons for different power-up options
            ButtonType shieldButton = new ButtonType("Shield");
            ButtonType fasterShipButton = new ButtonType("Faster Ship");

            // Add buttons to the dialog
            dialog.getDialogPane().getButtonTypes().addAll(shieldButton, fasterShipButton);

            // Handle button actions
            dialog.setOnCloseRequest(event -> {
                // If the dialog is closed without choosing a power-up, resume the game
                timeline.play();
            });

            // Show the dialog and wait for the user to choose a power-up
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == shieldButton) {
                    // Apply shield power-up effect
                    // Implement shield power-up effect here
                } else if (buttonType == fasterShipButton) {
                    // Apply faster ship power-up effect
                    // Implement faster ship power-up effect here
                }
                // Reset the flag after applying the power-up effect:
                showPowerUpSelection = false;
            });
        });
    }

    private void showRedCirclePowerUpOptions() {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Choose Red Circle Power-Up");
            dialog.setHeaderText("Select a power-up:");

            // Create buttons for different power-up options
            ButtonType fasterBulletButton = new ButtonType("Faster Bullet");
            ButtonType strongerBulletButton = new ButtonType("Stronger Bullet");

            // Add buttons to the dialog
            dialog.getDialogPane().getButtonTypes().addAll(fasterBulletButton, strongerBulletButton);

            // Show the dialog and wait for the user to choose a power-up
            dialog.showAndWait().ifPresent(buttonType -> {
                // Add selected power-up to the list if the limit is not reached
                if (buttonType == fasterBulletButton) {
                    redCirclePowerUps.add("FASTER");
                    System.out.println("Faster Bullet power-up added.");
                } else if (buttonType == strongerBulletButton) {
                    redCirclePowerUps.add("STRONGER");
                    System.out.println("Stronger Bullet power-up added.");
                } else {
                    System.out.println("Cannot add more than 6 power-ups from red circles.");
                }

                printRedCirclePowerUps(); // Print the list of power-ups after adding a new one

            });
        });
    }

    private void printRedCirclePowerUps() {
        System.out.println("Red Circle Power-Ups:");

    }

    private void showPowerUpOptions() {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Choose Power-Up");
            dialog.setHeaderText("Select a power-up:");

            // Create buttons for different power-up options
            ButtonType biggerBulletButton = new ButtonType("Stronger Bullet");
            ButtonType fasterBulletButton = new ButtonType("Faster Bullet");

            // Add buttons to the dialog
            dialog.getDialogPane().getButtonTypes().addAll(biggerBulletButton, fasterBulletButton);

            // Handle button actions
            dialog.setOnCloseRequest(event -> {
                // If the dialog is closed without choosing a power-up, resume the game
                timeline.play();
            });

            // Show the dialog and wait for the user to choose a power-up
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == biggerBulletButton) {
                    Shot.size *= 1.2;
                    DMG++; // Changes L
                    redCirclePowerUps.add("STRONGER");
                    System.out.println("STRONGER Bullet power-up added.");
                } else if (buttonType == fasterBulletButton) {
                    // Apply the chosen power-up effect for faster bullet
                    Shot.speed *= 1.2;
                    MAX_SHOTS += 2;
                    redCirclePowerUps.add("FASTER");
                    System.out.println("Faster Bullet power-up added.");
                }
                // Reset the flag after applying the power-up effect:
                showPowerUpSelection = false;
            });
        });
    }

    public class RedCirclePowerUpSorter {
        private List<String> redCirclePowerUps;

        public RedCirclePowerUpSorter(List<String> redCirclePowerUps) {
            this.redCirclePowerUps = redCirclePowerUps;
        }

        public void mergeSort() {
            mergeSort(0, redCirclePowerUps.size() - 1);
        }

        private void mergeSort(int left, int right) {
            if (left < right) {
                int middle = (left + right) / 2;
                mergeSort(left, middle);
                mergeSort(middle + 1, right);
                merge(left, middle, right);
            }
        }

        private void merge(int left, int middle, int right) {
            List<String> temp = new ArrayList<>();
            int i = left;
            int j = middle + 1;

            while (i <= middle && j <= right) {
                if (redCirclePowerUps.get(i).compareTo(redCirclePowerUps.get(j)) < 0) {
                    temp.add(redCirclePowerUps.get(i++));
                } else {
                    temp.add(redCirclePowerUps.get(j++));
                }
            }

            while (i <= middle) {
                temp.add(redCirclePowerUps.get(i++));
            }

            while (j <= right) {
                temp.add(redCirclePowerUps.get(j++));
            }

            for (int k = left; k <= right; k++) {
                redCirclePowerUps.set(k, temp.get(k - left));
            }
        }

        public List<String> getSortedPowerUps() {
            return redCirclePowerUps;
        }
    }

    public static void main(String[] args) {
        launch();
    }

    public class Rocket {
        int posX, posY, size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep = 0;

        public Rocket(int posX, int posY, int size, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shot shoot() {
            // Adjust the starting position of the bullet by subtracting half of the
            // bullet's size
            int bulletX = posX + size / 2 - Shot.size / 2;
            // Create a new Shot object
            return new Shot(bulletX, posY - Shot.size);
        }

        public void update() {
            if (exploding)
                explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;
        }

        public void draw() {
            if (exploding) {
                gc.drawImage(EXPLOSION_IMG, explosionStep % EXPLOSION_COL * EXPLOSION_W,
                        (explosionStep / EXPLOSION_ROWS) * EXPLOSION_H + 1,
                        EXPLOSION_W, EXPLOSION_H,
                        posX, posY, size, size);
            } else {
                gc.drawImage(img, posX, posY, size, size);
            }
        }

        public boolean collide(Rocket other) {
            int d = distance(this.posX + size / 2, this.posY + size / 2,
                    other.posX + other.size / 2, other.posY + other.size / 2);
            return d < other.size / 2 + this.size / 2;
        }

        public void explode() {
            exploding = true;
            explosionStep = -1;
        }
    }

    // RED CIRCLE POWERUPS KO
    public class RedCircle {
        private int posX, posY;
        private static final int SIZE = 20;
        private boolean active;
        private boolean activated; // New flag to track activation status

        public RedCircle(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            this.active = true;
            this.activated = false; // Initialize to false
        }

        public void draw(GraphicsContext gc) {
            if (active) {
                gc.setFill(Color.RED);
                gc.fillOval(posX, posY, SIZE, SIZE);
            }
        }

        public boolean collide(Rocket player) {
            if (active && !activated) { // Check if the red circle is active and not already activated
                double distance = Math.sqrt(Math.pow((player.posX + player.size / 2) - (posX + SIZE / 2), 2)
                        + Math.pow((player.posY + player.size / 2) - (posY + SIZE / 2), 2));
                if (distance < player.size / 2 + SIZE / 2) {
                    activated = true; // Set the activated flag
                    active = false; // Deactivate the red circle
                    return true;
                }
            }
            return false;
        }

        public void deactivate() {
            active = false;
        }

        // Getters
        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }

        public boolean isActive() {
            return active;
        }
    }

    public class Bomb extends Rocket {
        int hitpoints;
        int SPEED = (score / 10) + 1;

        private RedCircle redCircle;
        private static final double POWER_UP_DROP_PROBABILITY = 0.7; // 20% probability

        public Bomb(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = MAX_HITPOINTS;
            redCircle = null;
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
            if (posY > HEIGHT)
                destroyed = true;
        }

        public void hit() {
            hitpoints -= DMG;
            if (hitpoints <= 0) {
                explode();
                score++;

                // Drop power-up based on probability
                if (Math.random() <= POWER_UP_DROP_PROBABILITY) {
                    // Drop power-up (e.g., create red circle)
                    if (redCircle == null) {
                        redCircle = new RedCircle(this.posX, this.posY);
                    }
                }
            }
        }

        public void drawRedCircle(GraphicsContext gc) {
            if (redCircle != null) {
                redCircle.draw(gc);
            }
        }

        public void updateRedCircle() {
            if (redCircle != null) {
                redCircle.posY += 5; // Adjust speed as needed
                if (redCircle.posY > HEIGHT) {
                    redCircle = null; // Remove red circle if it goes beyond the screen
                }
            }
        }

        public RedCircle getRedCircle() {
            return redCircle;
        }
    }

    public class BossH extends Rocket {
        int hitpoints;
        int SPEED = 3;

        public BossH(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = 200 + (10 * MAX_HITPOINTS);

        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
        }

        public void hit() {
            hitpoints -= DMG;
            if (hitpoints <= 0) {
                explode();
                score += 100;
            }
        }
    }

    public class Boss extends Rocket {
        int hitpoints;
        int SPEED = 3;

        public Boss(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = 180 + (10 * MAX_HITPOINTS);
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
        }

        public void hit() {
            hitpoints -= DMG;
            if (hitpoints <= 0) {
                explode();
                score += 50;
            }
        }

        public void collapse() {
            hitpoints -= 30;
            if (hitpoints <= 0) {
                explode();
                score += 50;
            }
        }
    }

    // Shot class
    public static class Shot {

        public boolean toRemove;
        int posX, posY;
        static int size = 6;
        static int speed = 10; // Add speed variable

        private Image bulletImage;

        public Shot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            bulletImage = new Image("file:src/images/bullets.png");
        }

        public void update() {
            posY -= speed; // Update the position based on speed
        }

        public void draw() {
            if (bulletImage != null) {
                gc.drawImage(bulletImage, posX, posY, size * 0.2, size * 5);
            }
        }

        public boolean collide(Rocket rocket) {
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    rocket.posX + rocket.size / 2, rocket.posY + rocket.size / 2);
            return distance < rocket.size / 2 + size / 2;
        }
    }

    // MERGE SORTING!!!!!!!!!!
    // MERGE SORTING!!!!!!!!!!
    public class MergeSort {
        private List<String> redCirclePowerUps = new ArrayList<>(); // List to store power-ups from red circles

        // Method to sort the redCirclePowerUps list using merge sort
        private void mergeSortRedCirclePowerUps(List<String> list) {
            if (list.size() <= 1) {
                return;
            }
            int mid = list.size() / 2;
            List<String> left = new ArrayList<>(list.subList(0, mid));
            List<String> right = new ArrayList<>(list.subList(mid, list.size()));

            mergeSortRedCirclePowerUps(left);
            mergeSortRedCirclePowerUps(right);

            merge(list, left, right);
        }

        // Method to merge two lists in sorted order
        private void merge(List<String> list, List<String> left, List<String> right) {
            int leftIndex = 0, rightIndex = 0, listIndex = 0;

            while (leftIndex < left.size() && rightIndex < right.size()) {
                if (left.get(leftIndex).compareTo(right.get(rightIndex)) < 0) {
                    list.set(listIndex++, left.get(leftIndex++));
                } else {
                    list.set(listIndex++, right.get(rightIndex++));
                }
            }

            while (leftIndex < left.size()) {
                list.set(listIndex++, left.get(leftIndex++));
            }

            while (rightIndex < right.size()) {
                list.set(listIndex++, right.get(rightIndex++));
            }
        }

        // Method to display the sorted redCirclePowerUps list
        private void displaySortedRedCirclePowerUps() {
            System.out.println("Sorted redCirclePowerUps:");
            for (String powerUp : redCirclePowerUps) {
                System.out.println(powerUp);
            }
        }
    }
    // MERGE SORTING!!!!!!!!!!
    // MERGE SORTING!!!!!!!!!!

    public class Universe {
        int posX, posY;
        private int h, w, r, g, b;
        private double opacity;

        public Universe() {
            posX = RAND.nextInt(WIDTH);
            posY = 0;
            w = RAND.nextInt(5) + 1;
            h = RAND.nextInt(5) + 1;
            r = RAND.nextInt(100) + 150;
            g = RAND.nextInt(100) + 150;
            b = RAND.nextInt(100) + 150;
            opacity = RAND.nextFloat();
            if (opacity < 0)
                opacity *= -1;
            if (opacity > 0.5)
                opacity = 0.5;
        }

        public void draw() {
            if (opacity > 0.8)
                opacity -= 0.01;
            if (opacity < 0.1)
                opacity += 0.01;
            gc.setFill(Color.rgb(r, g, b, opacity));
            gc.fillOval(posX, posY, w, h);
            posY += 20;
        }
    }

    private static final int TRIANGLE_ROWS = 3;
    private static final int ENEMY_SIZE = 50;
    private static final int ENEMY_GAP = 30;
    private int startX = 100;

    // Method to create triangle formation of enemies
    private void createTriangleFormation() {
        if (score < 150 || score >= 160) {
            int currentY = -210; // Starting Y position of the triangle formation

            // Calculate a random starting X position within the visible area of the screen
            int startX = RAND.nextInt(WIDTH - (TRIANGLE_ROWS * (ENEMY_SIZE + ENEMY_GAP)));

            for (int row = 0; row < TRIANGLE_ROWS; row++) {
                int enemiesInRow = TRIANGLE_ROWS - row; // Number of enemies in the current row

                // Calculate starting X position for the current row to center it
                startX += (row == 0) ? 0 : (ENEMY_SIZE + ENEMY_GAP) / 2; // Offset for subsequent rows
                for (int i = 0; i < enemiesInRow; i++) {
                    int posX = startX + i * (ENEMY_SIZE + ENEMY_GAP);
                    bombs.add(new Bomb(posX, currentY, ENEMY_SIZE, BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]));
                }

                // Move to the next row
                currentY += ENEMY_SIZE + ENEMY_GAP;
            }
        }
    }

    // boss formation
    private void createBossFormation() {
        int currentY = (-500 * 11) - 400; // Starting Y position of the triangle formation

        // Calculate a random starting X position within the visible area of the screen

        // spawn head
        int posX = (WIDTH - 180) / 2;

        for (int row = 0; row < 12; row++) {
            int enemiesInRow = 1; // Number of enemies in the current row
            currentY += 400;
            boss.add(new Boss(posX, currentY, 500, BOSS_IMG));
        }

        // Add boss head (BOSS_H_IMG)
        currentY += 410; // Move to the next row
        bossH.add(new BossH(posX + 10, currentY, 480, BOSS_H_IMG));

    }
}
