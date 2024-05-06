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

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import application.LevelThree.Boss;
import application.LevelThree.Rocket;
import application.LevelThree.Universe;

public class LevelThree extends Application {

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
    private List<EnemyShot> enemyshots;
    private List<Universe> universes;
    private List<Bomb> bombs;

    private List<Boss> boss;

    private boolean showPowerUpSelection = false; // Combined flag
    private double mouseX;
    private double mouseY;
    private int score;
    private int MAX_HITPOINTS = 1; // Changes L
    private int MAX_SHOTS = 15;
    private boolean gameOver = false;
    private boolean powerUpAvailable = false;
    private boolean powerUpChosen = false;
    private Timeline timeline;
    private boolean gameFinished = false;
    private int DMG = 3; // Changes L

    static final Image PLAYER_IMG = new Image("file:src/images/player.png");
    static final Image EXPLOSION_IMG = new Image("file:src/images/explosion.png");
    static final Image BULLET_IMG = new Image("file:src/images/homingbullets.png");
    static final Image[] BOMBS_IMG = {
            new Image("file:src/images/4.png")
    };

    // Boss sprites
    static final Image BOSS_IMG = new Image("file:src/images/Seraphim.png");

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
            	shots.add(new Shot(player.posX, player.posY - Shot.size));
            	shots.add(new Shot(player.posX + player.size, player.posY - Shot.size));
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
                Main levelthree = new Main();
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
        stage.setTitle("Heaven's Gate");
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
        blackholes = new ArrayList<>();
        asteroids = new ArrayList<>();
        player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        // IntStream.range(0, MAX_BOMBS).mapToObj(i ->
        // this.newBomb()).forEach(Bombs::add);
        triangleSpawnTimeline = new Timeline(new KeyFrame(TRIANGLE_SPAWN_INTERVAL, e -> createTriangleFormation()));
        triangleSpawnTimeline.setCycleCount(Timeline.INDEFINITE);
        triangleSpawnTimeline.play();
        createTriangleFormation();
    }

    private List<Blackhole> blackholes;

    public class Blackhole extends Rocket {
        int SPEED = 5;

        public Blackhole(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
        }
    }

    private List<Asteroid> asteroids;

    public class Asteroid extends Rocket {
        int SPEED = 20;

        public Asteroid(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
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

        // Debug print
        System.out.println("Current score: " + score);

        // Check if power-up is available and handle power-up selection
        if (score > 0 && score % 35 == 0 && !showPowerUpSelection && score < 152) {
            showPowerUpSelection = true; // Set flag when score reaches a multiple of 20
            // Pause the game
            timeline.stop();
            // Prompt the player to choose a power-up
            showPowerUpOptions();
            score++;

        }
        if (RAND.nextInt(500) < 1 && score >= 50 && score <= 150) {
            int X;
            X = 50 + RAND.nextInt(WIDTH - 100);
            Blackhole newBlackhole = new Blackhole(X, 0, PLAYER_SIZE, new Image("file:src/images/blackhole.png"));
            blackholes.add(newBlackhole);
        }

        blackholes.forEach(blackholes -> {
            blackholes.update();
            blackholes.draw();
            double distance = Math
                    .sqrt(Math.pow(player.posX - blackholes.posX, 2) + Math.pow(player.posY - blackholes.posY, 2));

            // Define the range where gravitational pull starts affecting the player
            double gravitationalRange = 200; // Adjust this value as needed

            if (distance < gravitationalRange) {
                {
                    Robot robot;

                    // Calculate gravitational force direction

                    double dx = blackholes.posX - player.posX;
                    double dy = blackholes.posY - player.posY;
                    double angle = Math.atan2(dy, dx);
                    int offsetX = (blackholes.posX - (player.posX)) / 3;
                    // Apply gravitational force to player
                    try {
                        robot = new Robot();

                        int currentX = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX();
                        robot.mouseMove(currentX + offsetX,
                                (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY());

                    } catch (AWTException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        if (RAND.nextInt(500) < 3 && score <= 150) {
            Asteroid newAsteroid = new Asteroid(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE,
                    new Image("file:src/images/asteroid.png"));
            asteroids.add(newAsteroid);
        }

        asteroids.forEach(asteroid -> {
            asteroid.update();
            asteroid.draw();

            if (player.collide(asteroid) && !player.exploding) {
                player.explode();
            }
        });

        // all boss defeated and boss spawn

        if (score >= 150 && boss.size() < 1) {
            createBossFormation();
            score++;

        }

        if (score > 850) {
            // Stop the game
            timeline.stop();

            // Display "LEVEL CLEARED" screen
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);
            gc.setFont(Font.font(35));
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GUARDIAN SLAIN\nYour Total Score is: " + (score + 850 * 2) + "\nYou finished the game",
                    WIDTH / 2, HEIGHT / 2.5);

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

        player.posX = (int) (mouseX - player.size / 2); // center the player

        // Update and draw bombs
        bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });

        // Update and draw boss
        boss.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });
        // Update and draw boss head

        // Boss movements

        // Update and draw shots // Changes L

        for (int i = enemyshots.size() - 1; i >= 0; i--) {
            EnemyShot enemyshot = enemyshots.get(i);
            if (enemyshot.posY > HEIGHT || enemyshot.toRemove) {
                enemyshots.remove(i);
                continue;
            }
            enemyshot.update();
            enemyshot.draw();
            if (enemyshot.collide(player) && !player.exploding) {
                player.explode(); // Player explodes if hit by an enemy shot
                enemyshot.toRemove = true; // Remove the enemy shot after collision
            }

        }

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

        }

        // for (int i = Bombs.size() - 1; i >= 0; i--) {
        // if (Bombs.get(i).destroyed) {
        // Bombs.set(i, newBomb());
        // }
        // }

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

    private void showPowerUpOptions() {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Choose Power-Up");
            dialog.setHeaderText("Select a power-up:");

            // Create buttons for different power-up options
            ButtonType biggerBulletButton = new ButtonType("Bigger Bullet");
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
                    // Apply the chosen power-up effect for bigger bullet
                    Shot.size *= 1.5;
                    DMG++; // Changes L
                } else if (buttonType == fasterBulletButton) {
                    // Apply the chosen power-up effect for faster bullet
                    Shot.speed *= 2;
                    MAX_SHOTS += 5;
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
            int bulletX = posX + size / 2;
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

    public class Bomb extends Rocket {
        int hitpoints;
        int SPEED = (score / 10) + 1;
        private static final int SHOOTING_COOLDOWN = 50; // Adjust as needed
        private int shootingCooldown = SHOOTING_COOLDOWN;

        public Bomb(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = MAX_HITPOINTS;
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed)
                posY += SPEED;
            if (posY > HEIGHT)
                destroyed = true;
            if (!exploding && !destroyed) {
                posY += 2; // Move down

                if (moveRight) {
                    posX += SPEED;
                } else {
                    posX -= SPEED;
                }

                if (posX <= 0 || posX >= WIDTH - size) {
                    moveRight = !moveRight;
                    posY += size;
                }
            }
            shootingCooldown--;
            if (shootingCooldown <= 3 && !destroyed) {
                // Shoot towards the player
                shootTowardsPlayer();
                if (shootingCooldown <= 0 && !destroyed) {
                    shootingCooldown = SHOOTING_COOLDOWN;
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
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle, BULLET_IMG));
        }

        public void hit() {
            hitpoints -= DMG;
            if (hitpoints <= 0) {
                explode();
                score++;
            }
        }
    }

    public class Boss extends Rocket {
        int hitpoints, maxHitpoints;
        int SPEED = 5;
        int directionX = 1;
        int directionY = 1;
        private static final int SHOOTING_COOLDOWN = 120; // Adjust as needed
        private int shootingCooldown = SHOOTING_COOLDOWN;

        public Boss(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = 1000;
            maxHitpoints = hitpoints;
        }

        public void drawHealthBar() {
            double healthBarWidth = (double) hitpoints / maxHitpoints * size; // Calculate width based on current and
                                                                              // maximum hitpoints
            double healthBarHeight = 10; // Height of the health bar
            double healthBarX = (WIDTH / 2) - (500 / 2); // X-coordinate of the health bar
            double healthBarY = -40 + 90; // Y-coordinate of the health bar, positioned above the boss

            Font font = Font.font("Palatino Linotype", 25);
            gc.setFont(font);
            gc.setFill(Color.web("#FFFF6E"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("リ⊣ ∴╎リ⊣ \n The Gate Keeper", healthBarX + size / 2, healthBarY - 20);
            // Draw the background of the health bar (gray)
            gc.setFill(Color.GRAY);
            gc.fillRect(healthBarX, healthBarY + 30, size, healthBarHeight);
            // Draw the current health level (green)
            gc.setFill(Color.RED);
            gc.fillRect(healthBarX, healthBarY + 30, healthBarWidth, healthBarHeight);
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
                posY += 2 * directionY;
            ;
            if (!exploding && !destroyed)
                posX += SPEED * directionX;
            ;
            if (posX <= -10 || posX >= (WIDTH) - 490) {
                directionX *= -1; // Reverse direction
            }
            if (posY <= -120 || posY >= -45) {
                directionY *= -1; // Reverse direction
            }
            if (!exploding && hitpoints <= 50)
                hitpoints++;
            if (shootingCooldown <= 90 && !destroyed) {
                // Shoot towards the player
                shootingCooldown--;
                if (shootingCooldown <= 80 && !destroyed) {
                    // Shoot towards the player
                    shootingCooldown--;
                    shotgun();
                    if (shootingCooldown <= 0 && !destroyed) {
                        shootingCooldown = SHOOTING_COOLDOWN;
                    }
                }
            }
        }

        private void shootTowardsPlayer() {
            // Calculate direction towards the player
            double deltaX = player.posX - (posX + 250);
            double deltaY = player.posY - (posY + 250);
            double angle = Math.atan2(deltaY, deltaX);

            // Create a new shot towards the player's position
            int bulletX = posX + size / 2; // Adjust as needed
            int bulletY = posY + size / 2; // Adjust as needed
            // You can adjust the speed and other properties of the shot as needed
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle, BULLET_IMG));
        }

        private void shotgun() {
            // Calculate direction towards the player
            double deltaX = player.posX - (posX + 250);
            double deltaY = player.posY - (posY + 250);
            double angle = Math.atan2(deltaY, deltaX);

            // Create a new shot towards the player's position
            int bulletX = posX + size / 2; // Adjust as needed
            int bulletY = posY + size / 2; // Adjust as needed
            // You can adjust the speed and other properties of the shot as needed
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle + 0.25, new Image("file:src/images/bullets.png")));
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle - 0.25, new Image("file:src/images/bullets.png")));
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle + 0.5, new Image("file:src/images/bullets.png")));
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle - 0.5, new Image("file:src/images/bullets.png")));
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle + 0.75, new Image("file:src/images/bullets.png")));
            enemyshots.add(new EnemyShot(bulletX, bulletY, angle - 0.75, new Image("file:src/images/bullets.png")));
        }

        public void hit() {
            hitpoints -= DMG;
            shootTowardsPlayer();
            shootingCooldown--;
            if (hitpoints <= 0) {
                explode();
                score += 750;
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

    public class EnemyShot extends Shot {
        private Image image;
        int speed = 30 ;
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
            if (collidePlayer() && !player.exploding) {
                // Perform actions when the shot collides with the player
                player.explode(); // For example, explode the player
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

    private boolean moveRight = true;
    private static final int TRIANGLE_ROWS = 2;
    private static final int ENEMY_SIZE = 80;
    private static final int ENEMY_GAP = 15;
    private int startX = 100;

    private void createTriangleFormation() {
        if (score < 150 || score >= 160) {
            int currentY = -210;

            int startX = RAND.nextInt(WIDTH - (TRIANGLE_ROWS * (ENEMY_SIZE + ENEMY_GAP)));

            for (int row = 0; row < TRIANGLE_ROWS; row++) {
                int enemiesInRow = TRIANGLE_ROWS - row;

                startX += (row == 0) ? 0 : (ENEMY_SIZE + ENEMY_GAP) / 2;
                boolean moveRight = row % 2 == 0; // Alternate initial movement direction
                for (int i = 0; i < enemiesInRow; i++) {
                    int posX = startX + i * (ENEMY_SIZE + ENEMY_GAP);
                    bombs.add(new Bomb(posX, currentY, ENEMY_SIZE, BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]));
                }

                currentY += ENEMY_SIZE + ENEMY_GAP;
            }
        }
    }

    // boss formation
    private void createBossFormation() {
        int currentY = -50; // Starting Y position of the triangle formation

        // spawn
        int posX = WIDTH / 2;

        boss.add(new Boss(posX - 500 / 2, currentY, 500, BOSS_IMG));
    }
}

