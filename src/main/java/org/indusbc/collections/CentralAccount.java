package org.indusbc.collections;

import java.util.Date;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class CentralAccount {
    
    private ObjectId _id;
    private int year;
    private String accountName;
    private String revenueAccountHash;
    private String expenseAccountHash;
    private String amount;
    private Date transactionDate;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    

    public String getRevenueAccountHash() {
        return revenueAccountHash;
    }

    public void setRevenueAccountHash(String revenueAccountHash) {
        this.revenueAccountHash = revenueAccountHash;
    }

    public String getExpenseAccountHash() {
        return expenseAccountHash;
    }

    public void setExpenseAccountHash(String expenseAccountHash) {
        this.expenseAccountHash = expenseAccountHash;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    
    
    
    
}
