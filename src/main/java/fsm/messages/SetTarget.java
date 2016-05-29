package fsm.messages;

import akka.actor.ActorRef;

/**
 * Created by rahul.ka on 04/05/16.
 */
public final class SetTarget {
    private final ActorRef ref;

    public SetTarget(ActorRef ref) {
        this.ref = ref;
    }

    public ActorRef getRef() {
        return ref;
    }
    // boilerplate ...
}
