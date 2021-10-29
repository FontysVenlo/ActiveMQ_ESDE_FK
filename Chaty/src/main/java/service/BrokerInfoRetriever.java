package service;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.JMSException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class BrokerInfoRetriever {
    private static String QUEUE_USERNAME = "admin";
    private static String QUEUE_PASSWORD = "admin";
    private static String QUEUE_LOCATION = "tcp://localhost:61616";

    /**
     * Retrieve topic names list to be used as chatRooms
     * @return
     */
    public List<String> getTopics(){
        List<String> topicNames = new ArrayList<>();
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(QUEUE_USERNAME, QUEUE_PASSWORD,
                QUEUE_LOCATION);
        try {
            ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
            connection.start();
            DestinationSource destSource = connection.getDestinationSource();
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
