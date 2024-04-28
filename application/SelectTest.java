package application;

import javafx.application.Application;
import javafx.stage.Stage;

public class SelectTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create an instance of SpaceInvaders
        SpaceInvaders spaceInvaders = new SpaceInvaders();
        
        // Call the start method on the instance
        spaceInvaders.start(primaryStage);
    }
}
