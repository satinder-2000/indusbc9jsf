package org.indusbc.ejbs;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
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
import org.indusbc.collections.RevenueParty;


/**
 *
 * @author singh
 */
@Stateless
public class AllocationEjb implements AllocationEjbLocal {
    
    private static final Logger LOGGER = Logger.getLogger(AllocationEjb.class.getName());
    
    private MongoClient mongoClient;
    
    
    @PostConstruct
    public void init(){
        
        
    }

    @Override
    public Future<String> performAllocations(String allocationJobName, String granularity, int year) {
        ServletContext servletContext= (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        
        Bson filter=Filters.eq("year", year);
        MongoCollection<RevenueCategory> revenueCategoryColl=mongoDatabase.getCollection("RevenueCategory", RevenueCategory.class);
        FindIterable<RevenueCategory> revenueCategoryItrable= revenueCategoryColl.find(filter);
        List<RevenueCategory> revenueCategoryList = new ArrayList<>();
        Iterator<RevenueCategory> revenueCategoryItr = revenueCategoryItrable.iterator();
        while(revenueCategoryItr.hasNext()){
           revenueCategoryList.add(revenueCategoryItr.next());
        }
        LOGGER.info(String.format("Count of RevenueCategories is %d", revenueCategoryList.size()));
        
        MongoCollection<RevenueAccount> revenueAccountColl = mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        List<RevenueAccount> revenueAccountList = new ArrayList<>();
        FindIterable<RevenueAccount> revenueAccountItrable= revenueAccountColl.find(filter);
        Iterator<RevenueAccount> revenueAccountItr=revenueAccountItrable.iterator();
        while(revenueAccountItr.hasNext()){
            revenueAccountList.add(revenueAccountItr.next());
        }
        LOGGER.info(String.format("Count of RevenueAccounts is %d", revenueAccountList.size()));
        MongoCollection<RevenueAllocation> revenueAllocationColl=mongoDatabase.getCollection("RevenueAllocation", RevenueAllocation.class);
        FindIterable<RevenueAllocation> revenueAllocationItrable= revenueAllocationColl.find(filter);
        List<RevenueAllocation> revenueAllocationList = new ArrayList<>();
        Iterator<RevenueAllocation> revenueAllocationItr = revenueAllocationItrable.iterator();
        while(revenueAllocationItr.hasNext()){
           revenueAllocationList.add(revenueAllocationItr.next());
        }
        LOGGER.info(String.format("Count of RevenueAllocations is %d", revenueAllocationList.size()));
        
        Map<String, Integer> revenueAccountByCategoryMap=new HashMap<>();
        for(RevenueCategory rc: revenueCategoryList){
            Integer accountCt=revenueAccountByCategoryMap.get(rc.getRevenueCategory());
            if (accountCt==null){
                revenueAccountByCategoryMap.put(rc.getRevenueCategory(), 1);
            }else{
                revenueAccountByCategoryMap.put(rc.getRevenueCategory(),accountCt.intValue()+1);
            }
        }
        //At this stage the revenueAccountByCategoryMap Map should be all populated. Let's print the values
        Set<String> revCatSet=revenueAccountByCategoryMap.keySet();
        Iterator<String> revCatSetItr = revCatSet.iterator();
        while(revCatSetItr.hasNext()){
            String revCat = revCatSetItr.next();
            LOGGER.info(String.format("Count of RevenueCategory %s is %d", revCat, revenueAccountByCategoryMap.get(revCat)));
        }
        
         //Hold allocation for RevenueCategory
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
             String revCatAllocKey=revCatAllocMapKeysItr.next();
             BigDecimal revCatAllocValue = revCatAllocMap.get(revCatAllocKey);
             LOGGER.info(String.format("RevenueAllocation category %s has %.2f allocation", revCatAllocKey,revCatAllocValue));
         }
         //And merge RevenueAccount(s) with the DB
         for(RevenueAllocation ralloc : revenueAllocationList){
            String revCat = ralloc.getRevenueCategory();
            BigDecimal revCatAlloc = revCatAllocMap.get(revCat);
            BigDecimal revCatAllocPerAcct = revCatAlloc.divide(new BigDecimal(revenueAccountByCategoryMap.get(revCat)));
            for (RevenueAccount ra : revenueAccountList) {
                if (ra.getName().equals(revCat)){
                    ra.setYtdBalance(revCatAllocPerAcct.toString());
                }
                Bson filterRa=Filters.eq("_id", ra.getId());
                revenueAccountColl.replaceOne(filterRa, ra);
            }
         }
         
         //Lets load the merged RevenueAccount(s) to poputate CentralAccount(s)
        revenueAccountColl = mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        revenueAccountList = new ArrayList<>();
        revenueAccountItrable = revenueAccountColl.find(filter);
        revenueAccountItr = revenueAccountItrable.iterator();
        while (revenueAccountItr.hasNext()) {
            revenueAccountList.add(revenueAccountItr.next());
        }
        //CentralAccount Collection
        MongoCollection<CentralAccount> centralAccountColl=mongoDatabase.getCollection("CentralAccount", CentralAccount.class);
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
        InsertManyResult result= centralAccountColl.insertMany(centralAccountList);
        LOGGER.info(String.format("Count of CentralAccounts created is %d", result.getInsertedIds().size()));
         
        //Dealing with the expense side now.
        
        MongoCollection<ExpenseCategory> expenseCategoryColl=mongoDatabase.getCollection("ExpenseCategory", ExpenseCategory.class);
        FindIterable<ExpenseCategory> expenseCategoryItrable= expenseCategoryColl.find(filter);
        List<ExpenseCategory> expenseCategoryList = new ArrayList<>();
        Iterator<ExpenseCategory> expenseCategoryItr = expenseCategoryItrable.iterator();
        while(expenseCategoryItr.hasNext()){
           expenseCategoryList.add(expenseCategoryItr.next());
        }
        LOGGER.info(String.format("Count of ExpenseCategories is %d", expenseCategoryList.size()));
        //Load ExpenseAllocation(s) first
        MongoCollection<ExpenseAllocation> expenseAllocColl=mongoDatabase.getCollection("ExpenseAllocation", ExpenseAllocation.class);
        FindIterable<ExpenseAllocation> expenseAllocationItrable=expenseAllocColl.find(filter);
        List<ExpenseAllocation> expenseAllocationList=new ArrayList<>();
        Iterator<ExpenseAllocation> expenseAllocationItr = expenseAllocationItrable.iterator();
        while(expenseAllocationItr.hasNext()){
            expenseAllocationList.add(expenseAllocationItr.next());
        }
        LOGGER.info(String.format("Count of ExpenseAllocation is %d", expenseAllocationList.size()));
        
        MongoCollection<ExpenseAccount> expenseAccountColl=mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
        FindIterable<ExpenseAccount> expenseAccountItrable=expenseAccountColl.find(filter);
        List<ExpenseAccount> expenseAccountList=new ArrayList<>();
        Iterator<ExpenseAccount> expenseAccountItr = expenseAccountItrable.iterator();
        while(expenseAccountItr.hasNext()){
            expenseAccountList.add(expenseAccountItr.next());
        }
        LOGGER.info(String.format("Count of ExpenseAccounts is %d", expenseAccountList.size()));
        
        //Number of ExpenseAccounts per ExpenseCategory
        Map<String, Integer> expenseAccountByCategoryMap=new HashMap<>();
        for(ExpenseCategory ec: expenseCategoryList){
            Integer accountCt=revenueAccountByCategoryMap.get(ec.getExpenseCategory());
            if (accountCt==null){
                expenseAccountByCategoryMap.put(ec.getExpenseCategory(), 1);
            }else{
                expenseAccountByCategoryMap.put(ec.getExpenseCategory(),accountCt.intValue()+1);
            }
        }
        //At this stage the expenseAccountByCategoryMap Map should be all populated. Let's print the values
        Set<String> keysExpAcct = expenseAccountByCategoryMap.keySet();
        Iterator<String> keysExpAcctItr = keysExpAcct.iterator();
        while (keysExpAcctItr.hasNext()) {
            String expCatStrKey = keysExpAcctItr.next();
            LOGGER.info(String.format("ExpenseCategory %s has %d accounts", expCatStrKey, expenseAccountByCategoryMap.get(expCatStrKey)));
        }
        
        //Hold allocation for ExpenseCategory
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
        //expenseAccountColl.r.updateMany(filter, expenseAccountList);
            
        
        
        return null;
    }
    
}
