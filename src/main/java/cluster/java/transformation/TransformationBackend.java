package cluster.java.transformation;

import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.japi.Creator;
import akka.japi.Procedure;
import akka.persistence.SaveSnapshotFailure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import akka.persistence.UntypedPersistentActor;


//#backend
public class TransformationBackend extends UntypedPersistentActor {

  private String campaignId;
  private CampaignState campaignState;

  TransformationBackend(String campaignId) {
    this.campaignId = campaignId;
    campaignState = new CampaignState();
  }

  public static Props props(String campaignId) {
    return Props.create(new Creator<TransformationBackend>() {
      private static final long serialVersionUID = 1L;

      @Override
      public TransformationBackend create() throws Exception {
        return new TransformationBackend(campaignId);
      }
    });
  }

  Cluster cluster = Cluster.get(getContext().system());

  //subscribe to cluster changes, MemberUp
  @Override
  public void preStart() {
    cluster.subscribe(getSelf(), MemberUp.class);
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

    if (message instanceof TransformationMessages.TransformationJob) {
      TransformationMessages.TransformationJob job = (TransformationMessages.TransformationJob) message;

      persist(job, new Procedure<TransformationMessages.TransformationJob>() {
        public void apply(TransformationMessages.TransformationJob evt) throws Exception {
          System.out.println("rahul receive msg " + ((TransformationMessages.TransformationJob) message).getText() + " for campaign Id " + campaignId);
          campaignState.update(((TransformationMessages.TransformationJob) message).getText());
          saveSnapshot(campaignState);
          getSender().tell(new TransformationMessages.TransformationResult(job.getText().toUpperCase())+campaignId,
                  getSelf());
        }
      });
    } else if (message instanceof CurrentClusterState) {
      CurrentClusterState state = (CurrentClusterState) message;
      for (Member member : state.getMembers()) {
        if (member.status().equals(MemberStatus.up())) {
          register(member);
        }
      }
    } else if (message instanceof MemberUp) {
      MemberUp mUp = (MemberUp) message;
      register(mUp.member());

    } else if (message instanceof DeadLetter) {
      DeadLetter deadLetter = (DeadLetter) message;
      System.out.println("rahul deadletter receive " + deadLetter.message().toString());

    }else if (message.equals("print")) {
      System.out.println("current state for campaign " + campaignId + "= " + campaignState);
      getSender().tell(campaignState.toString(), getSelf());

    } else if (message instanceof SaveSnapshotSuccess) {
      System.out.println("rahul savesnapshot success");
      // ...
    } else if (message instanceof SaveSnapshotFailure) {
      System.out.println("savesnapshot fail");
      // ...
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

  void register(Member member) {
    if (member.hasRole("frontend2"))
      System.out.println("rahul sending msg to frontend" + member.address());
      getContext().actorSelection(member.address() + "/user/frontend").tell(
          new TransformationMessages.BackendRegistration(campaignId), getSelf());
  }
}
//#backend
