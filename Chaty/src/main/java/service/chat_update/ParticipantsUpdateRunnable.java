package service.chat_update;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.RemoveInfo;
import service.ActiveMQService;
import service.MQService;

import javax.jms.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Runnable which listens to the given topic and updates its participants
 */
public class ParticipantsUpdateRunnable implements Runnable {

    private ListView participants;
    private Label noOfUsers;
    private MQService activeMQService;
    private HashMap<String, String> participantInfo;
    private Connection connection;
    private Session session;
    private Destination monitored;
    private Destination destination;
    private MessageConsumer messageConsumer;
    private String topicName;

    public ParticipantsUpdateRunnable(ListView participants, Label noOfUsers, String topicName) {
        this.activeMQService = new ActiveMQService();
        this.participantInfo = new HashMap<>();
        this.participants = participants;
        this.noOfUsers = noOfUsers;
        this.topicName = topicName;

        try {
            this.connection = this.activeMQService.createConnection();
            this.session = this.activeMQService.createSession(this.connection, Session.AUTO_ACKNOWLEDGE);
            this.monitored = this.session.createTopic(this.topicName);
            this.destination = this.activeMQService.createAdvisoryDestination(this.session, this.topicName);
            this.messageConsumer = this.activeMQService.createAdvisoryMessageConsumer(this.session, this.destination);
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
            this.session.close();
            this.connection.close();
        } catch (JMSException e) {
            System.out.println(e.getStackTrace());
        }
    }


    @Override
    public void run() {
        try {
            this.messageConsumer.setMessageListener(message -> {
                try {
                    // get the destination source for the monitored topic
                    Destination source = message.getJMSDestination();
                    // if the source is equal to the advisory support for the targeted topic then monitor
                    // the addition and removal of new users
                    if (source.equals(AdvisorySupport.getConsumerAdvisoryTopic(this.monitored))) {
                        ActiveMQMessage activeMQMessage = (ActiveMQMessage) message;
                        // if a consumer was added update the list with the new usernames
                        if (activeMQMessage.getDataStructure() instanceof ConsumerInfo) {
                            ConsumerInfo consumerInfo = (ConsumerInfo) activeMQMessage.getDataStructure();
                            // add participant username and connection id to the map
                            this.participantInfo.put(getConnectionIdPart(consumerInfo.getConsumerId().getConnectionId()),
                                    getUsernameFromSubscriptionName(consumerInfo.getSubscriptionName()));
                            // if a consumer was removed then remove it's corresponding user from the list of participants
                        } else if (activeMQMessage.getDataStructure() instanceof RemoveInfo) {
                            RemoveInfo removeInfo = (RemoveInfo) activeMQMessage.getDataStructure();
                            // remove participant from the map
                            this.participantInfo.remove(getConnectionIdPart(removeInfo.getObjectId().toString().trim()));
                        }
                        // Update the UI
                        Platform.runLater(() -> {
                            this.participants.setItems(FXCollections
                                    .observableList(new ArrayList<>(this.participantInfo.values())));
                            this.participants.refresh();
                            this.noOfUsers.setText("" + this.participantInfo.size());
                        });
                    }

                } catch (JMSException e) {
                    System.out.println(e.getStackTrace());
                }
            });
            // start the conenction
            this.connection.start();
        } catch (JMSException e) {
            System.out.println(e.getStackTrace());
        }
    }

    /**
     * Retrieve the username from the subscription name
     *
     * @param subscriptionName the subscriber's name
     * @return the username part of the subscription name
     */
    private String getUsernameFromSubscriptionName(String subscriptionName) {
        if (!subscriptionName.contains("-")) {
            return subscriptionName;
        }

        String[] subscriptionNameParts = subscriptionName.split("-");
        return subscriptionNameParts[0];
    }

    /**
     * Retrieve the connection id without the last part to be used as a key in the hashmap
     *
     * @param connectionId activemq connection id
     * @return part of the connection id
     */
    private String getConnectionIdPart(String connectionId){
        String[] connectionIdSplit = connectionId.split("-");
        return connectionId.replaceAll("-"+connectionIdSplit[4], "");
    }
}
