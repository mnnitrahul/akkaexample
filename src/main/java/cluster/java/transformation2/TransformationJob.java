package cluster.java.transformation2;

import java.io.Serializable;

/**
 * Created by rahul.ka on 16/05/16.
 */
public class TransformationJob implements Serializable {
    private final String text;

    public TransformationJob(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
