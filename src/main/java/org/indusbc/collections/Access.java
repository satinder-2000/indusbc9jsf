package org.indusbc.collections;

import java.util.Date;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class Access {
    
    private ObjectId _id;
    private ObjectId partyId;
    private String email;
    private String password;
    private String accessType;
    private Date lastAccessedOn;
    private int failedAttempts=0;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getPartyId() {
        return partyId;
    }

    public void setPartyId(ObjectId partyId) {
        this.partyId = partyId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public Date getLastAccessedOn() {
        return lastAccessedOn;
    }

    public void setLastAccessedOn(Date lastAccessedOn) {
        this.lastAccessedOn = lastAccessedOn;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    
    
    
    
}
