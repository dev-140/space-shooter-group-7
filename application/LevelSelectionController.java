package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.Node;

public class LevelSelectionController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button back_button;

    @FXML
    private Button level1;

    @FXML
    private Button level2;

    @FXML
    private Button level3;
    
    //For back button
    @FXML
    void onBackClicked(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    //For Level 1 Selection
    @FXML
    void onLevel1Clicked(ActionEvent event) throws IOException {
    	try {
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            SpaceInvaders spaceInvaders = new SpaceInvaders();
            spaceInvaders.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
