package Client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by rahul.ka on 20/05/16.
 */

@AllArgsConstructor
@Getter
public class Message {
    private String requestId;
    private Event event;
    private long eventTimestamp;
}
