package cluster.java.transformation2;

import akka.actor.DeadLetter;
import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.SaveSnapshotFailure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import akka.persistence.UntypedPersistentActor;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;


//#backend
public class TransformationBackend extends UntypedPersistentActor {

  private CampaignState campaignState;
  private String campaignId ;
  TransformationBackend() {
    campaignState = new CampaignState();
    campaignId = "Campaign-" + getSelf().path().name();
  }


  Cluster cluster = Cluster.get(getContext().system());

  //subscribe to cluster changes, MemberUp
  @Override
  public void preStart() throws Exception {
    super.preStart();
    cluster.subscribe(getSelf(), MemberUp.class);
    context().setReceiveTimeout(Duration.create(120, SECONDS));
  }

  //re-subscribe when restart
  @Override
  public void postStop() {
    cluster.unsubscribe(getSelf());
  }

  @Override
  public String persistenceId() { return campaignId; }

  @Override
  public void onReceiveCommand(Object message) {


    if (message instanceof TransformationJob) {
      TransformationJob job = (TransformationJob) message;

      persist(job, new Procedure<TransformationJob>() {
        public void apply(TransformationJob evt) throws Exception {
          System.out.println("rahul receive msg " + ((TransformationJob) message).getText() + " for campaign Id " + campaignId);
          campaignState.update(((TransformationJob) message).getText());
          System.out.println("rahul4 Campaign state for campaign Id " + campaignId + " is " + campaignState.toString());
          saveSnapshot(campaignState);
          getSender().tell(new TransformationMessages.TransformationResult(job.getText().toUpperCase())+campaignId,
                  getSelf());
        }
      });
    } else if (message instanceof CurrentClusterState) {
      CurrentClusterState state = (CurrentClusterState) message;
      for (Member member : state.getMembers()) {
        if (member.status().equals(MemberStatus.up())) {
        }
      }
    } else if (message instanceof MemberUp) {
      MemberUp mUp = (MemberUp) message;

    } else if (message instanceof DeadLetter) {
      DeadLetter deadLetter = (DeadLetter) message;
      System.out.println("rahul deadletter receive " + deadLetter.message().toString());

    } else if (message.equals("print")) {
      System.out.println("current state for campaign " + campaignId + "= " + campaignState);
      getSender().tell(campaignState.toString(), getSelf());

    } else if (message instanceof SaveSnapshotSuccess) {
      System.out.println("rahul savesnapshot success");
      // ...
    } else if (message instanceof SaveSnapshotFailure) {
      System.out.println("savesnapshot fail");
      // ...
    } else if (message.equals(ReceiveTimeout.getInstance())) {
      System.out.println("rahul3 passivating " + campaignId);
      getContext().parent().tell(
              new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
    }


    else {
      unhandled(message);
    }
  }

  @Override
  public void onReceiveRecover(Object message) {
    System.out.println("running onRecover");
    if (message instanceof SnapshotOffer) {
      CampaignState s = (CampaignState) ((SnapshotOffer)message).snapshot();
      System.out.println("offered state = " + s);
      campaignState = s;
    } else if (message instanceof String) {
      campaignState.update((String) message);
      System.out.println("msg is " +message);
    } else {
      unhandled(message);
    }
  }
}
//#backend
