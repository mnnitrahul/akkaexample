package cluster.java.sharding;

import java.io.Serializable;

/**
 * Created by rahul.ka on 14/05/16.
 */
public class CounterChanged implements Serializable{
    final public int delta;

    public CounterChanged(int delta) {
        this.delta = delta;
    }
}