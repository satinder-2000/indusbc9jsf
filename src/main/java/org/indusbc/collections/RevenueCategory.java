package org.indusbc.collections;

import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class RevenueCategory {
    
    private ObjectId _id;
    private String revenueCategory;
    private int year;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getRevenueCategory() {
        return revenueCategory;
    }

    public void setRevenueCategory(String revenueCategory) {
        this.revenueCategory = revenueCategory;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    
    
    
    
}
