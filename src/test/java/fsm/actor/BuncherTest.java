package fsm.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import fsm.messages.Batch;
import fsm.messages.Flush;
import fsm.messages.Queue;
import fsm.messages.SetTarget;
import fsm.state.States;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by rahul.ka on 04/05/16.
 */
public class BuncherTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("BuncherTest");
    }

    @AfterClass
    public static void tearDown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testBuncherActorDoesntBatchUninitialized() throws InterruptedException {
        new JavaTestKit(system) {{
            final ActorRef buncher =
                    system.actorOf(Props.create(Buncher.class));
            Thread.sleep(1000*2);
            System.out.println("Test wake");
            final ActorRef probe = getRef();
            buncher.tell(new SetTarget(probe), probe);
            expectMsgEquals(States.Idle);
            buncher.tell(new Queue(42), probe);
            expectMsgEquals(States.Active);
            buncher.tell(Flush.Flush, probe);
            expectMsgEquals(States.Idle);
            expectMsgClass(Batch.class);
            buncher.tell(new SetTarget(probe), probe);
            expectMsgEquals(States.Idle);
            Thread.sleep(1000*2);
            system.stop(buncher);
        }};
    }
}