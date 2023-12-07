package org.indusbc.admin;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.indusbc.collections.CentralAccount;
import org.indusbc.collections.ExpenseAccount;
import org.indusbc.collections.ExpenseAllocation;
import org.indusbc.collections.ExpenseCategory;
import org.indusbc.collections.RevenueAccount;
import org.indusbc.collections.RevenueAllocation;
import org.indusbc.collections.RevenueCategory;
import org.indusbc.ejbs.AllocationEjbLocal;

/**
 *
 * @author singh
 */
@Named(value = "allocationsAdminMBean")
@ViewScoped
public class AllocationsAdminMBean implements Serializable {
    
    private static Logger LOGGER = Logger.getLogger(AllocationsAdminMBean.class.getName());
    
    private int year;
    private String granularity;
    private String allocationJobName;
    
    private MongoClient mongoClient;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext= (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        mongoClient=(MongoClient)servletContext.getAttribute("mongoClient");
    }
    
    public String startAllocation(){
        ServletContext servletContext= (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        Bson filter=Filters.eq("year", year);
        MongoCollection<CentralAccount> centralAccountColl=mongoDatabase.getCollection("CentralAccount", CentralAccount.class);
        DeleteResult deleteResult = centralAccountColl.deleteMany(filter);
        LOGGER.info(String.format("Count of CentralAccout(s) deleted %d",deleteResult.getDeletedCount()));
        
        //Step1: Gather RevenueCategory. It will have certain categories that won't have RevenueAllocation
        MongoCollection<RevenueCategory> revenueCategoryColl=mongoDatabase.getCollection("RevenueCategory", RevenueCategory.class);
        FindIterable<RevenueCategory> revenueCategoryItrable= revenueCategoryColl.find(filter);
        List<RevenueCategory> revenueCategoryList = new ArrayList<>();
        Iterator<RevenueCategory> revenueCategoryItr = revenueCategoryItrable.iterator();
        while(revenueCategoryItr.hasNext()){
           revenueCategoryList.add(revenueCategoryItr.next());
        }
        LOGGER.info(String.format("Count of RevenueCategories is %d", revenueCategoryList.size()));
        
        //Step2: Gather RevenueAccount. There won't be any RevenueAccount where RevenueAllocation has not been made.
        MongoCollection<RevenueAccount> revenueAccountColl = mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        List<RevenueAccount> revenueAccountList = new ArrayList<>();
        FindIterable<RevenueAccount> revenueAccountItrable= revenueAccountColl.find(filter);
        Iterator<RevenueAccount> revenueAccountItr=revenueAccountItrable.iterator();
        while(revenueAccountItr.hasNext()){
            revenueAccountList.add(revenueAccountItr.next());
        }
        LOGGER.info(String.format("Count of RevenueAccounts is %d", revenueAccountList.size()));
        
        
        //Step3: Gather RevenueAllocation.Won't have Allocation to all the RevenueCategory
        MongoCollection<RevenueAllocation> revenueAllocationColl=mongoDatabase.getCollection("RevenueAllocation", RevenueAllocation.class);
        Bson filterRC=Filters.and(Filters.eq("year", year),Filters.gt("allocation", "0"));
        FindIterable<RevenueAllocation> revenueAllocationItrable= revenueAllocationColl.find(filterRC);
        List<RevenueAllocation> revenueAllocationList = new ArrayList<>();
        Iterator<RevenueAllocation> revenueAllocationItr = revenueAllocationItrable.iterator();
        while(revenueAllocationItr.hasNext()){
           revenueAllocationList.add(revenueAllocationItr.next());
        }
        LOGGER.info(String.format("Count of RevenueAllocations is %d", revenueAllocationList.size()));
        
        //Step 4: Before we proceed further we need to drop those RevenueCategories where there is no RevenueAllocation for the year
        List<RevenueCategory> revenueCategoryListClean = new ArrayList<>();
        outer: for(RevenueCategory revCat : revenueCategoryList){
            for (RevenueAllocation ral : revenueAllocationList) {
            if (revCat.getRevenueCategory().equals(ral.getRevenueCategory())) {
                revenueCategoryListClean.add(revCat);
                continue outer;
            }
        }
            
        }
        
        //HERE Step 5: Determine the count of RevenueAccount each RevenueCategory in revenueAllocationListClean
        //Will be used to allocate the RevenueAllocation equally across the Accounts.
        Map<String, Integer> revenueAccountByCategoryMap=new HashMap<>();
        for(RevenueCategory rc: revenueCategoryList){
            Integer accountCt=revenueAccountByCategoryMap.get(rc.getRevenueCategory());
            if (accountCt==null){
                revenueAccountByCategoryMap.put(rc.getRevenueCategory(), 1);
            }else{
                revenueAccountByCategoryMap.put(rc.getRevenueCategory(),accountCt+1);
            }
        }
        
        //At this stage the revenueAccountByCategoryMap Map should be all populated. Let's print the values
        Set<String> revCatSet=revenueAccountByCategoryMap.keySet();
        Iterator<String> revCatSetItr = revCatSet.iterator();
        while(revCatSetItr.hasNext()){
            String revCat = revCatSetItr.next();
            LOGGER.info(String.format("Count of RevenueCategory %s is %d", revCat, revenueAccountByCategoryMap.get(revCat)));
        }
        
        //Hold RevenueAllocation for RevenueCategory
        Map<String, BigDecimal> revCatAllocMap = new HashMap<>();
        for(RevenueAllocation revAlloc : revenueAllocationList){
            String revCat=revAlloc.getRevenueCategory();
            //Do we have mapping of this recCat in revCatAllocMap?
            BigDecimal revCatAllocation = revCatAllocMap.get(revCat);
            if (revCatAllocation == null){
                revCatAllocMap.put(revCat, new BigDecimal(revAlloc.getAllocation()));
            }
        }
        
        //At this stage the revCatAllocMap should be all populated. Let's print the values
        Set<String> revCatAllocMapKeys=revCatAllocMap.keySet();
        Iterator<String> revCatAllocMapKeysItr=revCatAllocMapKeys.iterator();
        while(revCatAllocMapKeysItr.hasNext()){
             String revCat=revCatAllocMapKeysItr.next();
             BigDecimal revCatAllocValue = revCatAllocMap.get(revCat);
             LOGGER.info(String.format("RevenueAllocation category %s has %.2f allocation", revCat,revCatAllocValue));
         }
         
         //And update the RevenueAccount ytdBalance
         for (RevenueAllocation ralloc : revenueAllocationList) {
            String revCat = ralloc.getRevenueCategory();
            BigDecimal revCatAlloc = revCatAllocMap.get(revCat);
            if (revCatAlloc.equals(new BigDecimal("0"))) {
                //No allocation made
                //No allocation made
            } else {
                BigDecimal revCatAllocPerAcct = revCatAlloc.divide(new BigDecimal(revenueAccountByCategoryMap.get(revCat)));
                for (RevenueAccount ra : revenueAccountList) {
                    if (ra.getName().equals(revCat)) {
                        ra.setYtdBalance(revCatAllocPerAcct.toString());
                    }
                    //Bson filterRa = Filters.eq("_id", ra.getId());
                    //revenueAccountColl.replaceOne(filterRa, ra);
                }
            }

        }
         
        //Step 5 Lets load the merged RevenueAccount(s) to poputate CentralAccount(s)
        revenueAccountColl = mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        revenueAccountList = new ArrayList<>();
        revenueAccountItrable = revenueAccountColl.find(filter);
        revenueAccountItr = revenueAccountItrable.iterator();
        while (revenueAccountItr.hasNext()) {
            revenueAccountList.add(revenueAccountItr.next());
        }
        
        //Step6 CentralAccount Collection
        List<CentralAccount> centralAccountList=new ArrayList<>();
        for(RevenueAccount ra : revenueAccountList){
            while (new BigDecimal(ra.getYtdBalance()).compareTo(BigDecimal.ZERO)==1){
                CentralAccount ca=new CentralAccount();
                ca.setYear(year);
                ca.setRevenueAccountHash(ra.getRevenueAccountHash());
                ca.setAmount(granularity);
                ca.setAccountName(ra.getName() + " to ");
                ca.setTransactionDate(LocalDateTime.now());
                //Will set other fields such as Timestamp and Account Name (append Expense Account Name0 when we popuate the Expense records
                ra.setYtdBalance(new BigDecimal(ra.getYtdBalance()).subtract(new BigDecimal(granularity)).toString());
                centralAccountList.add(ca);
            }
        }
        //Commenting the code below. Hopefully we have enough memory to contain the CentralAccount records
        //InsertManyResult result= centralAccountColl.insertMany(centralAccountList);
        //LOGGER.info(String.format("Count of CentralAccounts created is %d", result.getInsertedIds().size()));
         
        //Dealing with the expense side now.
        //Step7 Gather ExpenseCategory
        MongoCollection<ExpenseCategory> expenseCategoryColl=mongoDatabase.getCollection("ExpenseCategory", ExpenseCategory.class);
        FindIterable<ExpenseCategory> expenseCategoryItrable= expenseCategoryColl.find(filter);
        List<ExpenseCategory> expenseCategoryList = new ArrayList<>();
        Iterator<ExpenseCategory> expenseCategoryItr = expenseCategoryItrable.iterator();
        while(expenseCategoryItr.hasNext()){
           expenseCategoryList.add(expenseCategoryItr.next());
        }
        LOGGER.info(String.format("Count of ExpenseCategories is %d", expenseCategoryList.size()));
        
        //Step8 Load ExpenseAllocation
        MongoCollection<ExpenseAllocation> expenseAllocColl=mongoDatabase.getCollection("ExpenseAllocation", ExpenseAllocation.class);
        FindIterable<ExpenseAllocation> expenseAllocationItrable=expenseAllocColl.find(filter);
        List<ExpenseAllocation> expenseAllocationList=new ArrayList<>();
        Iterator<ExpenseAllocation> expenseAllocationItr = expenseAllocationItrable.iterator();
        while(expenseAllocationItr.hasNext()){
            expenseAllocationList.add(expenseAllocationItr.next());
        }
        LOGGER.info(String.format("Count of ExpenseAllocation is %d", expenseAllocationList.size()));
        
        //Step9 Load ExpenseAccount
        MongoCollection<ExpenseAccount> expenseAccountColl=mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
        FindIterable<ExpenseAccount> expenseAccountItrable=expenseAccountColl.find(filter);
        List<ExpenseAccount> expenseAccountList=new ArrayList<>();
        Iterator<ExpenseAccount> expenseAccountItr = expenseAccountItrable.iterator();
        while(expenseAccountItr.hasNext()){
            expenseAccountList.add(expenseAccountItr.next());
        }
        LOGGER.info(String.format("Count of ExpenseAccounts is %d", expenseAccountList.size()));
        
        //Step10 Number of ExpenseAccounts per ExpenseCategory
        Map<String, Integer> expenseAccountByCategoryMap=new HashMap<>();
        for(ExpenseCategory ec: expenseCategoryList){
            Integer accountCt=revenueAccountByCategoryMap.get(ec.getExpenseCategory());
            if (accountCt==null){
                expenseAccountByCategoryMap.put(ec.getExpenseCategory(), 1);
            }else{
                expenseAccountByCategoryMap.put(ec.getExpenseCategory(),accountCt+1);
            }
        }
        
        //At this stage the expenseAccountByCategoryMap Map should be all populated. Let's print the values
        Set<String> keysExpAcct = expenseAccountByCategoryMap.keySet();
        Iterator<String> keysExpAcctItr = keysExpAcct.iterator();
        while (keysExpAcctItr.hasNext()) {
            String expCatStrKey = keysExpAcctItr.next();
            LOGGER.info(String.format("ExpenseCategory %s has %d accounts", expCatStrKey, expenseAccountByCategoryMap.get(expCatStrKey)));
        }
        
        //Step11 Hold allocation for ExpenseCategory
        Map<String, BigDecimal> expCatAllocnsMap = new HashMap<>();
        //Store the allocated amount for each ExpenseCategory
        for(ExpenseAllocation eAloc : expenseAllocationList){
            String expCatStr=eAloc.getExpenseCategory();
            //Do we have mapping of this expCatStr in exp
            BigDecimal expCatAlloc=expCatAllocnsMap.get(expCatStr);
            if (expCatAlloc==null){
                expCatAllocnsMap.put(expCatStr, new BigDecimal(eAloc.getAllocation()));
            }
        }
        //At this stage the expCatAllocs Map should be all populated. Let's print the values
        Set<String> expKeysAlloc = expCatAllocnsMap.keySet();
        Iterator<String> expKeysItrAlloc = expKeysAlloc.iterator();
        while (expKeysItrAlloc.hasNext()) {
            String expCatAllocStrKey = expKeysItrAlloc.next();
            BigDecimal allocationVal=expCatAllocnsMap.get(expCatAllocStrKey);
            LOGGER.info(String.format("ExpenseAllocation category %s has %.2f allocation", expCatAllocStrKey, allocationVal));
        }
        
        //And merge ExpenseAccount(s) with the DB
        for (ExpenseAllocation eal : expenseAllocationList) {
            String expCat = eal.getExpenseCategory();
            BigDecimal expCatAlloc = expCatAllocnsMap.get(expCat);
            BigDecimal expCatAllocPerAcct = expCatAlloc.divide(new BigDecimal(expenseAccountByCategoryMap.get(expCat)));
            for (ExpenseAccount ea : expenseAccountList) {
                if(ea.getName().equals(expCat)){
                     ea.setYtdBalance(expCatAllocPerAcct.toString());
                }
            }
        }
         //These CAs has full granularty amount populated.
        List<CentralAccount> asIsAccountsToKeep = new ArrayList<>();
        //If we split a CA into two because of granularty amount issue, we populate then in newCentralAccounts and mark the current CA for removal
        List<CentralAccount> centralAccountsToRemove = new ArrayList<>();
        //All the new Central Accounts to be posted  - effectively created from centralAccountsToRemove.
        List<CentralAccount> newCentralAccounts = new ArrayList<>();
        //Logic to pupulate each Central Account record by 10000 
        int counterForCentralAccount=0;
        CentralAccount centralAccountOnHold=null;   
        
        outer: for (ExpenseAccount ea : expenseAccountList)  {
            inner : while(true && counterForCentralAccount< centralAccountList.size()){
                if (centralAccountOnHold!=null){//Revenue related fields already have been populated
                   centralAccountOnHold.setExpenseAccountHash(ea.getExpenseAccountHash());
                   centralAccountOnHold.setTransactionDate(LocalDateTime.now());
                   centralAccountOnHold.setAccountName(centralAccountOnHold.getAccountName().concat(ea.getName()));
                   newCentralAccounts.add(centralAccountOnHold);
                   centralAccountOnHold=null;
                   //counterForCentralAccount++;
                   continue inner;
                }
                CentralAccount ca = centralAccountList.get(counterForCentralAccount);
                
                if (new BigDecimal(ea.getYtdBalance()).compareTo(new BigDecimal(granularity)) == 1)//EA YTD BAL > granurarity
                {
                    ca.setExpenseAccountHash(ea.getExpenseAccountHash());
                    ca.setTransactionDate(LocalDateTime.now());
                    ca.setAmount(granularity);
                    ea.setYtdBalance(new BigDecimal(ea.getYtdBalance()).subtract(new BigDecimal(granularity)).toString());
                    ca.setAccountName(ca.getAccountName().concat(ea.getName()));
                    asIsAccountsToKeep.add(ca);
                    counterForCentralAccount +=1 ;
                    continue inner;
                } else if (new BigDecimal(ea.getYtdBalance()).compareTo(new BigDecimal(granularity)) == -1)//EA YTD BAL > granurarity
                {
                    //mark the original CA record for deletion
                    centralAccountsToRemove.add(ca);
                    //And then create two CA Records.
                    //First with YTD Bal of current EA
                    CentralAccount caNew1 = new CentralAccount();
                    caNew1.setAccountName(ca.getAccountName().concat(ea.getName()));
                    caNew1.setYear(ca.getYear());
                    caNew1.setRevenueAccountHash(ca.getRevenueAccountHash());
                    caNew1.setExpenseAccountHash(ea.getExpenseAccountHash());
                    caNew1.setTransactionDate(LocalDateTime.now());
                    caNew1.setAmount(ea.getYtdBalance());
                    newCentralAccounts.add(caNew1);
                    CentralAccount caNew2 = new CentralAccount();
                    caNew2.setAccountName(ca.getAccountName());//Expense side of name completed in next iteration.
                    caNew2.setYear(ca.getYear());
                    caNew2.setRevenueAccountHash(ca.getRevenueAccountHash());
                    caNew2.setAmount(new BigDecimal(granularity).subtract(new BigDecimal(caNew1.getAmount())).toString());
                    //Rest of the props from the New EA such a accountName.
                    centralAccountOnHold = caNew2;
                    counterForCentralAccount +=1 ;
                    continue outer;
                }
                
            }
        }
        
        
        //Merge(s) and Persist(s)
        centralAccountColl.insertMany(asIsAccountsToKeep);
        centralAccountColl.insertMany(newCentralAccounts);
        /*for(CentralAccount ca: asIsAccountsToKeep){
            centralAccountColl.insertMany(newCentralAccounts);
            //centralAccountEjbLocal.saveAllocation(ca);
        }
        for(CentralAccount ca: centralAccountsToRemove){
            centralAccountColl..d
            centralAccountEjbLocal.removeCentralAccount(ca);
        }
        /*for(CentralAccount ca: newCentralAccounts){
            centralAccountEjbLocal.createAllocation(ca);
        }*/
        return null;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public String getAllocationJobName() {
        return allocationJobName;
    }

    public void setAllocationJobName(String allocationJobName) {
        this.allocationJobName = allocationJobName;
    }
    
    
    
    
}
