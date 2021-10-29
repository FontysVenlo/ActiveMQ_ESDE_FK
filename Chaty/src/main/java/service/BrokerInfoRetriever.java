package service;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQTopic;
import utils.QueueUtils;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

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
