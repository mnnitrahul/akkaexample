package snapshot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class SnapshotExample2 {

    public static void main(String... args) throws Exception {
        final ActorSystem system = ActorSystem.create("example2");
        final ActorRef persistentActor = system.actorOf(Props.create(ExamplePersistentActor.class), "persistentActor-1-java");
        Thread.sleep(2000);
        System.out.println("telling e");
        persistentActor.tell("e", null);
        System.out.println("telling f");
        persistentActor.tell("f", null);
        System.out.println("telling snap");
        persistentActor.tell("snap", null);
        Thread.sleep(1000);
        System.out.println("telling print");
        persistentActor.tell("print", null);

        Thread.sleep(5000);
        system.terminate();
    }
}
