package fsm.data;

import akka.actor.ActorRef;

import java.util.List;

/**
 * Created by rahul.ka on 04/05/16.
 */
final public class Todo implements Data {
    private final ActorRef target;
    private final List<Object> queue;

    public Todo(ActorRef target, List<Object> queue) {
        this.target = target;
        this.queue = queue;
    }

    public Todo copy(List<Object> queue) {
        queue.addAll(queue);
        return this;
    }

    public Todo addElement(Object object) {
        queue.add(object);
        return this;
    }

    public ActorRef getTarget() {
        return target;
    }

    public List<Object> getQueue() {
        return queue;
    }
// boilerplate ...
}
