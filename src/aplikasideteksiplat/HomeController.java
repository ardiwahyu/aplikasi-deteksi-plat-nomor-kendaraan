/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aplikasideteksiplat;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author ARDI
 */
public class HomeController implements Initializable {
    
    
    @FXML
    private Label label;
    @FXML
    private ImageView btnStart;
    
    @FXML
    private void btnStartOnClick(ActionEvent event) throws IOException{
        Parent home = FXMLLoader.load(getClass().getResource("Program.fxml"));
        Scene homeScene = new Scene(home);
        Stage appStage = (Stage)((Node) event.getSource()).getScene().getWindow();
        appStage.hide();
        appStage.setResizable(true);
        appStage.setMaxHeight(691);
//        appStage.setMaxWidth(810);
        appStage.setMaxWidth(1150);
        appStage.setScene(homeScene);
        appStage.show();
    }
    
    @FXML
    private void btnStartOnMouseEntered(){
        Image image = new Image("/image/buttonStart_entered.png");
        btnStart.setImage(image);
    }
    @FXML
    private void btnStartOnMouseExited(){
        Image image = new Image("/image/buttonStart.png");
        btnStart.setImage(image);
    }
    @FXML
    private void btnStartOnMousePressed(){
        Image image = new Image("/image/buttonStart_pressed.png");
        btnStart.setImage(image);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
