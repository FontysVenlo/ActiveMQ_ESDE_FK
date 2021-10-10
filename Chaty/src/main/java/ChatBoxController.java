import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import model.ChatMessage;
import model.User;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import utils.ChatHelper;
import utils.ColourHelper;

import javax.jms.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

@Getter
@Setter
public class ChatBoxController implements Initializable {
    
    // QUEUE Settings
    private static String QUEUE_USERNAME = "admin";
    private static String QUEUE_PASSWORD = "admin";
    private static String QUEUE_LOCATION = "tcp://localhost:61616";
    private static String QUEUE_NAME = "demo";
    private static String TOPIC_NAME = "Topico";

    // Message property keys
    private static String MESSAGE = "Message";

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField messageBox;

    @FXML
    private Button sendMessageButton;

    @FXML
    private Label errorLabel;

    @FXML
    private AnchorPane chatPane;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox chatBox;

    private boolean isListening = false;
    private Color colour;
    private ConnectionFactory factory;


    @FXML
    void sendAction(ActionEvent event) {
        this.messageBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                performSend();
            }
        });
    }

    @FXML
    void send(ActionEvent event) {
        performSend();
    }

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

            this.factory = createActiveMqConnectionFactory(QUEUE_USERNAME, QUEUE_PASSWORD,
                    QUEUE_LOCATION);
            Connection connection = null;
            try {
                connection = createConnection(this.factory);
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createTopic(TOPIC_NAME);

                MessageProducer producer = createProducer(session, destination);
                // send message
                sendQueueMessage(producer, session, this.messageBox.getText());
                connection.close();
                this.messageBox.setText("");
            } catch (JMSException e) {
                Logger.getLogger("Failed : " + e.getStackTrace());
            }

    }

    /**
     * Send message to Queue or Topic
     *
     * @param messageProducer {@link MessageProducer}
     * @param session         {@link Session}
     * @param text            text field input
     * @throws JMSException
     */
    private void sendQueueMessage(MessageProducer messageProducer, Session session, String text) throws JMSException {
        Message message = session.createMessage();
        // create message
        User user = new User(LoginController.USERNAME, LoginController.SUBSCRIBER_NUMBER, this.colour.toString());
        ChatMessage msg = new ChatMessage(text, ChatHelper.returnCurrentLocalDateTimeAsString(), user);
        // chat message as json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String chatMessageAsJson = gson.toJson(msg);
        message.setObjectProperty(MESSAGE, chatMessageAsJson);
        // insert print statement
        messageProducer.send(message);
        session.close();
    }

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
     * Creates a listener which "listens" to a specific topic and updates the UI with the received message
     *
     * @return a runnable
     */
    private Runnable chatListener() {
        Runnable runnable = () -> {
            ConnectionFactory factory = new ActiveMQConnectionFactory(QUEUE_USERNAME, QUEUE_PASSWORD,
                    QUEUE_LOCATION);

            try {
                Connection connection = factory.createConnection();
                connection.setClientID(LoginController.USERNAME + "-" + LoginController.SUBSCRIBER_NUMBER);
                connection.start();

                Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                Topic topic = session.createTopic(TOPIC_NAME);

                MessageConsumer consumer = session.createDurableSubscriber(topic, "Consumer-" + LoginController.USERNAME);
                // Listen to messages
                consumer.setMessageListener(message -> {
                    // Buffer to create the message

                    try {
                        // Gson builder
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        // retrieve message
                        String messageAsJson = (String) message.getObjectProperty(MESSAGE);
                        ChatMessage receivedChatMessage = gson.fromJson(messageAsJson, ChatMessage.class);

                        // acknowledge arrival
                        message.acknowledge();

                        // Update the UI
                        Platform.runLater(() -> {
                            // Chat message box creation
                            HBox hbox = ChatHelper.displayReceivedMessage(receivedChatMessage);
                            if (receivedChatMessage.getUser().getUsername().equals(LoginController.USERNAME)
                                    && receivedChatMessage.getUser().getSubscriberNumber().equals(LoginController.SUBSCRIBER_NUMBER)) {
                                hbox.setAlignment(Pos.CENTER_RIGHT);
                            } else {
                                hbox.setAlignment(Pos.CENTER_LEFT);
                            }
                            chatBox.getChildren().add(hbox);
                        });

                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                });
            } catch (JMSException e) {
                e.printStackTrace();
            }
        };

        return runnable;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Set background colour
        this.chatBox.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, null, null)));
        // Assign user to a colour
        this.colour = ColourHelper.generateRandomColour();
        // Bind Chat Height to its parents height
        chatScrollPane.vvalueProperty().bind(chatBox.heightProperty());
        // listener which listens to incoming messages from the topic and updates the UI
        if (!this.isListening) {
            this.isListening = true;
            Thread thread = new Thread(chatListener());
            thread.run();
        }

    }

}