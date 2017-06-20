package cn.zhanyongzhi.blog.zookeeper;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface LeaderElection {
    void setLeaderElectionListener(Runnable runnable);

    void start() throws IOException, InterruptedException, KeeperException;

    void stop();

    boolean isMaster();
}
