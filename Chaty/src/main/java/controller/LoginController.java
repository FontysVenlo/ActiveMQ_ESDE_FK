package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import service.BrokerInfoRetriever;
import utils.TitleUtils;

import java.util.List;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller which emulates a login situation
 */
@Getter
@Setter
public class LoginController implements Initializable {

    @FXML
    private AnchorPane loginAnchorPane;

    @FXML
    private TextField usernameField;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView logoStart;

    public static String USERNAME="";
    public static String SUBSCRIBER_NUMBER ="";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // if there is
        this.usernameField.setOnKeyPressed(keyEvent -> {
            if(StringUtils.isNotBlank(this.errorLabel.getText())){
                this.errorLabel.setText("");
            }
        });

        this.usernameField.setOnKeyPressed(keyEvent -> {

        });

    }

    @FXML
    public void login() throws IOException {
        // check username value
        if(this.usernameField.getText().isEmpty()){
            this.errorLabel.setText("Please insert a Username");
            this.errorLabel.setTextFill(Color.RED);
            return;
        }

        // set username and subscriber number
        USERNAME = this.usernameField.getText();
        SUBSCRIBER_NUMBER = RandomStringUtils.randomAlphanumeric(6);

        Stage stage = (Stage) loginAnchorPane.getScene().getWindow();
        stage.close();
        Stage primaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chatRooms.fxml"));
        Parent root = loader.load();


        primaryStage.setScene(new Scene(root,600,800));
        primaryStage.setTitle(TitleUtils.CHAT_ROOMS_TITLE);
        primaryStage.show();

    }


}
