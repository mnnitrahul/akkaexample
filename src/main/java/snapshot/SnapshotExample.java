package snapshot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class SnapshotExample {


    public static void main(String... args) throws Exception {
        final ActorSystem system = ActorSystem.create("example2");
        final ActorRef persistentActor = system.actorOf(Props.create(ExamplePersistentActor.class), "persistentActor-1-java");
        System.out.println("actor path is " + persistentActor.path());
        Thread.sleep(7000);
        System.out.println("telling a");
        persistentActor.tell("a", null);
        Thread.sleep(2000);
        System.out.println("telling snap");
        persistentActor.tell("snap", null);
        System.out.println("telling d");
        persistentActor.tell("d", null);
        Thread.sleep(2000);
        System.out.println("telling print");
        persistentActor.tell("print", null);

        Thread.sleep(3000);
        system.terminate();
    }
}
