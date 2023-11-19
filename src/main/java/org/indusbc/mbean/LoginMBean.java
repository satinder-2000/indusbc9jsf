package org.indusbc.mbean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import java.io.Serializable;
import java.util.Date;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.indusbc.collections.Access;
import org.indusbc.util.AccessType;
import org.indusbc.util.PasswordUtil;

/**
 *
 * @author singh
 */
@Named(value = "loginMBean")
@ViewScoped
public class LoginMBean implements Serializable {
    
    
    private String email;
    private String password;
    private boolean accountLocked;
    
    public String login(){
        String toReturn=null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext)facesContext.getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        Access access = accessColl.find(Filters.eq("email", email)).first();
        //Check the Password
        String passwordEnc = PasswordUtil.generateSecurePassword(password, email);
        if (!passwordEnc.equals(access.getPassword())){
            if (access.getFailedAttempts()==3){
                FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Account Locked","Acount Locked"));
            }else{
                access.setFailedAttempts(access.getFailedAttempts()+1);
                Bson filter = Filters.and(Filters.eq("email", access.getEmail()),Filters.eq("accessType", access.getAccessType()));
                accessColl.replaceOne(filter, access);
                if (access.getFailedAttempts()==3){
                    FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Account Locked","Acount Locked"));  
                }else {
                    String errorMsg=String.format("Login failed. %d attempts left", (3-access.getFailedAttempts()));
                    FacesContext.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_ERROR,errorMsg,errorMsg)); 
                }
            }
            return null;
        }else{
            access.setLastAccessedOn(new Date());
            access.setFailedAttempts(0);
            Bson filter=Filters.and(Filters.eq("email", access.getEmail()), Filters.eq("accessType", access.getAccessType()));
            accessColl.replaceOne(filter, access);
            //Redirects now
            if(access.getAccessType().equals(AccessType.ExpenseParty.toString())){
                toReturn = "/home/ExpensePartyHome?faces-redirect=true";
            }else if(access.getAccessType().equals(AccessType.RevenueParty.toString())){
                toReturn = "/home/RevenuePartyHome?faces-redirect=true";
            }
            
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

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
    
}
    

    
