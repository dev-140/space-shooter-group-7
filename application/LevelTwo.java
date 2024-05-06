package application;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

//FOR OPTION
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.Iterator;

import application.LevelOne.Bomb;
import application.LevelOne.RedCircle;
import application.LevelOne.Rocket;
//import application.SpaceShooter.Bomb;
//import application.SpaceShooter.Shot;

public class LevelTwo extends Application {

    private static final Random RAND = new Random();
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 680;
    private static final int PLAYER_SIZE = 60;
    private static final int MAX_BOMBS = 4;
    private static final int EXPLOSION_W = 128;
    private static final int EXPLOSION_ROWS = 3;
    private static final int EXPLOSION_COL = 3;
    private static final int EXPLOSION_H = 128;
    private int SPLIT = 1;
    private int Shield = 0;
    private int currentpu = 0;
    private static final int EXPLOSION_STEPS = 15;

    private static GraphicsContext gc;
    private Rocket player;
    private List<Shot> shots;
    private List<EnemyShot> enemyshots;
    private List<Universe> universes;
    private List<Bomb> bombs;
    private List<Boss> boss;
    private List<Wheel> wheel;
    private List<Tower> tower;
    private List<EnemyShooter> enemyshooter;

    private boolean showPowerUpSelection = false; // Combined flag
    private double mouseX;
    private double mouseY;
    private int score;
    private int MAX_HITPOINTS = 1; // Changes L
    private int MAX_SHOTS = 15;
    private int DMG = 1; // Changes L
    private boolean gameOver = false;
    private boolean powerUpAvailable = false;
    private boolean powerUpChosen = false;
    private Timeline timeline;
    private boolean frozen = false;
    private boolean warn = false;
    private int towerSpawnCounter = 0;
    private boolean gameFinished = false;
    private boolean powerUpMenuTriggered = false;

    private List<String> redCirclePowerUps = new ArrayList<>(); // List to store power-ups from red circles

    static final Image BACKGROUND_GIF = new Image("file:src/images/bg.gif");
    static final Image WARN_BACKGROUND_IMG = new Image("file:src/images/WARN.png");
    static final Image FROZEN_BACKGROUND_IMG = new Image("file:src/images/frozen.png");
    static final Image PLAYER_IMG = new Image("file:src/images/player.png");
    static final Image EXPLOSION_IMG = new Image("file:src/images/explosion.png");
    static final Image[] BOMBS_IMG = {
            new Image("file:src/images/3.png"),
            new Image("file:src/images/4.png"),
            new Image("file:src/images/2.png")
    };

    // Boss sprites
    static final Image BOSS_IMG = new Image("file:src/images/Naia.gif");
    static final Image PROPS_IMG = new Image("file:src/images/wheel.gif");

    // Enemy Bullet sprite
    static final Image[] BULLET_IMG = {
            new Image("file:src/images/enemybullets.png"),
            new Image("file:src/images/homingbullets.png"),
    };

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
        	if(!frozen) {
            mouseX = e.getX();
            mouseY = e.getY();
        	}
        });
        

        scene.setOnMouseClicked(e -> {
        	if (shots.size() < MAX_SHOTS && SPLIT >= 3 && !gameOver && !frozen) {
        		shots.add(player.shoot());
        		shots.add(new Shot(player.posX, player.posY - Shot.size));
        		shots.add(new Shot(player.posX + player.size, player.posY - Shot.size));
        	}else if (shots.size() < MAX_SHOTS && SPLIT >= 2 && !gameOver && !frozen) {
        		shots.add(new Shot(player.posX + 15, player.posY - Shot.size ));
        		shots.add(new Shot(player.posX + player.size - 15, player.posY - Shot.size));
        	}else if (shots.size() < MAX_SHOTS && !frozen)
                shots.add(player.shoot());
            if (gameOver && score >= 150) {
                gameOver = false;
                setup();
                score = 150;
            } else if (gameOver && score < 150) {
                gameOver = false;
                setup();
            }
            if (gameFinished) {
                gameFinished = false;
                LevelThree levelthree = new LevelThree();
                try {
                    levelthree.start(stage);
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
        stage.setTitle("Eternal Garden");
        stage.show();

        setup();
    }

    // Setup method
    private static final Duration TRIANGLE_SPAWN_INTERVAL = Duration.seconds(7); // Spawn triangle every 3 seconds
    private Timeline triangleSpawnTimeline;

    private void setup() {
        universes = new ArrayList<>();
        shots = new ArrayList<>();
        enemyshots = new ArrayList<>();
        bombs = new ArrayList<>();
        boss = new ArrayList<>();
        wheel = new ArrayList<>();
        tower = new ArrayList<>();
        enemyshooter = new ArrayList<>();
        player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        // IntStream.range(0, MAX_BOMBS).mapToObj(i ->
        // this.newBomb()).forEach(Bombs::add);
        triangleSpawnTimeline = new Timeline(new KeyFrame(TRIANGLE_SPAWN_INTERVAL, e -> createTriangleFormation()));
        triangleSpawnTimeline.setCycleCount(Timeline.INDEFINITE);
        triangleSpawnTimeline.play();
        createTriangleFormation();
    }

    // MERGE SORT
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
    // MERGE SORT

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
        if (frozen) {
            gc.drawImage(FROZEN_BACKGROUND_IMG, 0, 0, WIDTH, HEIGHT);
        } else if (warn) {
            gc.drawImage(WARN_BACKGROUND_IMG, 0, 0, WIDTH, HEIGHT);
        } else {
            gc.drawImage(BACKGROUND_GIF, 0, 0, WIDTH, HEIGHT);
        }
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 60, 20);

        // Define colors for the boxes
        Color redColor = Color.RED;
        Color blueColor = Color.BLUE;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(15));
        gc.fillText("       Red Circle Power-Ups:", 60, 40);

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
        System.out.println("Current score: " + score);

        // Check if power-up is available and handle power-up selection
        if (score > 0 && score % 35 == 0 && !showPowerUpSelection && score < 152) {
            showPowerUpSelection = true; // Set flag when score reaches a multiple of 20
            // Pause the game
            timeline.stop();
            // Prompt the player to choose a power-up
            showLevelPowerUpOptions();
            score++;

        }

        // clocktower something

        // Inside the run method or any appropriate update method
        // Increment the counter
        towerSpawnCounter++;
        if (score < 150 && towerSpawnCounter >= 200) {
            towerSpawnCounter = 0;
            tower.add(new Tower(70, -400, 300, new Image("file:src/images/clocktower.gif")));
            tower.add(new Tower(900, -400, 300, new Image("file:src/images/clocktower.gif")));
        }

        for (Tower tower : tower) {
            tower.update();
            tower.draw();
        }

        for (Tower tower : tower) {
            if (player.collide(tower) && !player.exploding) {
                frozen = true;
                // If the player is colliding with a tower, you might want to break out of the
                // loop
                // to prevent setting 'frozen' to false if the player is colliding with multiple
                // towers
                break;
            } else {
                frozen = false;
            }
        }

        // Timestop for boss
        if (score > 150 && score < 850 && towerSpawnCounter >= 230) {
            // warning
            warn = true;
        }
        if (score > 150 && score < 850 && towerSpawnCounter >= 250) {
            warn = false;
            frozen = true;
            for (Shot shot : shots) {
                shot.collidingWithWheel = true;
            }
            if (towerSpawnCounter >= 280) {
                towerSpawnCounter = 0;
                frozen = false;
                for (Shot shot : shots) {
                    shot.collidingWithWheel = false;
                }
            }
        }

        // Homingshots enemies spawn utilizing max_bombs

        Iterator<EnemyShooter> iterator = enemyshooter.iterator();
        while (iterator.hasNext()) {
            EnemyShooter shooter = iterator.next();
            if (shooter.isDestroyed()) {
                iterator.remove(); // Remove destroyed enemy shooters
            }
        }

        if (enemyshooter.size() < 2 && score > 100 && score < 150) {
            int spawnChance = RAND.nextInt(500);
            int X = RAND.nextInt(WIDTH - 100);
            if (spawnChance < 10) {
                enemyshooter.add(new EnemyShooter(X, -150, 100, BOMBS_IMG[0]));// Adjust the spawn chance as needed
            }
        }

        if (enemyshooter.size() < 5 && score > 150 && score < 850) {
            int spawnChance = RAND.nextInt(400);
            if (spawnChance < 10 && enemyshooter.size() < 1) { // Adjust the spawn chance as needed
                enemyshooter.add(new EnemyShooter(300, -150, 100, BOMBS_IMG[0]));
                enemyshooter.add(new EnemyShooter(WIDTH - 400, -150, 100, BOMBS_IMG[0]));
                enemyshooter.add(new EnemyShooter(100, -50, 100, BOMBS_IMG[0]));
                enemyshooter.add(new EnemyShooter(WIDTH - 200, -50, 100, BOMBS_IMG[0]));
            }
        }

        // all boss defeated and boss spawn

        if (score >= 150 && boss.size() < 1 && wheel.size() < 1) {
            for (Bomb bomb : bombs) {
                bomb.explode();
            }
            createBossFormation();
            score++;

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
            gc.fillText("\n\n\nThe path to Heaven's gate has cleared", WIDTH / 2, HEIGHT / 2.5);

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
        if (!frozen) {
            player.posX = (int) (mouseX - player.size / 2); // center the player
        }

        // Update and draw bombs
        bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
        	if (player.collide(e) && !player.exploding ) {
            	if(player.collide(e) && !player.exploding && Shield >= 1) {
            		Shield--;
            	}
            	if(player.collide(e) && !player.exploding && Shield <= 0) {
                		player.explode();
            	}
            }
            e.drawRedCircle(gc); // Draw red circle if exists
            e.updateRedCircle(); // Update red circle position

            // Check collision with red circle and handle power-up selection
            if (e.getRedCircle() != null && e.getRedCircle().isActive() && e.getRedCircle().collide(player)) {
                e.getRedCircle().deactivate(); // Deactivate red circle
                timeline.stop();
                showPowerUpOptions(); // Trigger power-up selection
            }
        });

        // Check collision with red circle and handle power-up selection
        for (int i = bombs.size() - 1; i >= 0; i--) {
            Bomb bomb = bombs.get(i);
            RedCircle redCircle = bomb.getRedCircle();
            if (redCircle != null && redCircle.collide(player)) {
            	 timeline.stop();
                redCircle.deactivate(); // Deactivate red circle
                showPowerUpOptions(); // Trigger power-up selection
            }
        }

        // Update and draw boss
        wheel.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });

        boss.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });

        // enemyshooter class
        enemyshooter.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });

        // shooter enemy
        bombs.forEach(bomb -> {
            bomb.update();
            bomb.draw();

            // Check if it's time for the bomb to shoot
            if (Math.random() < 0.01 && !bomb.destroyed) { // Adjust the probability as needed // final changed
                shootBomb(bomb); // Call the method to shoot
            }
        });
        // Boss movements

        for (Boss boss : boss) {
            // Calculate the offset between the player and the boss
            if (boss.posY >= 0) {
                boss.SPEED = 0;
            }
        }
        for (Wheel wheel : wheel) {
            // Calculate the offset between the player and the boss
            if (wheel.posY >= -100) {
                wheel.SPEED = 0;
            }
        }
        for (EnemyShooter enemyshooters : enemyshooter) {
            // Calculate the offset between the player and the boss
            if (enemyshooters.posY >= 100) {
                enemyshooters.SPEED = 0;
            }
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

            for (Bomb bomb : bombs) {
                if (!powerUpMenuTriggered && bomb.getRedCircle() != null && bomb.getRedCircle().isActive()
                        && bomb.getRedCircle().collide(player)) {
                    bomb.getRedCircle().deactivate(); // Deactivate red circle
                    // Show red circle power-up options
                    showRedCirclePowerUpOptions();
                    powerUpMenuTriggered = true; // Set the flag to true
                }
            }

            for (Boss boss : boss) {
                if (shot.collide(boss) && !boss.exploding) {
                    boss.hit(); // Decrease bomb's hitpoints
                    shot.toRemove = true;
                }
            }
            for (EnemyShooter enemyshooters : enemyshooter) {
                if (shot.collide(enemyshooters) && !enemyshooters.exploding) {
                    enemyshooters.hit(); // Decrease bomb's hitpoints
                    shot.toRemove = true;
                }
            }
            for (Wheel wheel : wheel) {
                if (shot.collide(wheel) && !wheel.exploding) {
                    shot.bosswheel = true;
                } else {
                    shot.bosswheel = false;
                }
            }

            for (Tower tower : tower) {
                if (shot.collide(tower) && !tower.exploding) {
                    shot.collidingWithWheel = true;
                    // You may break the loop here if you want to handle collisions with only one
                    // tower
                    break;
                } else {
                    shot.collidingWithWheel = false;
                }
            }

        }

        // Update and draw shots // Changes L
        for (int i = enemyshots.size() - 1; i >= 0; i--) {
            EnemyShot enemyshot = enemyshots.get(i);
            if (enemyshot.posY > HEIGHT || enemyshot.toRemove) {
                enemyshots.remove(i);
                continue;
            }
            enemyshot.update();
            enemyshot.draw();
            if (enemyshot.collide(player) && !player.exploding && Shield >= 1) {
            if (enemyshot.collide(player) && !player.exploding && Shield >= 1) {
        		Shield--;
        		enemyshot.toRemove = true;
        	}
            if (enemyshot.collide(player) && !player.exploding && Shield <= 0) {
                enemyshot.toRemove = true; // Remove the enemy shot after collision

                player.explode(); // Player explodes if hit by an enemy shot
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
            ButtonType fasterShipButton = new ButtonType("Wide Shot");

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
                	Shield += 2;
                	timeline.play();
                } else if (buttonType == fasterShipButton) {
                	SPLIT++;
                	timeline.play();
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

            });
        });
    }

    private void shootBomb(Bomb bomb) {
        int bulletX = bomb.posX + bomb.size / 2; // Calculate bullet position
        int bulletY = bomb.posY + bomb.size; // Adjust as needed

        // Create and add the bullet to the shots list
        enemyshots.add(new EnemyShot(bulletX, bulletY, Math.PI / 2, BULLET_IMG[0]));
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
                    currentpu++;
                    timeline.play();
                } else if (buttonType == fasterBulletButton) {
                    // Apply the chosen power-up effect for faster bullet
                    Shot.speed *= 1.2;
                    MAX_SHOTS += 2;
                    redCirclePowerUps.add("FASTER");
                    System.out.println("Faster Bullet power-up added.");
                    currentpu++;
                    timeline.play();
                }
                // Reset the flag after applying the power-up effect:
                showPowerUpSelection = false;
            });
        });
    }

    public static void main(String[] args) {
        launch();
    }

    public class Rocket {
        int posX, posY, size;
        boolean exploding, destroyed, entangled;
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
            int bulletX = posX + size / 2;
            // Create a new Shot object
            return new Shot(bulletX, posY - Shot.size);
        }

        public void update() {
            if (exploding)
                explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;
            if (entangled) {
                // Slow down the player while colliding with time tower
            } else {
                // Speed up the bullet if it's not colliding with a wheel
                // Reset the speed to its original value
            }
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

    public class Bomb extends Rocket {
        int hitpoints;
        int SPEED = (score / 10) + 1;
        int directionX = 1;

        private RedCircle redCircle;
        private static final double POWER_UP_DROP_PROBABILITY = 0.3; // Adjust probability as needed

        public Bomb(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = MAX_HITPOINTS;
            redCircle = null;

        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posX += SPEED * directionX;
            ;
            if (posX <= -500 || posX >= (WIDTH + 500)) {
                directionX *= -1; // Reverse direction
            }
        }

        public void hit() {
            hitpoints -= DMG;
            if (hitpoints <= 0) {
                explode();
                score++;

                // Drop power-up based on probability
                if (Math.random() <= POWER_UP_DROP_PROBABILITY && currentpu <= 8) {
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

    public class EnemyShooter extends Rocket {
        int hitpoints;
        int SPEED = 5;
        private static final int SHOOTING_COOLDOWN = 50; // Adjust as needed
        private int shootingCooldown = SHOOTING_COOLDOWN;

        public EnemyShooter(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = 20;
        }

        @Override
        public void update() {
            super.update();

            // Move towards the player's position
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
            if (posY > HEIGHT)
                destroyed = true;

            // Check shooting cooldown
            shootingCooldown--;
            if (shootingCooldown <= 8 && !destroyed) {
                // Shoot towards the player
                shootTowardsPlayer();
                if (shootingCooldown <= 4) {
                    shootTowardsPlayer();
                    if (shootingCooldown <= 0) {
                        shootTowardsPlayer();
                        // Reset shooting cooldown
                        shootingCooldown = SHOOTING_COOLDOWN;
                    }
                }

            }
        }

        private void shootTowardsPlayer() {
            // Calculate direction towards the player
            double deltaX = player.posX - posX;
            double deltaY = player.posY - posY;
            double angle = Math.atan2(deltaY, deltaX);

            // Create a new shot towards the player's position
            int bulletX = posX + size / 2; // Adjust as needed
            int bulletY = posY + size / 2; // Adjust as needed
            // You can adjust the speed and other properties of the shot as needed
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle, BULLET_IMG[1]));
        }

        public void hit() {
            hitpoints -= DMG;
            if (hitpoints <= 0) {
                explode();
                score++;
                destroyed = true; // Mark the enemy shooter as destroyed
            }
        }

        public boolean isDestroyed() {
            return destroyed;
        }
    }

    // UPDATE MARKER
    public class Boss extends Rocket {
        int hitpoints, maxHitpoints;
        int SPEED = 50;

        public Boss(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = 1600;
            maxHitpoints = 1600;
        }

        public void drawHealthBar() {
            double healthBarWidth = (double) hitpoints / maxHitpoints * size; // Calculate width based on current and
                                                                              // maximum hitpoints
            double healthBarHeight = 10; // Height of the health bar
            double healthBarX = posX; // X-coordinate of the health bar
            double healthBarY = posY + 50; // Y-coordinate of the health bar, positioned above the boss

            Font font = Font.font("Palatino Linotype", 25);
            gc.setFont(font);
            gc.setFill(Color.web("#FFFF6E"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Naia, the Avatar of Time", healthBarX + size / 2, healthBarY - 20);
            // Draw the background of the health bar (gray)
            gc.setFill(Color.GRAY);
            gc.fillRect(healthBarX, healthBarY, size, healthBarHeight);
            // Draw the current health level (green)
            gc.setFill(Color.RED);
            gc.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        }

        // Override the draw method to include drawing of the health bar
        @Override
        public void draw() {
            super.draw(); // Draw the boss sprite
            drawHealthBar(); // Draw the boss health bar
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

                score += 750;
            }
        }

        public void collapse() {
            hitpoints -= 30;
            if (hitpoints <= 0) {
                explode();
                score += 750;
            }
        }
    }

    public class Wheel extends Rocket {
        int hitpoints;
        int SPEED = 30;

        public Wheel(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = 1;
        }

        public void draw() {
            // Set the desired opacity value
            double opacity = 0.8; // Example value (0.0 for fully transparent, 1.0 for fully opaque)

            // Draw the image with the specified opacity
            gc.setGlobalAlpha(opacity);
            super.draw(); // Draw the image using the superclass method
            gc.setGlobalAlpha(1.0); // Reset the opacity to default after drawing
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
    }

    public class Tower extends Rocket {
        int SPEED = 5;

        public Tower(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
        }
    }

    // Shot class
    public static class Shot {

        public boolean toRemove;
        int posX, posY;
        public boolean collidingWithWheel, bosswheel;
        static int size = 6;
        static int speed = 10; // Add speed variable
        int currentspeed;

        private Image bulletImage;

        public Shot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            bulletImage = new Image("file:src/images/bullets.png");
            currentspeed = speed;
        }

        public void update() {
            posY -= currentspeed; // Update the position based on speed

            // Check if the bullet is colliding with a wheel
            if (collidingWithWheel) {
                this.currentspeed *= 0.8; // Slow down the bullet while colliding
            } else {
                // Speed up the bullet if it's not colliding with a wheel
                this.currentspeed = speed; // Reset the speed to its original value
            }

            if (bosswheel) {
                this.currentspeed = speed / 6; // Slow down the bullet while colliding
            }
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

    public class EnemyShot extends Shot {
        private Image image;
        int speed = 10 + score / 20;
        private double angle; // Add angle field

        public EnemyShot(int posX, int posY, double angle, Image image) {
            super(posX, posY);
            this.angle = angle;
            this.image = image;// Initialize angle
            // Customize the appearance or behavior of the enemy shot if needed
        }

        @Override
        public void update() {
            posX += Math.cos(angle) * speed;
            posY += Math.sin(angle) * speed;

            // Check if the shot collides with the player
            if (collidePlayer()) {
                // Perform actions when the shot collides with the player
                if(Shield <= 0) {
            	player.explode(); // For example, explode the player
                }
                toRemove = true; // Mark the shot for removal
            }
        }

        public void draw() {
            if (image != null) {
                gc.drawImage(image, posX, posY, size * 0.2, size * 5);
            }
        }

        // Method to check collision with the player
        private boolean collidePlayer() {
            // Calculate the distance between the shot and the player
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    player.posX + player.size / 2, player.posY + player.size / 2);
            // Check if the distance is less than the sum of the radii of the shot and the
            // player
            return distance < player.size / 2 + size / 2;
        }
    }

    public class Universe {
        int posX, posY;
        private int h, w, r, g, b;
        private double opacity;

        public Universe() {
            posX = RAND.nextInt(WIDTH);
            posY = 0;

        }

        public void draw() {
            gc.setFill(Color.rgb(r, g, b, opacity));
            gc.fillOval(posX, posY, w, h);
            posY += 20;
        }
    }

    private static final int TRIANGLE_ROWS = 3;
    private static final int ENEMY_SIZE = 50;
    private static final int ENEMY_GAP = 30;

    // Method to create triangle formation of enemies
    private void createTriangleFormation() {
        if (score < 150) {
            int[] possibleYValues = { 60, 120, 180 };
            int randomIndex = RAND.nextInt(possibleYValues.length); // Generates a random index within the array length
            int currentY = possibleYValues[randomIndex];

            // Calculate a random starting X position within the visible area of the screen
            int startX = RAND.nextBoolean() ? WIDTH : -290; // final changed

            for (int row = 0; row < 3; row++) { // Number of enemies in the current row

                // Calculate starting X position for the current row to center it
                startX += (row == 0) ? 0 : (ENEMY_SIZE + ENEMY_GAP) / 2; // Offset for subsequent rows
                int posX = startX + row * (ENEMY_SIZE + ENEMY_GAP);
                bombs.add(new Bomb(posX, currentY, ENEMY_SIZE, BOMBS_IMG[2]));

                // Move to the next row
            }
        }
    }

    // boss formation
    private void createBossFormation() {
        int currentY = -500; // Starting Y position of the triangle formation

        // spawn
        int posX = WIDTH / 2;

        boss.add(new Boss(posX - 390 / 2, currentY, 380, BOSS_IMG));
        wheel.add(new Wheel(posX - 580 / 2, currentY, 580, PROPS_IMG));
    }
}
