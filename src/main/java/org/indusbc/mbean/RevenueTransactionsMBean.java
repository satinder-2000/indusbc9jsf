package org.indusbc.mbean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.indusbc.collections.RevenueAccount;
import org.indusbc.collections.RevenueAccountTransaction;

/**
 *
 * @author singh
 */
@Named(value = "revenueTransactionsMBean")
@RequestScoped
public class RevenueTransactionsMBean {
    
    private static final Logger LOGGER = Logger.getLogger(RevenueTransactionsMBean.class.getName());
    private RevenueAccount revenueAccount;
    private List<RevenueAccountTransaction> revenueAccountTransactions;
    
    @PostConstruct
    public void init(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        ServletContext servletContext = (ServletContext)externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<RevenueAccount> revenueAcctColl = mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        ObjectId revAcctId=new ObjectId(request.getParameter("accountId"));
        Bson filter=Filters.eq("_id", revAcctId);
        revenueAccount =revenueAcctColl.find(filter).first();
        MongoCollection<RevenueAccountTransaction> revenueAcctTxColl = mongoDatabase.getCollection("RevenueAccountTransaction", RevenueAccountTransaction.class);
        Bson filterTx=Filters.eq("revenueAccountId", revAcctId);
        Iterable<RevenueAccountTransaction> revenueAcctTxItrable= revenueAcctTxColl.find(filterTx);
        Iterator<RevenueAccountTransaction> revenueAcctTxItr = revenueAcctTxItrable.iterator();
        revenueAccountTransactions = new ArrayList<>();
        while(revenueAcctTxItr.hasNext()){
            revenueAccountTransactions.add(revenueAcctTxItr.next());
        }
        
    }

    public RevenueAccount getRevenueAccount() {
        return revenueAccount;
    }

    public void setRevenueAccount(RevenueAccount revenueAccount) {
        this.revenueAccount = revenueAccount;
    }

    public List<RevenueAccountTransaction> getRevenueAccountTransactions() {
        return revenueAccountTransactions;
    }

    public void setRevenueAccountTransactions(List<RevenueAccountTransaction> revenueAccountTransactions) {
        this.revenueAccountTransactions = revenueAccountTransactions;
    }
    
    
    
    
    
}
