import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.Setter;
import org.apache.activemq.ActiveMQConnectionFactory;
import utils.Helper;

import javax.jms.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Logger;

@Getter
@Setter
public class ChatBoxController implements Initializable, ActionListener {

    // QUEUE Settings
    private static String QUEUE_USERNAME = "admin";
    private static String QUEUE_PASSWORD = "admin";
    private static String QUEUE_LOCATION = "tcp://145.93.128.93:61616";
    private static String QUEUE_NAME = "demo";
    private static String TOPIC_NAME = "Topico";
    private static String Payload = "";

    // Message property keys
    private static String USER = "User";
    private static String TIME = "Time";
    private static String MESSAGE = "Message";
    private static String COLOUR = "Colour";

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
    private int subscriberNumber;
    private ConnectionFactory factory;
    private String username = "";

    @FXML
    void sendAction(ActionEvent event) {
        this.messageBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    performSend();
                }
            }
        });

    }

    @FXML
    void send(ActionEvent event) {
        performSend();
    }

    /**
     *
     */
    private void performSend(){
        if (!this.isListening) {
            this.isListening = true;
            Thread thread = new Thread(chatListener());
            thread.run();
        }

        // Cannot send an empty message
        if (this.messageBox.getText().isEmpty()) {
            this.errorLabel.setText("Message Cannot be empty");
            this.errorLabel.setTextFill(Color.RED);
            return;
        }

        if (!this.errorLabel.getText().isEmpty()) {
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
     * @param messageProducer
     * @param session
     * @param text
     * @throws JMSException
     */
    private void sendQueueMessage(MessageProducer messageProducer, Session session, String text) throws JMSException {
        Message message = session.createMessage();
        message.setObjectProperty(TIME, returnCurrentLocalDateTimeAsString());
        message.setObjectProperty(USER, this.username);
        message.setObjectProperty(MESSAGE, text);
        message.setObjectProperty(COLOUR, this.colour.toString());

        messageProducer.send(message);
        session.close();
        Logger.getLogger("Message sent : " + message);
    }

    /**
     * Returns the current local date time as a string in the dd-MM-yyyy HH:mm format
     *
     * @return current local date time as a string
     */
    private String returnCurrentLocalDateTimeAsString() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String timeSent = dateTime.format(format);
        return timeSent;
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
     * @param session JMS Session
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
                connection.setClientID(username + "-" + subscriberNumber);
                connection.start();

                Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                Topic topic = session.createTopic(TOPIC_NAME);

                MessageConsumer consumer = session.createDurableSubscriber(topic, "Consumer-" + username);
                // Listen to messages
                consumer.setMessageListener(message -> {
                    // Buffer to create the message
                    StringBuffer sb = new StringBuffer();

                    try {
                        // the received message
                        Message receivedMessage = message;
                        String receivedUsername = (String) receivedMessage.getObjectProperty(USER);
                        String receivedColor = (String) receivedMessage.getObjectProperty(COLOUR);
                        // Construct message
                        sb.append(receivedUsername + " " + receivedMessage.getObjectProperty(TIME) + "\n");
                        sb.append(receivedMessage.getObjectProperty(MESSAGE));


                        message.acknowledge();

                        // Update the UI
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // Chat message JavaFX Node creation
                                HBox hbox = null;
                                if (receivedUsername.equals(username)) {
                                    hbox = displayReceivedMessage(sb.toString(), username, colour);
                                    hbox.setAlignment(Pos.CENTER_RIGHT);
                                } else {
                                    hbox = displayReceivedMessage(sb.toString(), receivedUsername, Color.valueOf(receivedColor));
                                    hbox.setAlignment(Pos.CENTER_LEFT);
                                }
                                chatBox.getChildren().add(hbox);
                            }
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

    /**
     * Creates a box, sets the icon for the user and returns a HBox to be added to the chat
     *
     * @param message  the received message
     * @param username the username
     * @param colour   the colour of the user
     * @return a HBox containing the message
     */
    private HBox displayReceivedMessage(String message, String username, Color colour) {
        HBox hbox = new HBox(12);

        // Add user Circle
        Circle img = new Circle(32, 32, 16);
        img.setFill(colour);

        // Generate the user icon
        String labelText = username.substring(0, 1);
        Text userLogoText = new Text(labelText);
        userLogoText.setBoundsType(TextBoundsType.VISUAL);
        userLogoText.setFont(new Font(20));

        // Added to a pane
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(img, userLogoText);

        // Generate the message
        Text text = new Text(message);
        TextFlow tempFlow = new TextFlow();
        tempFlow.getChildren().add(text);
        tempFlow.setMaxWidth(200);

        // add the Nodes to the HBox
        hbox.getChildren().add(stackPane);
        hbox.getChildren().add(tempFlow);

        // Set Message box padding
        hbox.setPadding(new Insets(5));

        return hbox;
    }

    /**
     * Returns random Colour object based on a list
     *
     * @return a Color
     */
    private Color returnCorrespondingColor() {
        List<String> colours = Helper.colourList();
        Random rand = new Random();
        int randomNumber = rand.nextInt(colours.size() - 1);

        String colourValue = colours.get(randomNumber);

        return Color.valueOf(colourValue.toUpperCase());
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chatBox.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN,null,null)));
        // Assign user to a colour
        this.colour = returnCorrespondingColor();
        Random random = new Random();
        this.subscriberNumber = random.nextInt(1000);
        // Bind Chat Height to its parents height
        chatScrollPane.vvalueProperty().bind(chatBox.heightProperty());
    }

}