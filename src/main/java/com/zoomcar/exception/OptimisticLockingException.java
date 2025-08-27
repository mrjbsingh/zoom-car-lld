package com.zoomcar.exception;

/**
 * Exception thrown when optimistic locking fails due to concurrent modifications
 */
public class OptimisticLockingException extends RuntimeException {
    
    private final String entityType;
    private final String entityId;
    private final Long expectedVersion;
    private final Long actualVersion;
    
    public OptimisticLockingException(String message) {
        super(message);
        this.entityType = null;
        this.entityId = null;
        this.expectedVersion = null;
        this.actualVersion = null;
    }
    
    public OptimisticLockingException(String entityType, String entityId, 
                                    Long expectedVersion, Long actualVersion) {
        super(String.format("Optimistic locking failed for %s with ID %s. Expected version: %d, Actual version: %d", 
                           entityType, entityId, expectedVersion, actualVersion));
        this.entityType = entityType;
        this.entityId = entityId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
    
    public OptimisticLockingException(String message, Throwable cause) {
        super(message, cause);
        this.entityType = null;
        this.entityId = null;
        this.expectedVersion = null;
        this.actualVersion = null;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public String getEntityId() {
        return entityId;
    }
    
    public Long getExpectedVersion() {
        return expectedVersion;
    }
    
    public Long getActualVersion() {
        return actualVersion;
    }
}
