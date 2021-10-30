package service;

import java.util.List;

/**
 * Class which is used to retrieve information from the ActiveMQ Broker
 */
public interface ActiveMQBrokerInfoRetriever {

    public List<String> getTopics();
}