package cluster.java.transformation;

public class TransformationBackendApp3 {

  public static void main(String[] args) throws InterruptedException {
    // starting 2 frontend nodes and 3 backend nodes
    TransformationBackendMain.main(new String[] { "2553", "campaign4", "campaign5" });
  }
}
