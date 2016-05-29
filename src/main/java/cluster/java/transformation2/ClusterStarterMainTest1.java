package cluster.java.transformation2;

/**
 * Created by rahul.ka on 23/05/16.
 */
public class ClusterStarterMainTest1 {

    public static void main(String[] args) throws InterruptedException {
        // starting 2 frontend nodes and 3 backend nodes
        ClusterStarter starter = new ClusterStarter();
        starter.startCluster(2555);
    }

}