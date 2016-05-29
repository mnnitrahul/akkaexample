package fsm.actor;

import akka.actor.AbstractLoggingFSM;
import akka.japi.pf.UnitMatch;
import fsm.data.Data;
import fsm.data.Todo;
import fsm.data.Uninitialized;
import fsm.messages.Batch;
import fsm.messages.Flush;
import fsm.messages.Queue;
import fsm.messages.SetTarget;
import fsm.state.States;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by rahul.ka on 04/05/16.
 */

public class Buncher extends AbstractLoggingFSM<States, Data> {
    {
        System.out.println("Creating Actor");
        startWith(States.Idle, Uninitialized.Uninitialized);

        when(States.Idle,
                matchEvent(SetTarget.class, Uninitialized.class,
                        (setTarget, uninitialized) -> {
                            System.out.println("in Idle");
                            return stay().using(new Todo(setTarget.getRef(), new LinkedList<Object>())).replying(States.Idle);
                        }));

        when(States.Idle,
                matchEvent(SetTarget.class, Todo.class,
                        (setTarget, todo) -> {
                            System.out.println("in Idle Todo");
                            return stay().using(new Todo(setTarget.getRef(), new LinkedList<Object>())).replying(States.Idle);
                        }));


        // transition elided ...

        when(States.Active, Duration.create(100, "second"),
                matchEvent(Arrays.asList(Flush.class, StateTimeout()), Todo.class,
                        (event, todo) -> {
                            System.out.println("In Active");
                            return goTo(States.Idle).using(todo.copy(new LinkedList<>())).replying(States.Idle);
                        }));

        whenUnhandled(
                matchEvent(Queue.class, Todo.class,
                        (queue, todo) -> goTo(States.Active).using(todo.addElement(queue.getObj())).replying(States.Active)).
                        anyEvent((event, state) -> {
                            System.out.println("receive unhandled message");
                            log().warning("received unhandled request {} in state {}/{}",
                                    event, stateName(), state);
                            return stay();
                        }));


        onTransition(
                matchState(States.Active, States.Idle, () -> {
                    // reuse this matcher
                    System.out.println("state name is " + stateName());
                    final UnitMatch<Data> m = UnitMatch.create(
                            matchData(Todo.class,
                                    todo -> todo.getTarget().tell(new Batch(todo.getQueue()), self())));
                    m.match(stateData());
                }).state(States.Idle, States.Active, () -> {
                    System.out.println("Moving from Idle to Active");
                }));

        initialize();
    }


}
