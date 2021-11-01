package service.broker_info;

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
 * Concrete implementation of the {@link ActiveMQBrokerInfoRetriever} interface
 */
public class ActiveMQBrokerInfoRetrieverImpl implements ActiveMQBrokerInfoRetriever {

    /**
     * Retrieve topic names list to be used as chatRooms
     *
     * @return a list of the available topic names
     */
    @Override
    public List<String> getTopics() {
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
                    String topicName = topic.getTopicName();
                    if (!topicName.contains("ActiveMQ.")) {
                        topicNames.add(topic.getTopicName());
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });
            // close open resources
            destSource.stop();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        return topicNames;
    }

}
