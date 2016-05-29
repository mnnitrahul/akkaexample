package cluster.java.transformation;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

import java.util.ArrayList;
import java.util.List;


//#frontend
public class TransformationFrontend extends UntypedActor {

  List<ActorRef> backends = new ArrayList<ActorRef>();
  int jobCounter = 0;

  @Override
  public void onReceive(Object message) {
    if ((message instanceof TransformationMessages.TransformationJob) && backends.isEmpty()) {
      TransformationMessages.TransformationJob job = (TransformationMessages.TransformationJob) message;
      getSender().tell(
          new TransformationMessages.JobFailed("Service unavailable, try again later", job),
          getSender());

    } else if (message instanceof TransformationMessages.TransformationJob) {
      TransformationMessages.TransformationJob job = (TransformationMessages.TransformationJob) message;
      jobCounter++;
      backends.get(jobCounter % backends.size())
          .forward(job, getContext());

    } else if (message instanceof TransformationMessages.BackendRegistration) {
      TransformationMessages.BackendRegistration registration = (TransformationMessages.BackendRegistration) message;
      System.out.println("rahul registering backend" + registration);
      getContext().watch(getSender());
      System.out.println("rahul sender message is " + registration.getCampaignId());
      System.out.println("rahul sender path is " + getSender().path());
      backends.add(getSender());

    } else if (message instanceof Terminated) {
      Terminated terminated = (Terminated) message;
      backends.remove(terminated.getActor());

    } else {
      unhandled(message);
    }
  }

}
//#frontend
