package service.broker_info;

import java.util.List;

/**
 * Class which is used to retrieve information from the ActiveMQ Broker
 */
public interface ActiveMQBrokerInfoRetriever {

    List<String> getTopics();
}
