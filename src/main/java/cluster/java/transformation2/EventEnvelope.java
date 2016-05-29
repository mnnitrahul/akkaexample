package cluster.java.transformation2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by rahul.ka on 20/05/16.
 */

@AllArgsConstructor
@Getter
@ToString
public class EventEnvelope implements Serializable {
    final private String id;
    final private Object payload;

}
