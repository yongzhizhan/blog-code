package cn.zhanyongzhi.blog.qpid;

import org.apache.qpid.client.AMQAnyDestination;
import org.apache.qpid.client.AMQConnection;
import org.apache.qpid.client.message.JMSTextMessage;
import org.apache.qpid.jms.Session;
import org.junit.Assert;
import org.testng.annotations.Test;

import javax.jms.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class TestBase {
    @Test
    public void testDefault() throws Exception {
        EmbeddedAMQPBroker embeddedAMQPBroker = new EmbeddedAMQPBroker();
        embeddedAMQPBroker.start();

        String host = "localhost";
        String queueName = "test";

        AMQConnection connection = new AMQConnection(host, EmbeddedAMQPBroker.BROKER_PORT, "admin", "admin",  null, "/default");
        connection.setExceptionListener(new ExceptionListener(){
            @Override
            public void onException(JMSException exception) {
                System.out.println("exception: " + exception.getMessage());
            }
        });

        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //create queue
        //AMQAnyDestination recvDestination = new AMQAnyDestination(String.format("ADDR:%s; {create: always}", queueName));
        String recvRouteKey = "";
        String recvBindKey = "recvbindkey";

        AMQAnyDestination recvDestination = new AMQAnyDestination("amp.topic", "topic", recvRouteKey,
                true, false, queueName, true, new String[]{recvBindKey});

        String[] bindingKeys = recvDestination.getBindingKeys();

        MessageConsumer consumer = session.createConsumer(recvDestination);

        final CountDownLatch latch = new CountDownLatch(1);
        consumer.setMessageListener(new MessageListener(){
            @Override
            public void onMessage(Message message) {
                try {
                    System.out.println(message.getStringProperty("qpid.subject"));

                    JMSTextMessage textMessage = (JMSTextMessage)message;
                    System.out.println(textMessage.getText());

                    latch.countDown();

                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });

        //send message
        String message = "test-message";
        String subject = "test";

        //route key equal queueName
        //AMQAnyDestination sendDestination = new AMQAnyDestination(String.format("ADDR:%s; {create: always}", "sender-" + queueName));
        //Destination sendDestination = new AMQTopic("amq.topic", queueName);
        AMQAnyDestination sendDestination = new AMQAnyDestination("amp.topic", "topic", recvBindKey,
                true, false, null, true, new String[]{});

        MessageProducer producer = session.createProducer(sendDestination);

        TextMessage textMessage = session.createTextMessage(message);
        textMessage.setStringProperty("qpid.subject", subject);

        producer.send(textMessage);

        if(false == latch.await(3, TimeUnit.SECONDS)){
            Assert.fail("wait timeout");
        }

        producer.close();
    }
}
