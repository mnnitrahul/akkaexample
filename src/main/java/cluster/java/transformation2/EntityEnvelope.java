package cluster.java.transformation2;

import java.io.Serializable;

/**
 * Created by rahul.ka on 14/05/16.
 */
public class EntityEnvelope implements Serializable{
    final public long id;
    final public Object payload;

    public EntityEnvelope(long id, Object payload) {
        this.id = id;
        this.payload = payload;
    }
}