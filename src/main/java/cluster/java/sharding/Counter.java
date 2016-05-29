package cluster.java.sharding;

import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by rahul.ka on 13/05/16.
 */

//#counter-actor
public class Counter extends UntypedPersistentActor {

    public static enum CounterOp {
        INCREMENT, DECREMENT
    }



    int count = 0;

    // getSelf().path().name() is the entity identifier (utf-8 URL-encoded)
    @Override
    public String persistenceId() {
        return "Counter-" + getSelf().path().name();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        context().setReceiveTimeout(Duration.create(120, SECONDS));
    }

    void updateState(CounterChanged event) {
        count += event.delta;
    }

    @Override
    public void onReceiveRecover(Object msg) {
        if (msg instanceof CounterChanged)
            updateState((CounterChanged) msg);
        else
            unhandled(msg);
    }

    @Override
    public void onReceiveCommand(Object msg) {
        if (msg instanceof Get)
            getSender().tell(count, getSelf());

        else if (msg == CounterOp.INCREMENT)
            persist(new CounterChanged(+1), new Procedure<CounterChanged>() {
                public void apply(CounterChanged evt) {
                    updateState(evt);
                    System.out.println("count value for id " + getSelf()+ " is " + count);
                }
            });

        else if (msg == CounterOp.DECREMENT)
            persist(new CounterChanged(-1), new Procedure<CounterChanged>() {
                public void apply(CounterChanged evt) {
                    updateState(evt);
                    System.out.println("count value for id " + getSelf()+ " is " + count);
                }
            });

        else if (msg.equals(ReceiveTimeout.getInstance()))
            getContext().parent().tell(
                    new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());

        else
            unhandled(msg);
    }
}