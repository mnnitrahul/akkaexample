package snapshot;

import akka.japi.Procedure;
import akka.persistence.SaveSnapshotFailure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import akka.persistence.UntypedPersistentActor;

/**
 * Created by rahul.ka on 09/05/16.
 */
public class ExamplePersistentActor extends UntypedPersistentActor {
    @Override
    public String persistenceId() { return "sample-id-2"; }

    private ExampleState state = new ExampleState();

    @Override
    public void onReceiveCommand(Object message) {
        if (message.equals("print")) {
            System.out.println("current state = " + state);
        } else if (message.equals("snap")) {
            // IMPORTANT: create a copy of snapshot
            // because ExampleState is mutable !!!
            saveSnapshot(state.copy());
        } else if (message instanceof SaveSnapshotSuccess) {
            System.out.println("rahul savesnapshot success");
            // ...
        } else if (message instanceof SaveSnapshotFailure) {
            System.out.println("savesnapshot fail");
            // ...
        } else if (message instanceof String) {
            String s = (String) message;
            persist(s, new Procedure<String>() {
                public void apply(String evt) throws Exception {
                    System.out.println("rahul receive msg " + evt);
                    state.update(evt);
                }
            });
        } else {
            unhandled(message);
        }
    }

    @Override
    public void onReceiveRecover(Object message) {
        System.out.println("running onRecover");
        if (message instanceof SnapshotOffer) {
            ExampleState s = (ExampleState)((SnapshotOffer)message).snapshot();
            System.out.println("offered state = " + s);
            state = s;
        } else if (message instanceof String) {
            state.update((String) message);
            System.out.println("msg is " +message);
        } else {
            unhandled(message);
        }
    }

}

