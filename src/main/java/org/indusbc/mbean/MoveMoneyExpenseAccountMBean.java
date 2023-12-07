package org.indusbc.mbean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Logger;
import org.bson.BsonObjectId;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.indusbc.collections.ExpenseAccount;
import org.indusbc.collections.ExpenseAccountTransaction;
import org.indusbc.util.FinancialYear;

/**
 *
 * @author singh
 */
@Named(value = "moveMoneyExpenseAccountMBean")
@ViewScoped
public class MoveMoneyExpenseAccountMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(MoveMoneyExpenseAccountMBean.class.getName());
    
    private ExpenseAccount expenseAccount;
    private String moneyIn;
    private String moneyOut;
    
    @PostConstruct
    public void init(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        ServletContext servletContext = (ServletContext) externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ExpenseAccount> expenseAccountColl=mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        ObjectId revenueAccountObjectId = new ObjectId(request.getParameter("accountId"));
        Bson filter = Filters.eq("_id", revenueAccountObjectId);
        expenseAccount = expenseAccountColl.find(filter).first();
        
    }
    
    public String payMoneyIn(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        LOGGER.info(String.format("Pay Money In request for %s", moneyIn));
        ExpenseAccountTransaction eat = new ExpenseAccountTransaction();
        eat.setExpenseAccountId(expenseAccount.getId());
        eat.setYear(FinancialYear.financialYear());
        eat.setMoneyIn(moneyIn);
        eat.setCreatedOn(new Date());
        BigDecimal ytdBalanceBd=new BigDecimal(expenseAccount.getYtdBalance()).add(new BigDecimal(moneyIn));
        moneyIn="0";
        expenseAccount.setYtdBalance(ytdBalanceBd.toString());
        eat.setYtdBalance(expenseAccount.getYtdBalance());
        //Create eat in DB First
        ServletContext servletContext = (ServletContext)externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ExpenseAccountTransaction> expenseAccountTransactionColl=mongoDatabase.getCollection("ExpenseAccountTransaction", ExpenseAccountTransaction.class);
        InsertOneResult insertOneResult = expenseAccountTransactionColl.insertOne(eat);
        String id = ((BsonObjectId) insertOneResult.getInsertedId()).getValue().toHexString();
        LOGGER.info(String.format("ExpenseAccountTransaction created with ID: %s", id));
        Bson filterExpAcct=Filters.eq("_id", expenseAccount.getId());
        MongoCollection<ExpenseAccount> expenseAccountColl=mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
        expenseAccountColl.replaceOne(filterExpAcct, expenseAccount);
        LOGGER.info(String.format("ExpenseAccount with ID of %s updated.", expenseAccount.getId().toString()));
        FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction completed.","Transaction completed."));
        return null;
    }
    
    public String payMoneyOut(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        LOGGER.info(String.format("Pay Money Out request for %s", moneyOut));
        //Error if moneyOut > ytdBalance
        if (new BigDecimal(moneyOut).compareTo(new BigDecimal(expenseAccount.getYtdBalance()))==1){
            FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Money Out cannot be more than YTD Balance","Money Out cannot be more than YTD Balance"));
            return null;
        }else{
            ExpenseAccountTransaction eat = new ExpenseAccountTransaction();
            eat.setExpenseAccountId(expenseAccount.getId());
            eat.setYear(FinancialYear.financialYear());
            eat.setMoneyOut(moneyOut);
            eat.setCreatedOn(new Date());
            BigDecimal ytdBalanceBd = new BigDecimal(expenseAccount.getYtdBalance()).subtract(new BigDecimal(moneyOut));
            moneyOut = "0";
            expenseAccount.setYtdBalance(ytdBalanceBd.toString());
            eat.setYtdBalance(expenseAccount.getYtdBalance());
            //Create eat in DB First
            ServletContext servletContext = (ServletContext) externalContext.getContext();
            MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
            MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
            MongoCollection<ExpenseAccountTransaction> expenseAccountTransactionColl = mongoDatabase.getCollection("ExpenseAccountTransaction", ExpenseAccountTransaction.class);
            InsertOneResult insertOneResult = expenseAccountTransactionColl.insertOne(eat);
            String id = ((BsonObjectId) insertOneResult.getInsertedId()).getValue().toHexString();
            LOGGER.info(String.format("ExpenseAccountTransaction created with ID: %s", id));
            Bson filterExpAcct = Filters.eq("_id", expenseAccount.getId());
            MongoCollection<ExpenseAccount> expenseAccountColl = mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
            expenseAccountColl.replaceOne(filterExpAcct, expenseAccount);
            LOGGER.info(String.format("ExpenseAccount with ID of %s updated.", expenseAccount.getId().toString()));
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction completed.", "Transaction completed."));
            return null;
        }
        
    }

    public ExpenseAccount getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(ExpenseAccount expenseAccount) {
        this.expenseAccount = expenseAccount;
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
    
    
    
    
    

    
}
