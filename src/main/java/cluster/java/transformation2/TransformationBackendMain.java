package cluster.java.transformation2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TransformationBackendMain {

  public static void main(String[] args) {

    ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {

      @Override
      public String entityId(Object message) {
        System.out.println("rahul2 receive msg1"  );
        if (message instanceof EntityEnvelope) {
          System.out.println("rahul2 receive msg  " + ((EntityEnvelope) message).id + " msg is " + ((EntityEnvelope) message).id);
          return String.valueOf(((EntityEnvelope) message).id);
        }
        else if (message instanceof Get)
          return String.valueOf(((Get) message).counterId);
        else
          return null;
      }

      @Override
      public Object entityMessage(Object message) {
        System.out.println("rahul2 receive msg2"  );
        if (message instanceof EntityEnvelope) {
          System.out.println("rahul2 receive msg2  " + ((EntityEnvelope) message).payload);
          return ((EntityEnvelope) message).payload;
        }

        else
          return message;
      }

      @Override
      public String shardId(Object message) {
        System.out.println("rahul2 receive ms3 " + message);
        int numberOfShards = 3;
        if (message instanceof EntityEnvelope) {
          long id = ((EntityEnvelope) message).id;
          System.out.println("rahul2 receive msg3  " + String.valueOf(id % numberOfShards));
          return String.valueOf(id % numberOfShards);
        } else if (message instanceof Get) {
          long id = ((Get) message).counterId;
          return String.valueOf(id % numberOfShards);
        } else {
          return null;
        }
      }

    };

/*    //#counter-start
    Option<String> roleOption = Option.none();
    ClusterShardingSettings settings = ClusterShardingSettings.create(system);
    ActorRef startedCounterRegion = akka.cluster.sharding.ClusterSharding.get(system).start("Counter",
            Props.create(Counter.class), settings, messageExtractor);
*/

    // Override the configuration of the port when specified as program argument
    final String port = args.length > 0 ? args[0] : "0";
    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
            withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
            withFallback(ConfigFactory.load());


    System.out.println("creating akka system with port " + port);
    ActorSystem system = ActorSystem.create("ClusterSystem", config);
    ClusterShardingSettings settings = ClusterShardingSettings.create(system);
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    ActorRef startedCounterRegion = akka.cluster.sharding.ClusterSharding.get(system).start("Counter",
            Props.create(TransformationBackend.class), settings, messageExtractor);
    System.out.println("rahul actor path is " + startedCounterRegion.path());
    ActorRef counterRegion = akka.cluster.sharding.ClusterSharding.get(system).shardRegion("Counter");
    ClusterClientReceptionist.get(system).registerService(counterRegion);

  }

}
