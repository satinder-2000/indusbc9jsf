package org.indusbc.mbean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.indusbc.collections.Access;
import org.indusbc.collections.ProofOfIdDocument;
import org.indusbc.util.IndusbcConstants;
import org.indusbc.util.PasswordUtil;

/**
 *
 * @author singh
 */
@Named(value = "accessCreateMBean")
@ViewScoped
public class AccessCreateMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(AccessCreateMBean.class.getName());
    
    private Access access;
    private String confirmPassword;
    
    @PostConstruct
    public void init(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext)facesContext.getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
        String email = request.getParameter("email");
        access=accessColl.find(Filters.eq("email", email)).first();
    }
    
    public String processForm(){
        FacesContext context = FacesContext.getCurrentInstance();
        String toReturn=null;
        String password=access.getPassword();
        String passwordConfirm=getConfirmPassword();
        if (password.trim().isEmpty()){
            FacesContext.getCurrentInstance().addMessage("password",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"No Password entered","No Password entered"));
        }else{
            //First, RegEx the password
            Pattern pCdIn=Pattern.compile(IndusbcConstants.PW_REGEX);
            Matcher mPCdIn=pCdIn.matcher(password);
            if (!mPCdIn.find()){
                FacesContext.getCurrentInstance().addMessage("password",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Password","Invalid Password"));  
            }else{//compare the password now
                if(!password.equals(passwordConfirm)){
                    FacesContext.getCurrentInstance().addMessage("confirmPassword",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"Passwords mismatch","Passwords mismatch"));
                }
                
            }
        }
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && !msgs.isEmpty()){
            toReturn =null;
        }else{
            access.setPassword(PasswordUtil.generateSecurePassword(access.getPassword(), access.getEmail()));
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ServletContext servletContext = (ServletContext)facesContext.getExternalContext().getContext();
            MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
            MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
            MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
            Bson filter = Filters.and(Filters.eq("email", access.getEmail()),Filters.eq("accessType", access.getAccessType()));
            accessColl.replaceOne(filter, access);
            toReturn="/home/UserWelcome?faces-redirect=true";
        }
        
        return toReturn;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    
    
    
    
}
