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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import service.broker_info.ActiveMQBrokerInfoRetrieverImpl;
import utils.TitleUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Getter
@Setter
public class ChatRoomController implements Initializable {

    public static String TOPIC_NAME = "";
    public static Stage CHAT_CONTROLLER_STAGE = null;

    @FXML
    private AnchorPane chatRoomsAnchorPane;

    @FXML
    private AnchorPane chatRoomsSpace;

    @FXML
    private Label chatRoomsLabel;

    @FXML
    private Button refreshRooms;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createChatRooms();
    }

    @FXML
    public void refresh(ActionEvent event) {
        // Remove the current
        this.chatRoomsSpace.getChildren().clear();
        createChatRooms();
    }

    private void openChat() throws IOException {
        Stage stage = (Stage) chatRoomsAnchorPane.getScene().getWindow();
        stage.close();
        Stage primaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
        Parent root = loader.load();


        // on closing this stage reset the topic name
        primaryStage.setOnCloseRequest(event -> {
            TOPIC_NAME="";

        });

        primaryStage.setScene(new Scene(root, 915, 600));
        CHAT_CONTROLLER_STAGE = primaryStage;
        primaryStage.setTitle(TOPIC_NAME + " " + TitleUtils.CHAT_TITLE);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Retrieves the available chatrooms and displays them
     */
    private void createChatRooms() {
        ActiveMQBrokerInfoRetrieverImpl brokerInfoRetriever = new ActiveMQBrokerInfoRetrieverImpl();
        List<String> chatRooms = brokerInfoRetriever.getTopics();
        ListView chatRoomsList = new ListView(FXCollections.observableList(chatRooms));
        this.chatRoomsSpace.getChildren().add(chatRoomsList);

        chatRoomsList.setOnMouseClicked(event -> {
            Object chatRoomValue = chatRoomsList.getSelectionModel().getSelectedItem();
            if (chatRoomValue != null) {
                TOPIC_NAME = chatRoomValue.toString().trim();
                try {
                    openChat();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
