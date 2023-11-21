package org.indusbc.collections;

import java.util.Date;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class RevenueAccount {
    
    private ObjectId _id;
    private String name;
    private ObjectId revenuePartyId;
    private String revenueAccountHash;
    private Date createdOn;
    private String ytdBalance="0.0";

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectId getRevenuePartyId() {
        return revenuePartyId;
    }

    public void setRevenuePartyId(ObjectId revenuePartyId) {
        this.revenuePartyId = revenuePartyId;
    }

    public String getRevenueAccountHash() {
        return revenueAccountHash;
    }

    public void setRevenueAccountHash(String revenueAccountHash) {
        this.revenueAccountHash = revenueAccountHash;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getYtdBalance() {
        return ytdBalance;
    }

    public void setYtdBalance(String ytdBalance) {
        this.ytdBalance = ytdBalance;
    }
    
    
    
}
