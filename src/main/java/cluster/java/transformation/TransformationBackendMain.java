package cluster.java.transformation;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClientReceptionist;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TransformationBackendMain {

  public static void main(String[] args) {
    // Override the configuration of the port when specified as program argument
    final String port = args.length > 0 ? args[0] : "0";
    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
            withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
            withFallback(ConfigFactory.load());

    ActorSystem system = ActorSystem.create("ClusterSystem", config);
    for (int i = 1; i < args.length; ++i) {
      final String campaignId =  args[i];
      ActorRef actorRef = system.actorOf(TransformationBackend.props(campaignId), campaignId);
      ClusterClientReceptionist.get(system).registerService(actorRef);
    }
  }

}
