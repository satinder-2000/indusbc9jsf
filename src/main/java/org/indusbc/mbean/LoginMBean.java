package org.indusbc.mbean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.indusbc.collections.Access;
import org.indusbc.util.AccessType;

/**
 *
 * @author singh
 */
@Named(value = "loginMBean")
@ViewScoped
public class LoginMBean implements Serializable {
    
    private String email;
    private String password;
    
    public String login(){
        String toReturn=null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext)facesContext.getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        Access access=accessColl.find(Filters.eq("email", email)).first();
        HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute("access", access);
        if(access.getAccessType().equals(AccessType.EXPENSE_PARTY.getShortName())){
            toReturn = "/home/ExpensePartyHome?faces-redirect=true";
        }else if(access.getAccessType().equals(AccessType.REVENUE_PARTY.getShortName())){
            toReturn = "/home/RevenuePartyHome?faces-redirect=true";
        } 
        return toReturn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
    
}
