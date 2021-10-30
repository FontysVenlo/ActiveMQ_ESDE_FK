package service;

import controller.ChatRoomController;
import org.apache.activemq.ActiveMQConnectionFactory;
import utils.QueueUtils;

import javax.jms.*;

public class ActiveMQService {

    public Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(QueueUtils.QUEUE_USERNAME, QueueUtils.QUEUE_PASSWORD,
                QueueUtils.QUEUE_LOCATION);

        Connection connection= factory.createConnection();

        return connection;
    }


    public Session createSession(Connection connection, int sessionMode) throws JMSException {
        Session session = connection.createSession(false, sessionMode);
        return session;
    }

    public Topic createTopic(Session session, String topicName) throws JMSException {
        Topic topic = session.createTopic(topicName);
        return topic;
    }

    public Destination createDestination(Session session, String topicName) throws JMSException {
        return session.createTopic(topicName);
    }

    public MessageConsumer createMessageConsumer(Session session, Topic topic, String consumerId) throws JMSException{
        return session.createDurableSubscriber(topic, consumerId);
    }


    public MessageProducer createMessageProducer(Session session, Destination destination) throws JMSException{
        return session.createProducer(destination);
    }


}
