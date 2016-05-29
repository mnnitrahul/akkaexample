/**
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */

package cluster.java.sharding;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Option;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;

// Doc code, compile only
public class ClusterShardingExample {

  ActorSystem system = null;

  ActorRef getSelf() {
    return null;
  }

  public static void main(String[] args) throws InterruptedException {
    ClusterShardingExample example = new ClusterShardingExample();
    example.demonstrateUsage();
  }

  public void demonstrateUsage() throws InterruptedException {
    //#counter-extractor
    ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {

      @Override
      public String entityId(Object message) {
        System.out.println("entityId object " + message);
        if (message instanceof EntityEnvelope)
          return String.valueOf(((EntityEnvelope) message).id);
        else if (message instanceof Get)
          return String.valueOf(((Get) message).counterId);
        else
          return null;
      }

      @Override
      public Object entityMessage(Object message) {
        System.out.println("entityId message " + message);
        if (message instanceof EntityEnvelope) {
          System.out.println("entityId message envelop" + ((EntityEnvelope) message).payload);
          return ((EntityEnvelope) message).payload;
        }
        else
          return message;
      }

      @Override
      public String shardId(Object message) {
        System.out.println("ShardId object " + message);
        int numberOfShards = 100;
        if (message instanceof EntityEnvelope) {
          long id = ((EntityEnvelope) message).id;
          return String.valueOf(id % numberOfShards);
        } else if (message instanceof Get) {
          long id = ((Get) message).counterId;
          return String.valueOf(id % numberOfShards);
        } else {
          return null;
        }
      }

    };
    //#counter-extractor

    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 2551).
            withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
            withFallback(ConfigFactory.load());
    System.out.println("creating akka system");
    ActorSystem system = ActorSystem.create("ClusterSystem", config);
    //#counter-start
    Option<String> roleOption = Option.none();
    System.out.println("creating akka system setting");
    ClusterShardingSettings settings = ClusterShardingSettings.create(system);
    System.out.println("start counter region");
    Thread.sleep(5000);
    ActorRef startedCounterRegion = akka.cluster.sharding.ClusterSharding.get(system).start("Counter",
      Props.create(Counter.class), settings, messageExtractor);
    System.out.println("rahul actor path is " + startedCounterRegion.path());
    //#counter-start
    System.out.println("get akka system");
    //#counter-usage
    ActorRef counterRegion = akka.cluster.sharding.ClusterSharding.get(system).shardRegion("Counter");
    ClusterClientReceptionist.get(system).registerService(counterRegion);



    System.out.println("Telling0");
    counterRegion.tell(new EntityEnvelope(1234,
            Counter.CounterOp.INCREMENT), getSelf());
    System.out.println("Telling");
    counterRegion.tell(new EntityEnvelope(123,
            Counter.CounterOp.INCREMENT), getSelf());
    System.out.println("Telling2");
    counterRegion.tell(new EntityEnvelope(123,
        Counter.CounterOp.INCREMENT), getSelf());
    System.out.println("Telling3");
    counterRegion.tell(new EntityEnvelope(234,
            Counter.CounterOp.INCREMENT), getSelf());
    System.out.println("Telling4");
    counterRegion.tell(new EntityEnvelope(234,
            Counter.CounterOp.INCREMENT), getSelf());
    //#counter-usage
    System.out.println("sleeping");
    Thread.sleep(100000);
    System.out.println("sleeping over");
    //#counter-supervisor-start
    akka.cluster.sharding.ClusterSharding.get(system).start("SupervisedCounter",
        Props.create(CounterSupervisor.class), settings, messageExtractor);
    //#counter-supervisor-start
  }


  static//#graceful-shutdown
  public class IllustrateGracefulShutdown extends AbstractActor {

    public IllustrateGracefulShutdown() {
      final ActorSystem system = context().system();
      final Cluster cluster = Cluster.get(system);
      final ActorRef region = akka.cluster.sharding.ClusterSharding.get(system).shardRegion("Entity");

      receive(ReceiveBuilder.
        match(String.class, s -> s.equals("leave"), s -> {
          context().watch(region);
          region.tell(ShardRegion.gracefulShutdownInstance(), self());
        }).
        match(Terminated.class, t -> t.actor().equals(region), t -> {
          cluster.registerOnMemberRemoved(() ->
            self().tell("member-removed", self()));
          cluster.leave(cluster.selfAddress());
        }).
        match(String.class, s -> s.equals("member-removed"), s -> {
          // Let singletons hand over gracefully before stopping the system
          context().system().scheduler().scheduleOnce(Duration.create(10, SECONDS),
              self(), "stop-system", context().dispatcher(), self());
        }).
        match(String.class, s -> s.equals("stop-system"), s -> {
          system.terminate();
        }).
        build());
    }
  }
  //#graceful-shutdown

  static//#supervisor
  public class CounterSupervisor extends UntypedActor {

    private final ActorRef counter = getContext().actorOf(
        Props.create(Counter.class), "theCounter");

    private static final SupervisorStrategy strategy =
      new OneForOneStrategy(DeciderBuilder.
        match(IllegalArgumentException.class, e -> SupervisorStrategy.resume()).
        match(ActorInitializationException.class, e -> SupervisorStrategy.stop()).
        match(Exception.class, e -> SupervisorStrategy.restart()).
        matchAny(o -> SupervisorStrategy.escalate()).build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }

    @Override
    public void onReceive(Object msg) {
      counter.forward(msg, getContext());
    }
  }
  //#supervisor

}
