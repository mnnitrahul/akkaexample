package cluster.java.transformation2;

import akka.actor.*;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.cluster.sharding.ShardRegion;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ManagerClient {

  public static void main(String[] args) throws InterruptedException {
    ActorSystem system = ActorSystem.create("ClusterSystemFrontend");
    final ActorRef frontend = system.actorOf(ClusterClient.props(
            ClusterClientSettings.create(system).withInitialContacts(initialContacts())),
            "client");
    System.out.println("telling cluster");

    final Timeout timeout = new Timeout(Duration.create(250, TimeUnit.SECONDS));
    final ExecutionContext ec = system.dispatcher();
    Future<Object> future = Patterns.ask(frontend, new ClusterClient.Send("/system/sharding/Counter", new ShardRegion.GetClusterShardingStats(FiniteDuration.fromNanos(1000000))), timeout);
    future.onSuccess(new OnSuccess<Object>() {
      public void onSuccess(Object result) {

        ShardRegion.ClusterShardingStats stats = (ShardRegion.ClusterShardingStats) result;
        for (Map.Entry<Address, ShardRegion.ShardRegionStats> statsEntry : stats.getRegions().entrySet() ) {
          System.out.println("address " + statsEntry.getKey() + " region " + statsEntry.getValue());
        }
      }
    }, ec);
    future.onFailure(new OnFailure() {
      @Override
      public void onFailure(Throwable throwable) throws Throwable {
        System.out.println(throwable.getCause() + " " + throwable.getMessage());
      }
    }, ec);
    Thread.sleep(5000);
    system.terminate();
  }

  static Set<ActorPath> initialContacts() {
    return new HashSet<>(Arrays.asList(
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:12551/system/receptionist"),
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2555/system/receptionist"),
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2552/system/receptionist")));
  }
}
