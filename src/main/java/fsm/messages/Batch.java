package fsm.messages;

import java.util.List;

/**
 * Created by rahul.ka on 04/05/16.
 */
public final class Batch {
    private final List<Object> list;

    public Batch(List<Object> list) {
        this.list = list;
    }

    public List<Object> getList() {
        return list;
    }
    // boilerplate ...
}
