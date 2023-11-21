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
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.indusbc.collections.Access;
import org.indusbc.collections.RevenueAccount;

/**
 *
 * @author singh
 */
@Named(value = "revenueAccountsActionsMBean")
@ViewScoped
public class RevenueAccountsActionsMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(RevenueAccountsActionsMBean.class.getName());
    private List<RevenueAccount> partyRevenueAccounts;
    
    @PostConstruct
    public void init(){
        partyRevenueAccounts = new ArrayList<>();
        ExternalContext externalContext= FacesContext.getCurrentInstance().getExternalContext();
        ServletContext servletContext = (ServletContext)externalContext.getContext();
        MongoClient mongoClient = (MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<RevenueAccount> revenueAccountColl=mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        HttpServletRequest request=(HttpServletRequest) externalContext.getRequest();
        HttpSession session = request.getSession(false);
        Access access = (Access)session.getAttribute("access");
        Bson filter=Filters.and(Filters.eq("revenuePartyId", access.getPartyId()));
        Iterable<RevenueAccount> revenueAccountItrable = revenueAccountColl.find(filter, RevenueAccount.class);
        Iterator<RevenueAccount> revenueAccountItr = revenueAccountItrable.iterator();
        while(revenueAccountItr.hasNext()){
            partyRevenueAccounts.add(revenueAccountItr.next());
        }
        
    }

    public List<RevenueAccount> getPartyRevenueAccounts() {
        return partyRevenueAccounts;
    }

    public void setPartyRevenueAccounts(List<RevenueAccount> partyRevenueAccounts) {
        this.partyRevenueAccounts = partyRevenueAccounts;
    }
    
    
    
    
}
