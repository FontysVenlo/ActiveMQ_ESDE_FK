import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import utils.TitleUtils;

import java.net.URL;

public class ChatBox extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();

        // set logo
        URL url = getClass().getResource("/pics/logo2.png");
        Image img = new Image(url.toExternalForm());
        // set logo image
        controller.setLogoStart(new ImageView(img));
        controller.getLogoStart().setLayoutX(192);
        controller.getLogoStart().setLayoutY(31);
        controller.getLoginAnchorPane().getChildren().add(controller.getLogoStart());
        loader.setController(controller);

        Scene scene = new Scene(root, 600, 600);
        stage.setTitle(TitleUtils.LOGIN_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}