package org.indusbc.util;

/**
 *
 * @author singh
 */
public enum AccessType {
    
    REVENUE_PARTY("rp","Revenue Party"),
    EXPENSE_PARTY("ep","Expense Party");
    
    AccessType(String shortName, String name){
        this.shortName=shortName;
        this.name = name;
    }
    private final String shortName;
    private final String name;

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }
    
    public static AccessType getByShortName(String shortName){
        if (shortName.equals("rp")){
            return REVENUE_PARTY;
        }else{
            return EXPENSE_PARTY;
        }
    } 

}
