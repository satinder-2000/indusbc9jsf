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
import org.indusbc.collections.RevenueAccount;
import org.indusbc.collections.RevenueAccountTransaction;
import org.indusbc.util.FinancialYear;

/**
 *
 * @author singh
 */
@Named(value = "moveMoneyRevenueAccountMBean")
@ViewScoped
public class MoveMoneyRevenueAccountMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(MoveMoneyRevenueAccountMBean.class.getName());
    private RevenueAccount revenueAccount;
    private String moneyIn;
    private String moneyOut;
    
    @PostConstruct
    public void init(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        ServletContext servletContext = (ServletContext)externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<RevenueAccount> revenueAccountColl=mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        ObjectId revenueAccountObjectId = new ObjectId(request.getParameter("accountId"));
        Bson filter = Filters.eq("_id", revenueAccountObjectId);
        revenueAccount = revenueAccountColl.find(filter).first();
    }
    
    public String payMoneyIn(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        LOGGER.info(String.format("Pay Money In request for %s", moneyIn));
        RevenueAccountTransaction rat = new RevenueAccountTransaction();
        rat.setRevenueAccountId(revenueAccount.getId());
        rat.setYear(FinancialYear.financialYear());
        rat.setMoneyIn(moneyIn);
        rat.setCreatedOn(new Date());
        BigDecimal ytdBalanceBd=new BigDecimal(revenueAccount.getYtdBalance()).add(new BigDecimal(moneyIn));
        moneyIn="0";
        revenueAccount.setYtdBalance(ytdBalanceBd.toString());
        rat.setYtdBalance(revenueAccount.getYtdBalance());
        //Create rat in DB First
        ServletContext servletContext = (ServletContext)externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<RevenueAccountTransaction> revenueAccountTransactionColl=mongoDatabase.getCollection("RevenueAccountTransaction", RevenueAccountTransaction.class);
        InsertOneResult insertOneResult = revenueAccountTransactionColl.insertOne(rat);
        String id = ((BsonObjectId) insertOneResult.getInsertedId()).getValue().toHexString();
        LOGGER.info(String.format("RevenueAccountTransaction created with ID: %s", id));
        Bson filterRevAcct=Filters.eq("_id", revenueAccount.getId());
        MongoCollection<RevenueAccount> revenueAccountColl=mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        revenueAccountColl.replaceOne(filterRevAcct, revenueAccount);
        LOGGER.info(String.format("RevenueAccount with ID of %s updated.", revenueAccount.getId().toString()));
        FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction completed.","Transaction completed."));
        return null;
    }
    
    public String payMoneyOut(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        LOGGER.info(String.format("Pay Money Out request for %s", moneyOut));
        //Error if moneyOut > ytdBalance
        if (new BigDecimal(moneyOut).compareTo(new BigDecimal(revenueAccount.getYtdBalance()))==1){
            FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Money Out cannot be more than YTD Balance","Money Out cannot be more than YTD Balance"));
            return null;
        }else{
            RevenueAccountTransaction rat = new RevenueAccountTransaction();
            rat.setRevenueAccountId(revenueAccount.getId());
            rat.setYear(FinancialYear.financialYear());
            rat.setMoneyOut(moneyOut);
            rat.setCreatedOn(new Date());
            BigDecimal ytdBalanceBd = new BigDecimal(revenueAccount.getYtdBalance()).subtract(new BigDecimal(moneyOut));
            moneyOut = "0";
            revenueAccount.setYtdBalance(ytdBalanceBd.toString());
            rat.setYtdBalance(revenueAccount.getYtdBalance());
            //Create rat in DB First
            ServletContext servletContext = (ServletContext) externalContext.getContext();
            MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
            MongoDatabase mongoDatabase = mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
            MongoCollection<RevenueAccountTransaction> revenueAccountTransactionColl = mongoDatabase.getCollection("RevenueAccountTransaction", RevenueAccountTransaction.class);
            InsertOneResult insertOneResult = revenueAccountTransactionColl.insertOne(rat);
            String id = ((BsonObjectId) insertOneResult.getInsertedId()).getValue().toHexString();
            LOGGER.info(String.format("RevenueAccountTransaction created with ID: %s", id));
            Bson filterRevAcct = Filters.eq("_id", revenueAccount.getId());
            MongoCollection<RevenueAccount> revenueAccountColl = mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
            revenueAccountColl.replaceOne(filterRevAcct, revenueAccount);
            LOGGER.info(String.format("RevenueAccount with ID of %s updated.", revenueAccount.getId().toString()));
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction completed.", "Transaction completed."));
            return null;
        }
        
    }

    public RevenueAccount getRevenueAccount() {
        return revenueAccount;
    }

    public void setRevenueAccount(RevenueAccount revenueAccount) {
        this.revenueAccount = revenueAccount;
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
