package cn.zhanyongzhi.blog.qpid;


import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;

import com.google.common.io.Files;

/**
 * We shouldn't need external things for testing
 */
public class EmbeddedAMQPBroker {
    public static final int BROKER_PORT = 2345;

    private final Broker broker = new Broker();
    private final BrokerOptions brokerOptions = new BrokerOptions();

    public EmbeddedAMQPBroker() {
        final String configFileName = "/qpid-config.json";

        // prepare options
        brokerOptions.setConfigProperty("broker.name", "embedded-broker");
        brokerOptions.setConfigProperty("qpid.amqp_port", String.valueOf(BROKER_PORT));
        brokerOptions.setConfigProperty("qpid.work_dir", Files.createTempDir().getAbsolutePath());
        brokerOptions.setInitialConfigurationLocation(getClass().getResource(configFileName).toString());

    }

    public void start() throws Exception {
        broker.startup(brokerOptions);
    }

    public void stop() {
        broker.shutdown();
    }
}