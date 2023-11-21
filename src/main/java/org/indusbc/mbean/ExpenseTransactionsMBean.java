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
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.indusbc.collections.ExpenseAccount;
import org.indusbc.collections.ExpenseAccountTransaction;

/**
 *
 * @author singh
 */
@Named(value = "expenseTransactionsMBean")
@RequestScoped
public class ExpenseTransactionsMBean {
    
    private static final Logger LOGGER = Logger.getLogger(ExpenseTransactionsMBean.class.getName());
    
    private ExpenseAccount expenseAccount;
    private List<ExpenseAccountTransaction> expenseAccountTransactions;
    
    @PostConstruct
    public void init(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        new ObjectId(request.getParameter("accountId"));
        ServletContext servletContext = (ServletContext)externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ExpenseAccount> expenseAcctColl = mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
        ObjectId expAcctId=new ObjectId(request.getParameter("accountId"));
        Bson filter=Filters.eq("_id", expAcctId);
        expenseAccount =expenseAcctColl.find(filter).first();
        MongoCollection<ExpenseAccountTransaction> expenseAcctTxColl = mongoDatabase.getCollection("ExpenseAccountTransaction", ExpenseAccountTransaction.class);
        Bson filterTx=Filters.eq("expenseAccountId", expAcctId);
        Iterable<ExpenseAccountTransaction> expenseAcctTxItrable= expenseAcctTxColl.find(filterTx);
        Iterator<ExpenseAccountTransaction> expenseAcctTxItr = expenseAcctTxItrable.iterator();
        expenseAccountTransactions = new ArrayList<>();
        while(expenseAcctTxItr.hasNext()){
            expenseAccountTransactions.add(expenseAcctTxItr.next());
        }
        
    }

    public ExpenseAccount getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(ExpenseAccount expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public List<ExpenseAccountTransaction> getExpenseAccountTransactions() {
        return expenseAccountTransactions;
    }

    public void setExpenseAccountTransactions(List<ExpenseAccountTransaction> expenseAccountTransactions) {
        this.expenseAccountTransactions = expenseAccountTransactions;
    }
    
    
    
    
}
