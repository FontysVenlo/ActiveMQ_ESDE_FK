import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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

    }

    public void login(javafx.event.ActionEvent event) throws IOException {
        // check username value
        if(this.usernameField.getText().isEmpty()){
            this.errorLabel.setText("Please insert a Username");
            this.errorLabel.setTextFill(Color.RED);
            return;
        }else{
            this.errorLabel.setText("");
        }

        Stage stage = (Stage) loginAnchorPane.getScene().getWindow();
        stage.close();
        Stage primaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
        Parent root = loader.load();
        // set username and subscriber
        USERNAME = this.usernameField.getText();
        SUBSCRIBER_NUMBER = RandomStringUtils.randomAlphanumeric(6);

        ChatBoxController controller = loader.getController();
        loader.setController(controller);

        primaryStage.setScene(new Scene(root,600,800));
        primaryStage.setTitle("A Chat That You Should Not Build");
        primaryStage.show();

    }

}
