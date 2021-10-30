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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.ChatMessage;
import model.User;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import service.ChatUpdater;
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

    // Message property key
    public static String MESSAGE = "Message";

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

    private boolean isListening = true;
    private Color colour;
    private ConnectionFactory factory;
    private EncryptorDecryptor encryptorDecryptor;

    private ChatUpdater chatUpdater;
    private Thread updater;

    @FXML
    void send(ActionEvent event) {
        performSend();
    }

    /**
     * Takes the user to the previous page of the Chat Rooms list
     * @param event
     * @throws IOException
     */
    @FXML
    public void back(ActionEvent event) throws IOException {
        // close current state
        Node node = (Node) event.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();
        setStageExit(currentStage);
        currentStage.close();

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
     * 1.7.1 {@link ChatBoxController#sendQueueMessage(MessageProducer, Session, String)}
     */

    /**
     * Performs the actual send of the message
     */
    private void performSend() {

        // Cannot send an empty message
        if (StringUtils.isBlank(this.messageBox.getText())) {
            this.errorLabel.setText("Message Cannot be empty");
            this.errorLabel.setTextFill(Color.RED);
            return;
        }

        if (StringUtils.isNotBlank(this.errorLabel.getText())) {
            this.errorLabel.setText("");
        }

        Stage currentStage = (Stage) this.errorLabel.getScene().getWindow();
        setStageExit(currentStage);

        // 1.1 set the factory using the QUEUE_USERNAME, QUEUE_PASSWORD and QUEUE_LOCATION variables of in QueueUtils - (continue on 1.1.1)
        // QueueUtils is a helper class with public static fields
        this.factory = createActiveMqConnectionFactory(QueueUtils.QUEUE_USERNAME, QueueUtils.QUEUE_PASSWORD,
                QueueUtils.QUEUE_LOCATION);
        // 1.2 Create a connection object
        Connection connection = null;
        try {
            // 1.3 set the connection using the private createConnection - (continue on 1.3.1)
            connection = createConnection(this.factory);

            // 1.4 Create a session object with the Session.AUTO_ACKNOWLEDGE
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 1.5 Create a Destination Object using the TOPIC_NAME
            Destination destination = session.createTopic(ChatRoomController.TOPIC_NAME);
            // 1.6 Create MessageProducer using the createProducer private method which uses the session and connection objects from 1.5-1.6
            MessageProducer producer = createProducer(session, destination);
            // 1.7 send the message using the sendQueueMessage method - (continue on 1.7.1)
            sendQueueMessage(producer, session, this.messageBox.getText());
            // close the connection, producer to save resources
            producer.close();
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
    private void sendQueueMessage(MessageProducer messageProducer, Session session, String text) throws JMSException {
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
        message.setObjectProperty(MESSAGE, chatMessageAsJson);
        // 1.7.1.3 use the producer
        messageProducer.send(message);
        // Close the session
        session.close();
    }

    /**
     * 1.1.1 Implement the createActiveMqConnectionFactory method which creates a ConnectionFactory - hint one line solution
     */

    /**
     * Create an ActiveMQConnectionFactory using the given username, password and the url that corresponds to the targeted Queue URL
     *
     * @param username queue username
     * @param password queue password
     * @param url      queue url location
     * @return a ConnectionFactory of {@link ActiveMQConnectionFactory}
     */
    private ConnectionFactory createActiveMqConnectionFactory(String username, String password, String url) {
        return this.factory = new ActiveMQConnectionFactory(username, password,
                url);
    }

    /**
     * 1.3.1 Implement the createConnection method which creates a JMS Connection - hint one line solution
     */

    /**
     * Creates a connection to the Queue using the given factory
     *
     * @param connectionFactory
     * @return a Connection to the Queue
     * @throws JMSException
     */
    private Connection createConnection(ConnectionFactory connectionFactory) throws JMSException {
        return connectionFactory.createConnection();
    }

    /**
     * 1.6.1 Implement the createProducer method which creates a JMS MessageProducer - hint one line solution
     */

    /**
     * Generate a JMS MessageProducer for the given JMS Session and Destination
     *
     * @param session     JMS Session
     * @param destination JMS Destination
     * @return a JMS MessageProducer
     * @throws JMSException
     */
    private MessageProducer createProducer(Session session, Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    /**
     * Sets a listener of what to do when the the current stage is closing
     * @param stage Current active Stage
     */
    private void setStageExit(Stage stage){
        stage.setOnCloseRequest(event -> {
            // terminate the thread and kill it!!!!
            this.chatUpdater.terminate();
            this.updater.interrupt();
            try {
                this.updater.join();
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
        chatScrollPane.vvalueProperty().bind(chatBox.heightProperty());
        chatScrollPane.vvalueProperty().bind(chatBox.widthProperty());
        // listener for the message box
        this.messageBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                performSend();
            }
        });

        // Create an updater object and start a new thread which updates the ui
        this.chatUpdater = new ChatUpdater(true,chatBox);
        this.updater = new Thread(this.chatUpdater);
        this.updater.start();

    }

}