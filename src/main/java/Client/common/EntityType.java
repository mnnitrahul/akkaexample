package Client.common;

/**
 * Created by rahul.ka on 20/05/16.
 */
public enum EntityType {
    CAMPAIGN("Campaign");
    private String entityType;
    EntityType(String entityType) {
        this.entityType = entityType;
    }
    public String getEntityType() {
        return entityType;
    }
}
