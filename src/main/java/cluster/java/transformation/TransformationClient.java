package cluster.java.transformation;

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
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TransformationClient {

  public static void main(String[] args) throws InterruptedException {
    // Override the configuration of the port when specified as program argument
   /* final String port = args.length > 0 ? args[0] : "0";
    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
            withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
            withFallback(ConfigFactory.load());
*/
    ActorSystem system = ActorSystem.create("ClusterSystemFrontend");
    final ActorRef frontend = system.actorOf(ClusterClient.props(
            ClusterClientSettings.create(system).withInitialContacts(initialContacts())),
            "client");
    System.out.println("telling cluster");
/*    frontend.tell(new ClusterClient.Send("/user/campaign1", new TransformationMessages.TransformationJob("msg6")), ActorRef.noSender());
    frontend.tell(new ClusterClient.SendToAll("/user/campaign2", new TransformationMessages.TransformationJob("msg6")), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign4", new TransformationMessages.TransformationJob("msg6")), ActorRef.noSender());
/*    frontend.tell(new ClusterClient.SendToAll("/user/campaign3", new TransformationMessages.TransformationJob("msg5")), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign5", new TransformationMessages.TransformationJob("msg5")), ActorRef.noSender());
/*    frontend.tell(new ClusterClient.SendToAll("/user/campaign4", new TransformationMessages.TransformationJob("msg1")), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign3", new TransformationMessages.TransformationJob("msg2")), ActorRef.noSender());
    frontend.tell(new ClusterClient.SendToAll("/user/campaign2", new TransformationMessages.TransformationJob("msg2")), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign4", new TransformationMessages.TransformationJob("msg2")), ActorRef.noSender());
    frontend.tell(new ClusterClient.SendToAll("/user/campaign1", new TransformationMessages.TransformationJob("msg2")), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign2", new TransformationMessages.TransformationJob("msg3")), ActorRef.noSender());
    frontend.tell(new ClusterClient.SendToAll("/user/campaign1", new TransformationMessages.TransformationJob("msg3")), ActorRef.noSender());
    System.out.println("sent msg to all campaigns");
*/

    Random random = new Random();
    String actorPrefix = "/user/campaign";
    String msgPrefix = "msg-";
    String[] actorPath = new String[6];
    int[] count = new int[6];
    for (int i = 0; i < 6; ++i) {
      count[i] = 0;
      actorPath[i] = "/user/campaign" + (i+1);
    }
    frontend.tell(new ClusterClient.Send("/user/campaign1", "print"), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign2", "print"), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign3", "print"), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign4", "print"), ActorRef.noSender());
    frontend.tell(new ClusterClient.Send("/user/campaign5", "print"), ActorRef.noSender());
    final FiniteDuration interval = Duration.create(2, TimeUnit.SECONDS);
    final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
    final ExecutionContext ec = system.dispatcher();
    system.scheduler().schedule(interval, interval, new Runnable() {
      public void run() {
        int id = random.nextInt(Integer.SIZE - 1)%5;
        count[id]++;
        System.out.println("sending msg " + msgPrefix + count[id] + " to user path " + actorPath[id]);
        Future<Object> future = Patterns.ask(frontend, new ClusterClient.Send(actorPath[id], new TransformationMessages.TransformationJob(msgPrefix + count[id])), timeout);
        future.onSuccess(new OnSuccess<Object>() {
          public void onSuccess(Object result) {
            System.out.println(result);
          }
        }, ec);
        future.onFailure(new OnFailure() {
          @Override
          public void onFailure(Throwable throwable) throws Throwable {
            System.out.println(actorPath[id] + " " + throwable.getCause() + " " + throwable.getMessage());
          }
        }, ec);

/*        Patterns.ask(frontend, new ClusterClient.Send(actorPath[id], new TransformationMessages.TransformationJob(msgPrefix + count[id])),
                timeout).onSuccess(new OnSuccess<Object>() {
          public void onSuccess(Object result) {
            System.out.println(result);
          }
        }, ec) ;*/
        if (count[id]%4 == 0) {
          System.out.println("getting print for campaign " + actorPath[id]);
          Patterns.ask(frontend, new ClusterClient.Send(actorPath[id], "print"),
                  timeout).onSuccess(new OnSuccess<Object>() {
            public void onSuccess(Object result) {
              System.out.println("result  for campaign " + actorPath[id] + " is " + result);
            }
          }, ec);
        }

      }

    }, ec);

    Thread.sleep(500000);
    system.terminate();
  }

  static Set<ActorPath> initialContacts() {
    return new HashSet<>(Arrays.asList(
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/system/receptionist"),
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2552/system/receptionist")));
  }
}
