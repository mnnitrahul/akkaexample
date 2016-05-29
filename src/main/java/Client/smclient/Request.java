package Client.smclient;

import Client.common.EntityType;
import Client.common.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by rahul.ka on 20/05/16.
 */

@AllArgsConstructor
@Getter
public class Request {
    private String requestId;
    private EntityType entityType;
    private String entityId;
    private Event event;
    private long eventTimestamp;
    private String version;

}
