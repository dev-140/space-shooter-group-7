package application;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import application.SpaceShooter.Bomb;
import application.SpaceShooter.Shot;

public class LevelThree extends Application {

    private static final Random RAND = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 60;
    private static final int MAX_BOMBS = 10;
    private static final int MAX_SHOTS = MAX_BOMBS * 2;
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
    private int score;
    private int MAX_HITPOINTS = 1; // Changes L
    private boolean gameOver = false;
    private boolean powerUpAvailable = false;
    private boolean powerUpChosen = false;
    private Timeline timeline;
    

    static final Image PLAYER_IMG = new Image("file:images/player.png");
    static final Image EXPLOSION_IMG = new Image("file:images/explosion.png");
    static final Image[] BOMBS_IMG = {
        new Image("file:images/1.png"),
        new Image("file:images/2.png")
    };
    
    //Boss sprites
    static final Image BOSS_H_IMG = new Image("file:images/head.png");
    static final Image BOSS_IMG = new Image("file:images/body.png");

    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if (shots.size() < MAX_SHOTS) shots.add(player.shoot());
            if (gameOver) {
                gameOver = false;
                setup();
            }
        });
        setup();
        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("Space Invaders");
        stage.show();
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
        score = 0;
//        IntStream.range(0, MAX_BOMBS).mapToObj(i -> this.newBomb()).forEach(Bombs::add); 
        triangleSpawnTimeline = new Timeline(new KeyFrame(TRIANGLE_SPAWN_INTERVAL, e -> createTriangleFormation()));
        triangleSpawnTimeline.setCycleCount(Timeline.INDEFINITE);
        triangleSpawnTimeline.play();
        createTriangleFormation();        
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
        
        
        //all boss defeated and boss spawn
        boolean allBossesDefeated = boss.isEmpty() && bossH.isEmpty();
        
        if(score >= 150 && score < 151 && allBossesDefeated) {
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
            gc.fillText("Oroboros, the Endless Devourer", WIDTH / 2, 35);
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
            gc.fillText("OROBOROS SLAIN\nYour Score is: " + score + "\nnext level still work in progress", WIDTH / 2, HEIGHT / 2.5);
        }

        
        // Resume the game if power-up selection is done
        if (powerUpChosen) {
            timeline.play();
        }

        //DRAW UNIVERSE
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
        bossH.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
            if (player.collide(e) && !player.exploding) {
                player.explode();
            }
        });
        //Boss movements
        for (BossH bossH : bossH) {
            // Calculate the offset between the player and the boss
            int offsetX = (player.posX - (bossH.posX + 180))/20;
            
            // Update boss position based on the player's position
            bossH.posX += offsetX;            
        }
        for (Boss boss : boss) {
            // Calculate the offset between the player and the boss
            int offsetX = (player.posX - (boss.posX + 190))/22;
            
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
        
     

//        for (int i = Bombs.size() - 1; i >= 0; i--) {
//            if (Bombs.get(i).destroyed) {
//                Bombs.set(i, newBomb());
//            }
//        }

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
    //boss 
    private BossH newBossH() {
        return new BossH(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, BOSS_H_IMG);
    }
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
                    MAX_HITPOINTS--; // Changes L
                } else if (buttonType == fasterBulletButton) {
                    // Apply the chosen power-up effect for faster bullet
                    Shot.speed *= 2;
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
            // Adjust the starting position of the bullet by subtracting half of the bullet's size
            int bulletX = posX + size / 2 - Shot.size / 2;
            // Create a new Shot object
            return new Shot(bulletX, posY - Shot.size);
        }

        public void update() {
            if (exploding) explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;
        }

        public void draw() {
            if (exploding) {
                gc.drawImage(EXPLOSION_IMG, explosionStep % EXPLOSION_COL * EXPLOSION_W, (explosionStep / EXPLOSION_ROWS) * EXPLOSION_H + 1,
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

        public Bomb(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = MAX_HITPOINTS;
        }	

        public void update() {
            super.update();
            if (!exploding && !destroyed) posY += SPEED;
            if (posY > HEIGHT) destroyed = true;
            if (!exploding && !destroyed) {
                posY += 2; // Move down
                
                if (moveRight) {
                    posX += 2;
                } else {
                    posX -= 2;
                }
                
                if (posX <= 0 || posX >= WIDTH - size) {
                    moveRight = !moveRight;
                    posY += size;
                }
            }
        }
        public void hit() {
            hitpoints--;
            if (hitpoints <= 0  ) {
                explode();
                score++;
            }
        }
    }
    
    public class BossH extends Rocket {
    	int hitpoints;
        int SPEED = 3;

        public BossH(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
            hitpoints = 70 + (10*MAX_HITPOINTS);
            
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed) posY += SPEED;
        }
        public void hit() {
            hitpoints--;
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
            hitpoints = 70;
        }

        public void update() {
            super.update();
            if (!exploding && !destroyed) posY += SPEED;
        }
        public void hit() {
            hitpoints--;
            if (hitpoints <= 0) {
                explode();
                score+= 50;
            }
        }
        public void collapse() {
            hitpoints -= 30;
            if (hitpoints <= 0) {
                explode();
                score+= 50;
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
            bulletImage = new Image("file:images/bullets.png");
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
            if (opacity < 0) opacity *= -1;
            if (opacity > 0.5) opacity = 0.5;
        }

        public void draw() {
            if (opacity > 0.8) opacity -= 0.01;
            if (opacity < 0.1) opacity += 0.01;
            gc.setFill(Color.rgb(r, g, b, opacity));
            gc.fillOval(posX, posY, w, h);
            posY += 20;
        }
    }
    
    private boolean moveRight = true; 
    private static final int TRIANGLE_ROWS = 4;
    private static final int ENEMY_SIZE = 50;
    private static final int ENEMY_GAP = 5;
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
    
    //boss formation
    private void createBossFormation() {
        int currentY = (-500*11) - 400; // Starting Y position of the triangle formation
        
        // Calculate a random starting X position within the visible area of the screen
        
        //spawn head
        int posX = (WIDTH - 180) / 2;
       
        for (int row = 0; row < 12; row++) {
            int enemiesInRow = 1; // Number of enemies in the current row
            currentY += 410;
            boss.add(new Boss(posX, currentY, 500, BOSS_IMG));
        }

        // Add boss head (BOSS_H_IMG)
        currentY += 410; // Move to the next row
        bossH.add(new BossH(posX + 10, currentY, 480, BOSS_H_IMG));
            
        }
        
   
}
