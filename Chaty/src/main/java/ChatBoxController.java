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

    Logger logger
            = Logger.getLogger(ChatBoxController.class.getName());

    // QUEUE Settings
    private static String QUEUE_USERNAME = "admin";
    private static String QUEUE_PASSWORD = "admin";
    private static String QUEUE_LOCATION = "tcp://localhost:61616";
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
     * 1. Implement the performSend() and all the private methods
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
     *
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

            // 1.1 set the factory using the QUEUE_USERNAME, QUEUE_PASSWORD and QUEUE_LOCATION variables of this Controller - (continue on 1.1.1)
            this.factory = createActiveMqConnectionFactory(QUEUE_USERNAME, QUEUE_PASSWORD,
                    QUEUE_LOCATION);
            // 1.2 Create a connection object
            Connection connection = null;
            try {
                // 1.3 set the connection using the private createConnection - (continue on 1.3.1)
                connection = createConnection(this.factory);

                // 1.4 Create a session object with the Session.AUTO_ACKNOWLEDGE
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // 1.5 Create a Destination Object using the TOPIC_NAME
                Destination destination = session.createTopic(TOPIC_NAME);
                // 1.6 Create MessageProducer using the createProducer private method which uses the session and connection objects from 1.5-1.6
                MessageProducer producer = createProducer(session, destination);
                // 1.7 send the message using the sendQueueMessage method - (continue on 1.7.1)
                sendQueueMessage(producer, session, this.messageBox.getText());
                // close the connection to save resources
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
        String chatMessageAsJson = gson.toJson(msg);
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
     *  2. Create the listener which is going to update the UI with the incoming messages
     */

    /**
     * Creates a listener which "listens" to a specific topic and updates the UI with the received message
     *
     * @return a runnable
     */
    private Runnable chatListener() {
        Runnable runnable = () -> {
            // 2.1. Create connection factory
            ConnectionFactory factory = new ActiveMQConnectionFactory(QUEUE_USERNAME, QUEUE_PASSWORD,
                    QUEUE_LOCATION);

            try {
                // 2.2. Create connection
                Connection connection = factory.createConnection();
                // 2.3. Set the Client ID using the username from that was set in the previous controller
                //    a dash - and the subscriber number also generated in the LoginController class
                connection.setClientID(LoginController.USERNAME + "-" + LoginController.SUBSCRIBER_NUMBER);
                connection.start();

                // 2.4. Create a session which acknowledges the incoming messages
                Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                // 2.5. Create the topic object using the TOPIC_NAME
                Topic topic = session.createTopic(TOPIC_NAME);
                // 2.6. Create a MessageConsumer object
                MessageConsumer consumer = session.createDurableSubscriber(topic, "Consumer-" + LoginController.USERNAME);
                // 2.7. Listen to incoming messages using the consumer
                consumer.setMessageListener(message -> {
                    // Buffer to create the message

                    try {
                        // 2.8. Create a gson object which will deserialize the object
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        // 2.9. Retrieve a message from the message properties using the MESSAGE
                        String messageAsJson = (String) message.getObjectProperty(MESSAGE);
                        // 2.10. Create the ChatMessage object using the Gson and the message from step 9.
                        ChatMessage receivedChatMessage = gson.fromJson(messageAsJson, ChatMessage.class);

                        // 2.11. Acknowledge the message
                        message.acknowledge();

                        // Update the UI
                        Platform.runLater(() -> {
                            // Generate the message
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
        chatScrollPane.vvalueProperty().bind(chatBox.widthProperty());
        // listener which listens to incoming messages from the topic and updates the UI
        if (!this.isListening) {
            this.isListening = true;
            Thread thread = new Thread(chatListener());
            thread.run();
        }

    }

}