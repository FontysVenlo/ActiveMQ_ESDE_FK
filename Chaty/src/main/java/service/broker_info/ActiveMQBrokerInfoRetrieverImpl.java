package service.broker_info;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQTopic;
import service.ActiveMQService;
import service.MQService;
import utils.BrokerUtils;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Concrete implementation of the {@link ActiveMQBrokerInfoRetriever} interface
 */
public class ActiveMQBrokerInfoRetrieverImpl implements ActiveMQBrokerInfoRetriever {

    /**
     * 3. Implement the logic of retrieving the topics you created from ActiveMQ Broker
     */

    /**
     * Retrieve topic names list to be used as chatRooms
     *
     * @return a list of the available topic names
     */
    @Override
    public List<String> getTopics() {
        // 3.1 Create an MQService
        MQService mqService = new ActiveMQService();
        List<String> topicNames = new ArrayList<>();

        try {
            // 3.2 Create an active mq connection using the MQService - note it needs to be of type ActiveMQConnection
            ActiveMQConnection connection = (ActiveMQConnection) mqService.createConnection();
            // 3.3 Start the connection
            connection.start();
            // 3.4 Create a DestinationSource object using the previously created connections
            DestinationSource destinationSource = connection.getDestinationSource();
            // 3.5 Get the topics from the destination source
            Set<ActiveMQTopic> topics = destinationSource.getTopics();

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
            // 3.6 Close the destination source using its stop method and the previously created connection
            destinationSource.stop();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        return topicNames;
    }

}
