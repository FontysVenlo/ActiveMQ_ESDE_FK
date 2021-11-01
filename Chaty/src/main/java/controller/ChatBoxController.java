package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.EncryptorDecryptor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.Setter;
import model.ChatMessage;
import model.User;
import org.apache.commons.lang3.StringUtils;
import service.MQService;
import service.ActiveMQService;
import service.chat_update.ChatUpdaterRunnable;
import service.chat_update.ParticipantsUpdateRunnable;
import utils.ChatHelper;
import utils.ColourHelper;
import utils.QueueUtils;
import utils.TitleUtils;

import javax.jms.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * controller.ChatBoxController which handles the chat stage actions
 */
@Getter
@Setter
public class ChatBoxController implements Initializable {

    Logger logger
            = Logger.getLogger(ChatBoxController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField messageBox;

    @FXML
    private Button sendMessageButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @FXML
    private AnchorPane chatPane;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox chatBox;

    @FXML
    private ListView<String> participants;

    @FXML
    private Label usersLabel;

    @FXML
    private Label numberOfUsers;

    private Color colour;
    private EncryptorDecryptor encryptorDecryptor;

    private ParticipantsUpdateRunnable participantsUpdateRunnable;
    private Thread participantsUpdater;

    private ChatUpdaterRunnable chatUpdaterRunnable;
    private Thread updater;

    private boolean hasEntered = false;

    @FXML
    void send(ActionEvent event) {
        performSend();
    }

    /**
     * Binds the exit strategy the first time the mouse enters the main layout
     *
     * @param mouseEvent {@link MouseEvent}
     */
    @FXML
    public void bindExit(MouseEvent mouseEvent) {
        if (!this.hasEntered) {
            Stage currentStage = (Stage) this.anchorPane.getScene().getWindow();
            setStageExit(currentStage);
            this.hasEntered = true;
        }
    }

    /**
     * Takes the user to the previous page of the Chat Rooms list
     *
     * @param event {@link ActionEvent}
     * @throws IOException
     */
    @FXML
    public void back(ActionEvent event) throws IOException {
        // close current stage
        Node node = (Node) event.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();
        // set stage exit if it has not been set
        setStageExit(currentStage);
        // close current stage
        currentStage.fireEvent(
                new WindowEvent(currentStage,
                        WindowEvent.WINDOW_CLOSE_REQUEST));

        // load chat rooms page
        Stage primaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chatRooms.fxml"));
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root, 600, 800));
        primaryStage.setTitle(TitleUtils.CHAT_ROOMS_TITLE);
        primaryStage.show();
    }

    /**
     * 1. Implement the performSend() and all the private methods
     *
     * Quick links to other exercises
     *
     * 1.1.1 {@link ChatBoxController#createActiveMqConnectionFactory(String, String, String)}
     *
     * 1.3.1 {@link ChatBoxController#createConnection(ConnectionFactory)}
     *
     * 1.6.1 {@link ChatBoxController#createProducer(Session, Destination)}
     *
     * 1.7.1 {@link ChatBoxController#sendMessage(MessageProducer, Session, String)}
     */

    /**
     * Performs the actual send of the message
     */
    private void performSend() {

        // Cannot send an empty message
        if (StringUtils.isBlank(this.messageBox.getText())) {
            this.errorLabel.setText("Message cannot be empty");
            this.errorLabel.setTextFill(Color.RED);
            return;
        }

        Stage currentStage = (Stage) this.errorLabel.getScene().getWindow();
        setStageExit(currentStage);

        MQService activeMQService = new ActiveMQService();

        try {
            // 1.2 Create a connection object
            Connection connection = activeMQService.createConnection();

            // 1.4 Create a session object with the Session.AUTO_ACKNOWLEDGE
            Session session = activeMQService.createSession(connection, Session.AUTO_ACKNOWLEDGE);

            // 1.5 Create a Destination Object using the TOPIC_NAME
            Destination destination = activeMQService.createDestination(session, ChatRoomController.TOPIC_NAME);
            // 1.6 Create MessageProducer using the createProducer private method which uses the session and connection objects from 1.5-1.6
            MessageProducer producer = activeMQService.createMessageProducer(session, destination);
            // 1.7 send the message using the sendQueueMessage method - (continue on 1.7.1)
            sendMessage(producer, session, this.messageBox.getText());

            // close the connection, session and  producer to save resources
            producer.close();
            session.close();
            connection.close();
            this.messageBox.setText("");
        } catch (JMSException e) {
            this.logger.info("Failed : " + e.getStackTrace());
        }

    }

    /**
     * 1.7.1 Implement the missing parts of the sendQueueMessage method
     */

    /**
     * Send message to Queue or Topic
     *
     * @param messageProducer {@link MessageProducer}
     * @param session         {@link Session}
     * @param text            text field input
     * @throws JMSException
     */
    private void sendMessage(MessageProducer messageProducer, Session session, String text) throws JMSException {
        //1.7.1.1 Create Message object using the given Session parameter
        Message message = session.createMessage();
        // create message and user
        User user = new User(LoginController.USERNAME, LoginController.SUBSCRIBER_NUMBER, this.colour.toString());
        ChatMessage msg = new ChatMessage(text, ChatHelper.returnCurrentLocalDateTimeAsString(), user);
        // chat message as json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        this.encryptorDecryptor = new EncryptorDecryptor();
        String chatMessageAsJson = encryptorDecryptor.encrypt(gson.toJson(msg));
        // 1.7.1.2 Pass the chatMessageAsJson as an ObjectProperty of the message using the setObjectProperty(MESSAGE, chatMessageAsJson)
        message.setObjectProperty(QueueUtils.MESSAGE, chatMessageAsJson);
        // 1.7.1.3 use the producer
        messageProducer.send(message);
    }

    /**
     * Sets a listener of what to do when the current stage is closing
     *
     * @param stage Current active Stage
     */
    public void setStageExit(Stage stage) {
        stage.setOnCloseRequest(event -> {
            // terminate the thread and its resources and kill it!!!!
            this.chatUpdaterRunnable.terminator();
            this.participantsUpdateRunnable.terminator();
            // interrupt the threads
            this.participantsUpdater.interrupt();
            this.updater.interrupt();
            try {
                this.updater.join();
                this.participantsUpdater.join();
                ChatRoomController.TOPIC_NAME = "";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Set background colour
        this.chatBox.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, null, null)));
        // Assign user to a colour
        this.colour = ColourHelper.generateRandomColour();
        // Bind Chat Height to its parents height
        this.chatScrollPane.vvalueProperty().bind(chatBox.heightProperty());
        this.chatScrollPane.vvalueProperty().bind(chatBox.widthProperty());
        // listener for the message box when user presses enter
        this.messageBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                performSend();
            }

            if (StringUtils.isNotBlank(this.errorLabel.getText())) {
                this.errorLabel.setText("");
            }
        });


        // Client id which is used to set the connection id and the consumer name
        String clientId = LoginController.USERNAME + "-" + LoginController.SUBSCRIBER_NUMBER;
        // Create an updater object and start a new thread which updates the ui
        this.chatUpdaterRunnable = new ChatUpdaterRunnable(this.chatBox, clientId, ChatRoomController.TOPIC_NAME);
        this.updater = new Thread(this.chatUpdaterRunnable);
        this.updater.start();

        // Create an updater for the participants
        this.participantsUpdateRunnable = new ParticipantsUpdateRunnable(this.participants, this.numberOfUsers, ChatRoomController.TOPIC_NAME);
        this.participantsUpdater = new Thread(this.participantsUpdateRunnable);
        this.participantsUpdater.start();
    }


}