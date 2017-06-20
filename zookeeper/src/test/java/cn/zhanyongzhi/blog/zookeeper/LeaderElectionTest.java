package cn.zhanyongzhi.blog.zookeeper;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class LeaderElectionTest {
    TestingServer zkServer;

    @BeforeClass
    public void setup() throws Exception {
        zkServer = new TestingServer(2181, true);
        zkServer.start();
    }

    @AfterClass
    public void tearDown() throws IOException {
        zkServer.close();
    }

    @Test
    public void testDefault() throws InterruptedException, KeeperException, IOException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zkClient = new ZooKeeper("127.0.0.1:2181", 4000, event -> {
            if(event.getState() == Watcher.Event.KeeperState.SyncConnected)
                latch.countDown();
        });

        latch.await();

        if(null != zkClient.exists("/cn", false))
            zkClient.delete("/cn", -1);

        zkClient.create("/cn", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zkClient.create("/cn/zhanyongzhi", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zkClient.create("/cn/zhanyongzhi/leaderelection", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        LeaderElection leaderElection = new ZKLeaderElection("127.0.0.1:2181", "/cn/zhanyongzhi/leaderelection");

        leaderElection.start();

        TimeUnit.SECONDS.sleep(3);


        leaderElection.stop();
    }

}