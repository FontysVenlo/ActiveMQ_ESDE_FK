package service.chat_update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.ChatBoxController;
import controller.LoginController;
import data.EncryptorDecryptor;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.ChatMessage;
import service.ActiveMQService;
import service.MQService;
import utils.ChatHelper;
import utils.QueueUtils;

import javax.jms.*;

/**
 * Class which is responsible for updating the Chat UI using a separate thread
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatUpdaterRunnable implements Runnable {

    private VBox chatBox;
    private MQService activeMQService;
    private Connection connection;
    private Session session;
    private Topic topic;
    private MessageConsumer messageConsumer;
    private String topicName;
    private String subId;

    /**
     * 3.1. Implement the constructor using the ActiveMQService
     */

    public ChatUpdaterRunnable(VBox chatBox, String subId, String topicName) {
        this.activeMQService = new ActiveMQService();
        this.chatBox = chatBox;
        this.subId = subId;
        this.topicName = topicName;

        try {
            // 3.1.1 create connection
            this.connection = this.activeMQService.createConnection();
            // 3.1.2 set client id using the subId
            this.connection.setClientID(this.subId);
            // 3.1.3 create session
            this.session = this.activeMQService.createSession(this.connection, Session.CLIENT_ACKNOWLEDGE);
            // 3.1.4 create topic
            this.topic = this.activeMQService.createTopic(this.session, this.topicName);
            // 3.1.5 create message consumer
            this.messageConsumer = this.activeMQService.createMessageConsumer(this.session, this.topic, this.subId);
        } catch (JMSException e) {
            System.out.println(e.getStackTrace());
        }

    }

    /**
     * Method which is used to terminate the current thread and its resources
     */
    public void terminator() {
        try {
            this.messageConsumer.close();
            this.session.unsubscribe(this.subId);
            this.session.close();
            this.connection.close();
        } catch (JMSException e) {
            System.out.println(e.getStackTrace());
        }
    }

    /**
     * 3.2. Implement the missing parts of the
     */
    @Override
    public void run() {
        // gson object which
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        EncryptorDecryptor encryptorDecryptor = new EncryptorDecryptor();

        try {
            this.connection.start();
            this.messageConsumer.setMessageListener(message -> {
                try {
                    // 3.2.1 Retrieve a message from the message properties using the MESSAGE field in the QueueUtils
                    String retrievedMessageAsString = (String) message.getObjectProperty(QueueUtils.MESSAGE);
                    String decryptedMessage = encryptorDecryptor.decrypt(retrievedMessageAsString);
                    ChatMessage receivedChatMessage = gson.fromJson(decryptedMessage, ChatMessage.class);

                    // 3.2.2 Acknowledge the message
                    message.acknowledge();
                    // Update the UI
                    Platform.runLater(() -> {
                        // Generate the message as a UI object
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
                    System.out.println(e.getStackTrace());
                }
            });

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
