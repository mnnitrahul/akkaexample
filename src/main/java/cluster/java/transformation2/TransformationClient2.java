package cluster.java.transformation2;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TransformationClient2 {

  public static void main(String[] args) throws InterruptedException {
    ActorSystem system = ActorSystem.create("ClusterSystemFrontend");
    final ActorRef frontend = system.actorOf(ClusterClient.props(
            ClusterClientSettings.create(system).withInitialContacts(initialContacts())),
            "client");
    System.out.println("telling cluster");

    Random random = new Random();
    String[] actorPath = new String[6];
    String msgPrefix = "msg-";
    int count = 0;

    final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
    final ExecutionContext ec = system.dispatcher();
    EntityEnvelope envelope = new EntityEnvelope(6, new TransformationJob(msgPrefix + count));
    Future<Object> future = Patterns.ask(frontend, new ClusterClient.Send("/system/sharding/Counter", envelope), timeout);
    future.onSuccess(new OnSuccess<Object>() {
      public void onSuccess(Object result) {
        System.out.println(result);
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
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/system/receptionist"),
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2552/system/receptionist")));
  }
}
