package cn.zhanyongzhi.blog.zookeeper;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZKLeaderElection implements LeaderElection {
    private String leaderNodePath;
    private String seqNodePath;

    private String connectString;

    private volatile ZooKeeper zkCurrentClient;

    Runnable electionListener;

    Thread leaderCheckThread;

    public ZKLeaderElection(String connectString, String leaderNodePath) {
        this.leaderNodePath = leaderNodePath;
        this.connectString = connectString;
    }

    @Override
    public void start() {
        leaderCheckThread = new Thread(new Runnable() {
            Object lock = new Object();

            @Override
            public void run() {
                while(true) {
                    ZooKeeper zkClient = getZkClient();

                    try {
                        //创建候选人
                        seqNodePath = zkClient.create(leaderNodePath + "/candidate", "".getBytes(),
                                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

                        if(true == checkIsLeader(zkClient, seqNodePath)){
                            electionListener.run();
                        }

                        //watch the min node in the path, if node not exists throw exception
//                        zkClient.getData(firstNodePath, event -> {
//                            if(event.getType() == Watcher.Event.EventType.NodeDeleted){
//                                lock.notify();
//                            }
//                        }, null);

                        TimeUnit.SECONDS.timedWait(lock, 30);

                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e){
                    }

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        break;
                    }

                }
            }
        });

        leaderCheckThread.start();
    }


    private boolean checkIsLeader(ZooKeeper zkClient, String node) throws KeeperException, InterruptedException {

        List<String> childrenList = zkClient.getChildren(node, null);

        childrenList.sort((o1, o2) -> o1.compareTo(o2));

        if(childrenList.isEmpty()){
            return false;
        }

        //判断当前是否为leader
        String firstNodePath = childrenList.get(0);
        if(!firstNodePath.equals(seqNodePath)){
            return false;
        }

        //current is leader
        return true;
    }

    @Override
    public void stop(){

    }

    private ZooKeeper connect() throws IOException, InterruptedException, KeeperException.OperationTimeoutException {
        int sessionTimeout = (int) TimeUnit.SECONDS.toMillis(10);

        final CountDownLatch connectLatch = new CountDownLatch(1);
        ZooKeeper zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher(){
            @Override
            public void process(WatchedEvent event) {
                Event.KeeperState state = event.getState();
                if(Event.KeeperState.SyncConnected == state)
                    connectLatch.countDown();
            }
        });

        if(false == connectLatch.await(5, TimeUnit.SECONDS)){
            throw new KeeperException.OperationTimeoutException();
        }

        return zkClient;
    }

    private ZooKeeper getZkClient(){
        if(null == zkCurrentClient || zkCurrentClient.getState().isAlive()) {
            synchronized (this) {
                if(null == zkCurrentClient || !zkCurrentClient.getState().isAlive()) {
                    try {
                        zkCurrentClient = connect();
                    }catch (Exception e){
                        return null;
                    }
                }
            }
        }

        return zkCurrentClient;
    }

    @Override
    public void setLeaderElectionListener(Runnable electionListener){
        this.electionListener = electionListener;
    }

    @Override
    public boolean isMaster() {
        return false;
    }
}
