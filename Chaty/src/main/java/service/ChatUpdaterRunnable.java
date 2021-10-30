package service;

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

    public ChatUpdaterRunnable( VBox chatBox, String subId, String topicName) {
        this.activeMQService = new ActiveMQService();
        this.chatBox = chatBox;
        this.subId = subId;
        this.topicName = topicName;

        try {
            this.connection = this.activeMQService.createConnection();
            this.connection.setClientID(this.subId);
            this.session = this.activeMQService.createSession(this.connection, Session.CLIENT_ACKNOWLEDGE);
            this.topic = this.activeMQService.createTopic(this.session, this.topicName);
            this.messageConsumer = this.activeMQService.createMessageConsumer(this.session, this.topic, this.subId);
        } catch (JMSException e) {
            System.out.println(e.getStackTrace());
        }

    }

    /**
     * Method which is used to terminate the current thread and its resources
     */
    public void terminator(){
        try{
            this.messageConsumer.close();
            this.session.unsubscribe(this.subId);
            this.session.close();
            this.connection.close();
        }catch (JMSException e){
            System.out.println(e.getStackTrace());
        }
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

        try {
            this.connection.start();
            this.messageConsumer.setMessageListener(message -> {
                try {
                    // 2.9. Retrieve a message from the message properties using the MESSAGE
                    String retrievedMessageAsString = (String) message.getObjectProperty(QueueUtils.MESSAGE);
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
                    System.out.println(e.getStackTrace());
                }
            });

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
