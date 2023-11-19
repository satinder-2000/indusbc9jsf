package org.indusbc.collections;

import java.util.Date;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class RevenueAccountTransaction {
    
    private ObjectId _id;
    private ObjectId revenueAccountId;
    private int year;
    private String moneyIn;
    private String moneyOut;
    private String ytdBalance;
    private Date createdOn;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getRevenueAccountId() {
        return revenueAccountId;
    }

    public void setRevenueAccountId(ObjectId revenueAccountId) {
        this.revenueAccountId = revenueAccountId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMoneyIn() {
        return moneyIn;
    }

    public void setMoneyIn(String moneyIn) {
        this.moneyIn = moneyIn;
    }

    public String getMoneyOut() {
        return moneyOut;
    }

    public void setMoneyOut(String moneyOut) {
        this.moneyOut = moneyOut;
    }

    public String getYtdBalance() {
        return ytdBalance;
    }

    public void setYtdBalance(String ytdBalance) {
        this.ytdBalance = ytdBalance;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
    
    
    
    
}
