package service;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQTopic;
import utils.QueueUtils;

import javax.jms.JMSException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Class which is used to retrieve information from the ActiveMQ Broker
 */
public class BrokerInfoRetriever {

    /**
     * Retrieve topic names list to be used as chatRooms
     * @return
     */
    public List<String> getTopics(){
        List<String> topicNames = new ArrayList<>();
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(QueueUtils.QUEUE_USERNAME, QueueUtils.QUEUE_PASSWORD,
                QueueUtils.QUEUE_LOCATION);

        try {
            // create an ActiveMQConnection
            ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
            connection.start();
            // Create a Destination Source
            DestinationSource destSource = connection.getDestinationSource();
            // Get the current topics
            Set<ActiveMQTopic> topics = destSource.getTopics();

            topics.forEach(topic -> {
                try {
                    topicNames.add(topic.getTopicName());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        return topicNames;
    }

}
