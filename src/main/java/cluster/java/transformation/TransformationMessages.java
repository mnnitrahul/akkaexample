package cluster.java.transformation;

import java.io.Serializable;

//#messages
public interface TransformationMessages {

  public static class TransformationJob implements Serializable {
    private final String text;

    public TransformationJob(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  }

  public static class TransformationResult implements Serializable {
    private final String text;

    public TransformationResult(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }

    @Override
    public String toString() {
      return "TransformationResult(" + text + ")";
    }
  }

  public static class JobFailed implements Serializable {
    private final String reason;
    private final TransformationJob job;

    public JobFailed(String reason, TransformationJob job) {
      this.reason = reason;
      this.job = job;
    }

    public String getReason() {
      return reason;
    }

    public TransformationJob getJob() {
      return job;
    }

    @Override
    public String toString() {
      return "JobFailed(" + reason + ")";
    }
  }

  public static class BackendRegistration implements Serializable {
    private final String campaignId;

    public BackendRegistration(String campaignId) {
      this.campaignId = campaignId;
    }

    public String getCampaignId() {
      return campaignId;
    }
  }
}
//#messages