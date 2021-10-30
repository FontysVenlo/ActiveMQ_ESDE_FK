package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.ChatBoxController;
import controller.ChatRoomController;
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
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
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
public class ChatUpdater implements Runnable {

    private volatile boolean terminator;
    private VBox chatBox;

    /**
     * Method which is used to terminate the current thread
     */
    public void terminate() {
        this.terminator = false;
    }

    /**
     * 2. Create the listener which is going to update the UI with the incoming messages
     */
    @Override
    public void run() {

        // gson object which
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        EncryptorDecryptor encryptorDecryptor = new EncryptorDecryptor();

        // 2.1 create client id using the username from that was set in the previous controller
        //     a dash - and the subscriber number also generated in the controller.LoginController class
        String clientId = LoginController.USERNAME + "-" + LoginController.SUBSCRIBER_NUMBER;

        // 2.1. Create connection factory using
        ConnectionFactory factory = new ActiveMQConnectionFactory(QueueUtils.QUEUE_USERNAME, QueueUtils.QUEUE_PASSWORD,
                QueueUtils.QUEUE_LOCATION);

        try {
            // 2.2. Create connection
            Connection connection = factory.createConnection();
            // 2.3. Set the Client ID using the username from that was set in the previous controller
            //      a dash - and the subscriber number also generated in the controller.LoginController class
            connection.setClientID(clientId);
            connection.start();

            // 2.4. Create a session which acknowledges the incoming messages
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            // 2.5. Create the topic object using the TOPIC_NAME
            Topic topic = session.createTopic(ChatRoomController.TOPIC_NAME);
            // 2.6. Create a MessageConsumer object
            MessageConsumer consumer = session.createDurableSubscriber(topic, clientId);
            // while the thread is running listen to incoming messages
            while (terminator) {

                // 2.7. Listen to incoming messages using the consumer
                consumer.setMessageListener(message -> {

                    try {

                        // 2.9. Retrieve a message from the message properties using the MESSAGE
                        String retrievedMessageAsString = (String) message.getObjectProperty(ChatBoxController.MESSAGE);
                        String decryptedMessage = encryptorDecryptor.decrypt(retrievedMessageAsString);
                        // 2.10. Create the ChatMessage object using the Gson and the decrypted message from step 9.
                        ChatMessage receivedChatMessage = gson.fromJson(decryptedMessage, ChatMessage.class);

                        // 2.11. Acknowledge the message
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
                        e.printStackTrace();
                    }
                });
            }

            /**
             * Close the message consumer, session and connection when they are not needed anymore
             * and shutdown the listener
             */
            if (!terminator) {
                consumer.close();
                // unsub from topic to re-use the id
                session.unsubscribe(clientId);
                session.close();
                connection.close();
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
