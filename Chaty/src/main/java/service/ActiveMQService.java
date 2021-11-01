package service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import utils.QueueUtils;

import javax.jms.*;

/**
 * Concrete implementation of {@link MQService}
 */
public class ActiveMQService implements MQService {

    /**
     * Creates a JMS {@link Connection} using the fields of the {@link QueueUtils} and a JMS {@link ConnectionFactory} of type
     * {@link ActiveMQConnectionFactory} in order to create a connection to the given ActiveMQ broker
     *
     * @return a JMS {@link Connection}
     * @throws JMSException
     */
    @Override
    public Connection createConnection() throws JMSException {
        // 1.1 create a ConnectionFactory of type ActiveMQConnectionFactory using the QUEUE_USERNAME, QUEUE_PASSWORD and QUEUE_LOCATION
        // fields in the QueueUtils class
        ConnectionFactory factory = new ActiveMQConnectionFactory(QueueUtils.QUEUE_USERNAME, QueueUtils.QUEUE_PASSWORD,
                QueueUtils.QUEUE_LOCATION);

        return factory.createConnection();
    }

    /**
     * Creates a JMS {@link Session} using the passed connection and session mode
     *
     * @param connection  JMS Connection
     * @param sessionMode
     * @return
     * @throws JMSException
     */
    @Override
    public Session createSession(Connection connection, int sessionMode) throws JMSException {
        Session session = connection.createSession(false, sessionMode);
        return session;
    }

    /**
     * Create a JMS {@link Topic} object using the passed JMS {@link Session} and topic name
     *
     * @param session   {@link Session}
     * @param topicName the topic name
     * @return a {@link Topic} object
     * @throws JMSException
     */
    @Override
    public Topic createTopic(Session session, String topicName) throws JMSException {
        Topic topic = session.createTopic(topicName);
        return topic;
    }

    /**
     * Creates a new JMS {@link Destination} using the passed JMS {@link Session} and topic name
     *
     * @param session   {@link Session}
     * @param topicName the topic name
     * @return JMS {@link Destination}
     * @throws JMSException
     */
    @Override
    public Destination createDestination(Session session, String topicName) throws JMSException {
        return session.createTopic(topicName);
    }

    /**
     * Create a {@link MessageConsumer} using the given {@link Session} and {@link Topic} and a subscriber name
     *
     * @param session        {@link Session}
     * @param topic          {@link Topic} object
     * @param subscriberName String subscriber name
     * @return a new {@link MessageConsumer}
     * @throws JMSException
     */
    @Override
    public MessageConsumer createMessageConsumer(Session session, Topic topic, String subscriberName) throws JMSException {
        return session.createDurableSubscriber(topic, subscriberName);
    }


    /**
     * Creates a {@link MessageProducer} which is used to send {@link Message} using
     * the passed {@link Session} and {@link Destination}
     *
     * @param session     {@link Session}
     * @param destination JMS {@link Destination}
     * @return a new {@link MessageProducer}
     * @throws JMSException
     */
    @Override
    public MessageProducer createMessageProducer(Session session, Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    /**
     * Create AdvisoryDestination for a specific topic
     *
     * @param session     {@link Session}
     * @param topicName the topic name
     * @return an advisory destination {@link Destination}
     * @throws JMSException
     */
    @Override
    public Destination createAdvisoryDestination(Session session, String topicName) throws JMSException {
        Destination monitored = session.createTopic(topicName);

        return session.createTopic(
                AdvisorySupport.getConsumerAdvisoryTopic(monitored).getPhysicalName() + "," +
                        AdvisorySupport.getProducerAdvisoryTopic(monitored).getPhysicalName());
    }

    /**
     * Creates an advisory Message Consumer for a specific topic
     *
     * @param session     {@link Session}
     * @param destination {@link Destination}
     * @return an advisory message consumer
     * @throws JMSException
     */
    @Override
    public MessageConsumer createAdvisoryMessageConsumer(Session session, Destination destination) throws JMSException {
        return session.createConsumer(destination);
    }


}
