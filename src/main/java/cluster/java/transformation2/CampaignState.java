package cluster.java.transformation2;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by rahul.ka on 09/05/16.
 */
public class CampaignState implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ArrayList<String> states;

    public CampaignState() {
        this(new ArrayList<String>());
    }

    public CampaignState(ArrayList<String> states) {
        this.states = states;
    }

    public CampaignState copy() {
        return new CampaignState(new ArrayList<String>(states));
    }

    public void update(String s) {
        states.add(s);
    }

    @Override
    public String toString() {
        return states.toString();
    }
}

