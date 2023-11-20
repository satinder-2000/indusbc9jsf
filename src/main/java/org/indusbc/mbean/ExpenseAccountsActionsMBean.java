package org.indusbc.mbean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
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
import org.indusbc.collections.Access;
import org.indusbc.collections.ExpenseAccount;
import org.indusbc.collections.ExpenseAccountTransaction;

/**
 *
 * @author singh
 */
@Named(value = "expenseAccountsActionsMBean")
@ViewScoped
public class ExpenseAccountsActionsMBean implements Serializable{
    
    private static final Logger LOGGER = Logger.getLogger(ExpenseAccountsActionsMBean.class.getName());
    
    private List<ExpenseAccount> partyExpenseAccounts;
    
    private ExpenseAccountTransaction expenseAccountTransaction;
    
    @PostConstruct
    public void init(){
        partyExpenseAccounts = new ArrayList<>();
        ExternalContext externalContext= FacesContext.getCurrentInstance().getExternalContext();
        ServletContext servletContext = (ServletContext)externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ExpenseAccount> expenseAccountColl=mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
        HttpServletRequest request=(HttpServletRequest) externalContext.getRequest();
        HttpSession session = request.getSession(false);
        Access access = (Access)session.getAttribute("access");
        Bson filter=Filters.and(Filters.eq("expensePartyId", access.getPartyId()));
        Iterable<ExpenseAccount> expenseAccountItrable = expenseAccountColl.find(filter, ExpenseAccount.class);
        Iterator<ExpenseAccount> expenseAccountItr = expenseAccountItrable.iterator();
        while(expenseAccountItr.hasNext()){
            partyExpenseAccounts.add(expenseAccountItr.next());
        }
        
    }

    public List<ExpenseAccount> getPartyExpenseAccounts() {
        return partyExpenseAccounts;
    }

    public void setPartyExpenseAccounts(List<ExpenseAccount> partyExpenseAccounts) {
        this.partyExpenseAccounts = partyExpenseAccounts;
    }

    public ExpenseAccountTransaction getExpenseAccountTransaction() {
        return new ExpenseAccountTransaction();
    }

    public void setExpenseAccountTransaction(ExpenseAccountTransaction expenseAccountTransaction) {
        this.expenseAccountTransaction = expenseAccountTransaction;
    }
    
    
    
    

    
}
