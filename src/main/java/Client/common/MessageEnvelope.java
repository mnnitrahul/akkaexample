package Client.common;

import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * Created by rahul.ka on 20/05/16.
 */
@AllArgsConstructor
public class MessageEnvelope implements Serializable {
    final public String id;
    final public Object payload;

}
